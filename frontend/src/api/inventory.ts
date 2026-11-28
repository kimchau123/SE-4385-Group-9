import { apiFetch } from './client'

export type PersonSkill = {
    personId: number
    skillId: number
    skillProficiencyLevelId: number
    status: 'APPROVED' | 'REQUESTED' | 'CANCLED'
}

const BASE_URL = '/api/inventory'

export async function getPersonSkills(
    personId: number
): Promise<PersonSkill[]> {
    const response = await apiFetch(
        `${BASE_URL}/persons/${personId}`
    )

    if (!response.ok) {
        throw new Error('Failed to load employee skills')
    }
    
    return response.json()
}