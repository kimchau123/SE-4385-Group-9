import type { ReactNode } from 'react'

export function Icon({ name, size = 17 }: { name: string; size?: number }) {
  const paths: Record<string, ReactNode> = {
    cube: <><path d="m12 3 9 5.2v7.6L12 21l-9-5.2V8.2L12 3Z" /><path d="m3.4 8.4 8.6 5 8.6-5M12 13.4V21" /></>,
    home: <><path d="m3 10 9-7 9 7" /><path d="M5 9v11h14V9M9 20v-6h6v6" /></>,
    catalog: <><path d="M4 5.5A2.5 2.5 0 0 1 6.5 3H20v17H6.5A2.5 2.5 0 0 1 4 17.5z" /><path d="M4 16.5A2.5 2.5 0 0 1 6.5 14H20M8 7h8M8 10h6" /></>,
    layers: <><path d="m12 3 9 5-9 5-9-5 9-5Z" /><path d="m3 12 9 5 9-5M3 16l9 5 9-5" /></>,
    organization: <><rect x="9" y="3" width="6" height="5" rx="1" /><rect x="2.5" y="16" width="6" height="5" rx="1" /><rect x="15.5" y="16" width="6" height="5" rx="1" /><path d="M12 8v4M5.5 16v-4h13v4M18.5 12v4" /></>,
    chart: <><path d="M4 20V10M10 20V4M16 20v-7M22 20H2" /></>,
    search: <><circle cx="10.8" cy="10.8" r="6.8" /><path d="m16 16 5 5" /></>,
    arrow: <><path d="M5 12h14M13 6l6 6-6 6" /></>,
    bell: <><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4" /></>,
  }
  return <svg aria-hidden="true" className="icon" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">{paths[name] ?? paths.layers}</svg>
}

export function PageIntro({ title, description, children }: { title: string; description?: string; children?: ReactNode }) {
  return <div className="page-intro"><div><h1>{title}</h1>{description && <p>{description}</p>}</div>{children && <div className="intro-actions">{children}</div>}</div>
}

export function Metric({ value, label, note }: { value: string; label: string; note: string }) {
  return <div className="metric"><strong>{value}</strong><b>{label}</b><span>{note}</span></div>
}

export function StatCard({ label, value, note, accent }: { label: string; value: string; note: string; accent: string }) {
  return <div className="panel stat-card"><span className={`stat-dot ${accent}`} /> <span className="stat-label">{label}</span><strong>{value}</strong><small>{note}</small></div>
}

export function StatusBadge({ status }: { status: string }) {
  return <span className={`status-badge ${status.toLowerCase()}`}><i />{status}</span>
}

export function Fact({ label, value }: { label: string; value: string }) {
  return <div className="fact"><span>{label}</span><strong>{value}</strong></div>
}

export function MiniMetric({ label, value, note }: { label: string; value: string; note: string }) {
  return <div className="mini-metric"><span>{label}</span><strong>{value}</strong>{note && <small>{note}</small>}</div>
}
