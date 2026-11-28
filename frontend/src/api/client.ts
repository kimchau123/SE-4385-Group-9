let authorizationHeader: string | null = null
export function setCredentials(
    personId:string,
    password:string
) { authorizationHeader = `Basic ${btoa(`${personId}:${password}`)}` 
} 
export function clearCredentials() { 
    authorizationHeader = null 
} 
export async function apiFetch( 
    url: string, 
    options: RequestInit = {} 
): Promise<Response> { 
    const headers = new Headers(options.headers) 
    if (authorizationHeader) { 
        headers.set('Authorization', authorizationHeader) 
    } 
    return fetch(url, {...options, headers }) 
}