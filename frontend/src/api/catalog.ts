// Skills Catalog API used by the Workforce Planner pages.
//
// Endpoint paths follow the Detailed Design (Phase 1 and Phase 5 sequence diagrams):
//   GET  /api/skills?query=&type=&status=     filterSkills / getAllSkills
//   POST /api/skills                          createSkill        -> 201, status REQUESTED
//   PUT  /api/skills/{id}                     updateSkill
//   PUT  /api/skills/{id}/approve | /deny     approveSkill / denySkill
//   GET  /api/inventory/skills/{skillId}      people rated on a skill (SkillsInventoryController.searchBySkill)
// The design does not give paths for proficiency levels, so they are collected in PATHS to change in one place.
//
// While the backend catalog endpoints are not available, the app runs on demo data (catalogMock.ts).
// Set VITE_USE_MOCK_API=false (see .env.example) to call the real backend through the Vite /api proxy.
import { mockCatalogApi } from './catalogMock'
import {
  ApiError,
  type CatalogApi,
  type CatalogSkill,
  type ProficiencyLevel,
  type SkillInput,
  type SkillResource,
} from './catalogTypes'

export const isMockApi = import.meta.env.VITE_USE_MOCK_API !== 'false'

const PATHS = {
  skills: '/api/skills',
  levels: '/api/proficiency-levels',
  skillResources: (skillId: number) => `/api/inventory/skills/${skillId}`,
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response
  try {
    response = await fetch(path, {
      ...init,
      headers: { Accept: 'application/json', ...(init?.body ? { 'Content-Type': 'application/json' } : {}) },
    })
  } catch {
    throw new ApiError(0, 'Cannot reach the server. Check that the backend is running.')
  }
  const text = await response.text()
  if (!response.ok) {
    let message = `Request failed (${response.status})`
    try {
      const body = JSON.parse(text) as { message?: string }
      if (body.message) message = body.message
    } catch {
      // keep the generic message when the body is not JSON
    }
    throw new ApiError(response.status, message)
  }
  return (text ? JSON.parse(text) : undefined) as T
}

const send = (method: string, body?: unknown): RequestInit => ({ method, body: body === undefined ? undefined : JSON.stringify(body) })

const skillBody = (input: SkillInput) => ({
  skillName: input.skillName.trim(),
  skillDescription: input.skillDescription.trim(),
  skillType: input.skillType,
})

const httpCatalogApi: CatalogApi = {
  listSkills({ query, type, status }) {
    const params = new URLSearchParams()
    if (query.trim()) params.set('query', query.trim())
    if (type !== 'ALL') params.set('type', type)
    if (status !== 'ALL') params.set('status', status)
    const queryString = params.toString()
    return request<CatalogSkill[]>(queryString ? `${PATHS.skills}?${queryString}` : PATHS.skills)
  },
  createSkill: (input) => request<CatalogSkill>(PATHS.skills, send('POST', skillBody(input))),
  updateSkill: (skillId, input) => request<CatalogSkill>(`${PATHS.skills}/${skillId}`, send('PUT', skillBody(input))),
  approveSkill: (skillId) => request<void>(`${PATHS.skills}/${skillId}/approve`, send('PUT')),
  denySkill: (skillId) => request<void>(`${PATHS.skills}/${skillId}/deny`, send('PUT')),
  listProficiencyLevels: () => request<ProficiencyLevel[]>(PATHS.levels),
  createProficiencyLevel: (name) => request<ProficiencyLevel>(PATHS.levels, send('POST', { skillProficiencyLevelName: name.trim() })),
  updateProficiencyLevel: (levelId, name) =>
    request<ProficiencyLevel>(`${PATHS.levels}/${levelId}`, send('PUT', { skillProficiencyLevelName: name.trim() })),
  listSkillResources: (skillId) => request<SkillResource[]>(PATHS.skillResources(skillId)),
}

export const catalogApi: CatalogApi = isMockApi ? mockCatalogApi : httpCatalogApi
