export type CurrentUser = {
    personId: number
    personName: string
    role: string
    isAdmin: boolean
}

export async function login(
    personId:string,
    password:string
): Promise<CurrentUser> {
    const credentials = btoa(`${personId}:${password}`)
    const response = await fetch('/api/auth/me', {
        method:'GET',
        headers: {
            Authorization: `Basic ${credentials}`
        }      
    })
    if (response.status === 401) {
        throw new Error('Invalid person ID or password')
    }
    if (!response.ok) {
        throw new Error('Unable to log in')
    }
    return response.json()
}