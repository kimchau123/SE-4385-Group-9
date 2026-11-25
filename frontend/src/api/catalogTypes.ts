// Types, limits and validation for the Skills Catalog (Workforce Planner).
// Names and enum values mirror the backend Tier 4 entities (Skill, SkillProficiencyLevel, PersonSkill).

export const SKILL_TYPES = ['KNOWLEDGE', 'EXPERIENCE', 'TECHNOLOGY', 'OTHER'] as const
export type SkillType = (typeof SKILL_TYPES)[number]

export const APPROVAL_STATUSES = ['REQUESTED', 'APPROVED', 'CANCELED'] as const
export type ApprovalStatus = (typeof APPROVAL_STATUSES)[number]

export const TYPE_LABELS: Record<SkillType, string> = {
  KNOWLEDGE: 'Knowledge',
  EXPERIENCE: 'Experience',
  TECHNOLOGY: 'Technology',
  OTHER: 'Other',
}

export const STATUS_LABELS: Record<ApprovalStatus, string> = {
  REQUESTED: 'Requested',
  APPROVED: 'Approved',
  CANCELED: 'Canceled',
}

// Limits from the Test Plan (TC-WP-01 / TC-WP-02, boundary value analysis).
export const NAME_MIN = 1
export const NAME_MAX = 50
export const DESCRIPTION_MIN = 10
export const DESCRIPTION_MAX = 500
export const LEVEL_NAME_MAX = 30

export type CatalogSkill = {
  skillId: number
  skillName: string
  skillDescription: string | null
  skillType: SkillType
  status: ApprovalStatus
}

export type SkillInput = {
  skillName: string
  skillDescription: string
  skillType: SkillType
}

export type ProficiencyLevel = {
  skillProficiencyLevelId: number
  skillProficiencyLevelName: string
}

/** One person's rating of a skill (backend PersonSkill). */
export type SkillResource = {
  personId: number
  skillId: number
  skillProficiencyLevelId: number
  status: ApprovalStatus
}

export type SkillFilters = {
  query: string
  type: SkillType | 'ALL'
  status: ApprovalStatus | 'ALL'
}

export type CatalogApi = {
  listSkills(filters: SkillFilters): Promise<CatalogSkill[]>
  createSkill(input: SkillInput): Promise<CatalogSkill>
  updateSkill(skillId: number, input: SkillInput): Promise<CatalogSkill>
  approveSkill(skillId: number): Promise<void>
  denySkill(skillId: number): Promise<void>
  listProficiencyLevels(): Promise<ProficiencyLevel[]>
  createProficiencyLevel(name: string): Promise<ProficiencyLevel>
  updateProficiencyLevel(levelId: number, name: string): Promise<ProficiencyLevel>
  listSkillResources(skillId: number): Promise<SkillResource[]>
}

export class ApiError extends Error {
  status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

export function describeError(error: unknown): string {
  if (error instanceof ApiError) {
    if (error.status === 403) return "You don't have permission to do that."
    return error.message
  }
  return 'Something went wrong. Please try again.'
}

export type FieldErrors = Partial<Record<'skillName' | 'skillDescription' | 'skillType', string>>

/** Client-side version of the backend rules, so users get feedback before a 400 round trip. */
export function validateSkillInput(input: { skillName: string; skillDescription: string; skillType: string }): FieldErrors {
  const errors: FieldErrors = {}
  const name = input.skillName.trim()
  const description = input.skillDescription.trim()
  if (name.length < NAME_MIN) errors.skillName = 'Skill name is required.'
  else if (name.length > NAME_MAX) errors.skillName = `Skill name must be ${NAME_MAX} characters or fewer.`
  if (description.length < DESCRIPTION_MIN) errors.skillDescription = `Description must be at least ${DESCRIPTION_MIN} characters.`
  else if (description.length > DESCRIPTION_MAX) errors.skillDescription = `Description must be ${DESCRIPTION_MAX} characters or fewer.`
  if (!(SKILL_TYPES as readonly string[]).includes(input.skillType)) errors.skillType = 'Select a skill type.'
  return errors
}

export function validateLevelName(name: string): string | null {
  const trimmed = name.trim()
  if (trimmed.length === 0) return 'Level name is required.'
  if (trimmed.length > LEVEL_NAME_MAX) return `Level name must be ${LEVEL_NAME_MAX} characters or fewer.`
  return null
}
