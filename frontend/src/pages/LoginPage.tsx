import { useState, type FormEvent } from 'react'
import { useAuth } from '../auth/useAuth'
import './LoginPage.css'

export function LoginPage() {
    const { login } = useAuth()
    const [personId, setPersonId] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        setError('')
        setLoading(true)
        try {
            await login(personId.trim(), password)
        } catch (err) {
            if (err instanceof Error) {
                setError(err.message)
            } else {
                setError('Login failed')
            }
        } finally {
            setLoading(false)
        }
    }
    return (
        <div className="login-container">
            <div className="login-card">
                <div className="login-brand">
                    <h1>Skills Hub</h1>
                    <p>SABRE CORPORATION</p>
                </div>

                <h2>Login</h2>
                <p className="login-description"> Sign in to access Skills Hub. </p>

                <form onSubmit={handleSubmit}>
                    <label htmlFor="personId">Person ID</label>
                    <input id="personId" type="text" value={personId} onChange={(e) => setPersonId(e.target.value)} placeholder="Enter your Person ID" autoComplete="username" required />

                    <label htmlFor="password">Password</label>
                    <input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Enter your password" autoComplete="current-password" required />

                    {error && (<p className="login-error" role="alert"> {error} </p> )}

                    <button type="submit"disabled={loading}className="login-button">
                        {loading ? 'Signing in...' : 'Sign in'}
                    </button>
                </form>
            </div>
        </div>
    )
}