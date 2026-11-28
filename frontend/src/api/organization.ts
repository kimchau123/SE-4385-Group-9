export type OrganizationHierarchy = {
    organizationID: number
    organizationName: string
    managerPersonId: number | null
    managerName: string | null
    memberCount: number
    children: OrganizationHierarchy[]
}

export type Person = {
    personId: number
    personName: string
    managerPersonId: number | null
    memberOfOrganizationId: number | null
    admin: boolean
}

export type OrganizationPersonSkill = {
    personId: number
    skillId: number
    skillName: string
    skillProficiencyLevelId:number
    skillProficiencyLevelName: string
    status: 'APPROVED' | 'REQUESTED' | 'CANCELED'
}

export type OrganizationSkillDistribution = {
    skillId: number
    skillName: string
    employeeCount: number

}

const BASE_URL = '/api/organizations'

export async function getOrganizationHierarchy():
    Promise<OrganizationHierarchy[]> {
        const response = await fetch(`${BASE_URL}/hierarchy`)
        if (!response.ok) {
            throw new Error('Failed to load organization hierarchy')
        }
        return response.json()
    }

export async function getOrganizationMembers(orgID: number,includeSubOrganizations: boolean = false): Promise<Person[]> {
    const response = await fetch(
        `${BASE_URL}/${orgID}/members?includeSubOrganizations=${includeSubOrganizations}`
    )
    if (!response.ok) {
        throw new Error('Failed to load members')
    }
    return response.json()
}

export async function getPersonSkillDetails(personId: number): Promise<OrganizationPersonSkill[]> {
    const response = await fetch(
        `${BASE_URL}/persons/${personId}/skills`
    )
    if (!response.ok) {
        throw new Error('Failed to load employee skill details') 
    }
    return response.json()
}

export async function getSkillDistribution( 
    orgId: number, 
    skillId: number | null = null, 
    proficiencyLevelId: number | null = null 
): Promise<OrganizationSkillDistribution[]> { 
    const params = new URLSearchParams() 
    if (skillId !== null) { 
        params.set('skillId', String(skillId)) 
    } 
    if (proficiencyLevelId !== null) { 
        params.set('proficiencyLevelId', String(proficiencyLevelId)) 
    } 
    const response = await fetch(`${BASE_URL}/${orgId}/skills-distribution?${params.toString()}`)
    if (!response.ok) { 
        throw new Error('Failed to load skills distribution') 
    } 
    return response.json() 
}

export async function getSkillSummary(orgId: number, includeSubOrganizations: boolean = false): Promise<OrganizationSkillDistribution[]> {
    const response = await fetch(
        `${BASE_URL}/${orgId}/skills-summary?includeSubOrganizations=${includeSubOrganizations}`
    )
    if (!response.ok) {
        throw new Error('Failed to load skills summary')
    }
    return response.json()
}

export async function filterOrganizationMembers(
    orgId: number,
    skillId: number | null,
    proficiencyLevelId: number | null,
    includeSubOrganizations: boolean = false
): Promise<Person[]> {
    const params = new URLSearchParams()
    params.set(
        'includeSubOrganizations',
        String(includeSubOrganizations)
    )
    if (skillId !== null) {
        params.set('skillId', String(skillId))
    }
    if (proficiencyLevelId !== null) {
        params.set(
            'proficiencyLevelId',
            String(proficiencyLevelId)
        )
    }
    const response = await fetch(
        `${BASE_URL}/${orgId}/members/filter?${params.toString()}`
    )
    if (!response.ok) {
        throw new Error('Failed to filter organization members')
    }
    return response.json()
}

export type OrganizationProficiencyLevel = {
  skillProficiencyLevelId: number
  skillProficiencyLevelName: string
}

export async function getProficiencyLevels(): Promise<OrganizationProficiencyLevel[]> {
  const response = await fetch(`${BASE_URL}/proficiency-levels`)
  if (!response.ok) {
    throw new Error('Failed to load proficiency levels')
  }
  return response.json()
}

export async function getPersonById(personId: number): Promise<Person> {
  const response = await fetch(
    `${BASE_URL}/persons/${personId}`
  );
  if (!response.ok) {
    throw new Error('Failed to load person details');
  }
  return response.json();
}

export type OrganizationSkillCapacity = { 
    totalEmployees: number 
    employeesWithSkills: number 
    employeesWithoutSkills: number 
    distinctSkills: number 
    skillCoveragePercentage: number 
    proficiencyDistribution: Record<string, number> 
} 

export async function getSkillCapacity(orgId: number): Promise<OrganizationSkillCapacity> { 
    const response = await fetch( `${BASE_URL}/${orgId}/skills-capacity` ) 
    if (!response.ok) { 
        throw new Error('Failed to load skill capacity') 
    } 
    return response.json() 
}

export async function getOverallSkillCapacity(): Promise<OrganizationSkillCapacity> { 
    const response = await fetch(`${BASE_URL}/skills-capacity`) 
    if (!response.ok) { 
        throw new Error('Failed to load overall skill capacity') 
    } 
    return response.json() 
}