import { createContext, useContext } from 'react'
import type { CurrentUser } from '../api/auth'

export type AuthContextType = {
    user: CurrentUser | null
    isAuthenticated: boolean
    isAdmin: boolean
    login: (personId: string, password: string) => Promise<void>
    logout: () => void
}

export const AuthContext = createContext<AuthContextType | null>(null)

export function useAuth() {
    const context = useContext(AuthContext)
    if (!context) {
        throw new Error('useAuth must be used within AuthProvider')
    }
    return context
}