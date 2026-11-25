// In-memory stand-in for the Skills Catalog API, used until the backend catalog endpoints (#15) exist.
// It enforces the same rules the Test Plan expects: 400 for invalid input, 409 for duplicates,
// 404 for unknown ids, and REQUESTED -> APPROVED/CANCELED transitions only.
import {
  ApiError,
  validateLevelName,
  validateSkillInput,
  type CatalogApi,
  type CatalogSkill,
  type ProficiencyLevel,
  type SkillResource,
  type SkillType,
} from './catalogTypes'

const delay = () => new Promise<void>((resolve) => setTimeout(resolve, 80))

type Seed = { name: string; description: string; type: SkillType; status: CatalogSkill['status']; resources: number }

const seeds: Seed[] = [
  { name: 'React', type: 'TECHNOLOGY', status: 'APPROVED', resources: 34, description: 'A JavaScript library for building reusable UI components. It helps teams create fast, declarative, and maintainable interfaces for web applications.' },
  { name: 'Java', type: 'TECHNOLOGY', status: 'CANCELED', resources: 7, description: 'An object-oriented programming language used to build reliable enterprise applications.' },
  { name: 'Spring Boot', type: 'TECHNOLOGY', status: 'REQUESTED', resources: 9, description: 'A framework for building production-ready Spring applications.' },
  { name: 'C++', type: 'TECHNOLOGY', status: 'REQUESTED', resources: 21, description: 'A general-purpose language used for high-performance software.' },
  { name: 'Python', type: 'TECHNOLOGY', status: 'APPROVED', resources: 28, description: 'A versatile language used for automation, analytics, and application development.' },
  { name: 'AWS', type: 'TECHNOLOGY', status: 'APPROVED', resources: 16, description: 'Cloud infrastructure and services for secure, scalable workloads.' },
  { name: 'SQL', type: 'KNOWLEDGE', status: 'APPROVED', resources: 19, description: 'A language for querying, transforming, and managing relational data.' },
  { name: 'Communication', type: 'OTHER', status: 'APPROVED', resources: 12, description: 'Clear, considerate communication across teams and stakeholders.' },
  { name: 'Project Management', type: 'EXPERIENCE', status: 'APPROVED', resources: 11, description: 'Planning, tracking and delivering projects on time and within scope.' },
]

let skills: CatalogSkill[] = seeds.map((seed, index) => ({
  skillId: index + 1,
  skillName: seed.name,
  skillDescription: seed.description,
  skillType: seed.type,
  status: seed.status,
}))
let nextSkillId = skills.length + 1

// Ids 1-5 with Beginner = 1 and Expert = 5, as the Test Plan assumes (TC-RM-01, TC-WP-05).
let levels: ProficiencyLevel[] = ['Beginner', 'Elementary', 'Intermediate', 'Advanced', 'Expert'].map((name, index) => ({
  skillProficiencyLevelId: index + 1,
  skillProficiencyLevelName: name,
}))
let nextLevelId = levels.length + 1

const resources: SkillResource[] = seeds.flatMap((seed, seedIndex) =>
  Array.from({ length: seed.resources }, (_, index) => ({
    personId: index + 1,
    skillId: seedIndex + 1,
    skillProficiencyLevelId: (index % 5) + 1,
    status: 'APPROVED' as const,
  })),
)

const sameName = (a: string, b: string) => a.trim().toLowerCase() === b.trim().toLowerCase()

function assertValidSkill(input: { skillName: string; skillDescription: string; skillType: string }) {
  const errors = validateSkillInput(input)
  const first = Object.values(errors)[0]
  if (first) throw new ApiError(400, first)
}

function findSkill(skillId: number): CatalogSkill {
  const found = skills.find((skill) => skill.skillId === skillId)
  if (!found) throw new ApiError(404, `Skill ${skillId} was not found`)
  return found
}

async function review(skillId: number, next: 'APPROVED' | 'CANCELED') {
  await delay()
  const found = findSkill(skillId)
  if (found.status !== 'REQUESTED') throw new ApiError(400, 'Only requested skills can be approved or denied')
  skills = skills.map((skill) => (skill.skillId === skillId ? { ...skill, status: next } : skill))
}

export const mockCatalogApi: CatalogApi = {
  async listSkills({ query, type, status }) {
    await delay()
    const needle = query.trim().toLowerCase()
    return skills
      .filter((skill) => !needle || skill.skillName.toLowerCase().includes(needle))
      .filter((skill) => type === 'ALL' || skill.skillType === type)
      .filter((skill) => status === 'ALL' || skill.status === status)
      .map((skill) => ({ ...skill }))
  },

  async createSkill(input) {
    await delay()
    assertValidSkill(input)
    if (skills.some((skill) => sameName(skill.skillName, input.skillName))) {
      throw new ApiError(409, `A skill named "${input.skillName.trim()}" already exists`)
    }
    const created: CatalogSkill = {
      skillId: nextSkillId++,
      skillName: input.skillName.trim(),
      skillDescription: input.skillDescription.trim(),
      skillType: input.skillType,
      status: 'REQUESTED',
    }
    skills = [...skills, created]
    return { ...created }
  },

  async updateSkill(skillId, input) {
    await delay()
    findSkill(skillId)
    assertValidSkill(input)
    if (skills.some((skill) => skill.skillId !== skillId && sameName(skill.skillName, input.skillName))) {
      throw new ApiError(409, `A skill named "${input.skillName.trim()}" already exists`)
    }
    let updated: CatalogSkill | undefined
    skills = skills.map((skill) => {
      if (skill.skillId !== skillId) return skill
      updated = { ...skill, skillName: input.skillName.trim(), skillDescription: input.skillDescription.trim(), skillType: input.skillType }
      return updated
    })
    return { ...(updated as CatalogSkill) }
  },

  approveSkill: (skillId) => review(skillId, 'APPROVED'),
  denySkill: (skillId) => review(skillId, 'CANCELED'),

  async listProficiencyLevels() {
    await delay()
    return levels.map((level) => ({ ...level }))
  },

  async createProficiencyLevel(name) {
    await delay()
    const problem = validateLevelName(name)
    if (problem) throw new ApiError(400, problem)
    if (levels.some((level) => sameName(level.skillProficiencyLevelName, name))) {
      throw new ApiError(409, `A proficiency level named "${name.trim()}" already exists`)
    }
    const created = { skillProficiencyLevelId: nextLevelId++, skillProficiencyLevelName: name.trim() }
    levels = [...levels, created]
    return { ...created }
  },

  async updateProficiencyLevel(levelId, name) {
    await delay()
    if (!levels.some((level) => level.skillProficiencyLevelId === levelId)) {
      throw new ApiError(404, `Proficiency level ${levelId} was not found`)
    }
    const problem = validateLevelName(name)
    if (problem) throw new ApiError(400, problem)
    if (levels.some((level) => level.skillProficiencyLevelId !== levelId && sameName(level.skillProficiencyLevelName, name))) {
      throw new ApiError(409, `A proficiency level named "${name.trim()}" already exists`)
    }
    let updated: ProficiencyLevel | undefined
    levels = levels.map((level) => {
      if (level.skillProficiencyLevelId !== levelId) return level
      updated = { ...level, skillProficiencyLevelName: name.trim() }
      return updated
    })
    return { ...(updated as ProficiencyLevel) }
  },

  async listSkillResources(skillId) {
    await delay()
    findSkill(skillId)
    return resources.filter((resource) => resource.skillId === skillId).map((resource) => ({ ...resource }))
  },
}
