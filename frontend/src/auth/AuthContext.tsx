import { useState, type ReactNode } from 'react'
import { login as loginRequest, type CurrentUser } from '../api/auth'
import { setCredentials, clearCredentials } from '../api/client'
import { AuthContext } from './useAuth'

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<CurrentUser | null>(null)
    async function login(personId: string, password: string) {
        const currentUser = await loginRequest(personId, password)

        setCredentials(personId, password)
        setUser(currentUser)
    }
    function logout() {
        clearCredentials()
        setUser(null)
    }
    return (
        <AuthContext.Provider value={{ user, isAuthenticated: user !== null, isAdmin: user?.isAdmin ?? false, login, logout, }} >
            {children}
        </AuthContext.Provider>
    )
}