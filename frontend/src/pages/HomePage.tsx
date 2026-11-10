import type { Page } from '../data'
import { Icon, Metric, PageIntro } from '../components/ui'

export function HomePage({ navigate }: { navigate: (page: Page) => void }) {
  const cards: { title: Page; desc: string; note: string; icon: string }[] = [
    { title: 'Skills Catalog', desc: 'Explore the master definitions that give skills a shared meaning.', note: 'Skill definitions, categories and types', icon: 'catalog' },
    { title: 'Skills Inventory', desc: 'See how catalog skills are assigned to employees across the workforce.', note: 'Proficiency, approval status and certifications', icon: 'layers' },
    { title: 'Organization', desc: 'Understand the people and reporting relationships behind your workforce.', note: 'Departments, teams and employee profiles', icon: 'organization' },
    { title: 'Data Reports', desc: 'Review workforce insight drawn from approved skill assignments.', note: 'Summaries, distributions, coverage and history', icon: 'chart' },
  ]
  const roles = [
    ['Workforce Planner', 'Maintain the master catalog and submit skill additions or updates for administrative approval.', 'Skills Catalog'],
    ['Resource Manager', 'Find people, maintain employee–skill associations, and track certifications and their verification.', 'Skills Inventory · Organization'],
    ['Organizational Leader', 'View people and skills, review inventory summaries, and understand workforce coverage and history.', 'Organization · Data Reports'],
    ['Business Operations / Admin', 'Manage data imports and exports, backup and restore, and review additional-skill submissions.', 'Data operations and submission review'],
  ]
  return <>
    <PageIntro title="Home" description="Welcome to Skills Hub. Explore skill definitions, connect skills to people, and understand your workforce." />
    <section className="section-block">
      <div className="section-heading"><h2>Explore your workspace</h2><p>Choose an area to get started. Available pages and actions depend on your role and permissions.</p></div>
      <div className="workspace-cards">
        {cards.map(({ title, desc, note, icon }) => <button key={title} className="workspace-card" type="button" onClick={() => navigate(title)}>
          <div className="card-title"><span className="card-icon"><Icon name={icon} /></span><strong>{title}</strong></div>
          <p>{desc}</p><small>{note}</small>
          <span className="card-link">Open {title}<Icon name="arrow" size={15} /></span>
        </button>)}
      </div>
    </section>
    <div className="connection-strip"><strong>How it connects</strong><span>Catalog defines skills</span><Icon name="arrow" size={14} /><span>Inventory links skills to people in your organization</span><Icon name="arrow" size={14} /><span>Reports turn approved assignments into insight</span></div>
    <section className="panel glance-panel">
      <div className="panel-heading"><h2>Inventory at a glance</h2><span className="subtle-tag">Illustrative data</span><small>Q3 2026 · quarter ended 30 Sep</small></div>
      <div className="metric-row">
        <Metric value="128" label="Employees represented" note="Distinct employees with approved assignments" />
        <Metric value="42" label="Skills represented" note="Distinct skills in approved assignments" />
        <Metric value="184" label="Approved assignments" note="Approved employee–skill pairs" />
        <Metric value="4" label="Departments represented" note="Departments with approved assignments" />
      </div>
      <p className="footnote">Reporting scope only — these figures are not total organization headcount or the total master skills catalog.</p>
    </section>
    <section className="panel role-panel"><div className="panel-heading"><h2>Find your workspace</h2><p>A guide to how each role uses Skills Hub. Your assigned permissions determine what you can view or maintain.</p></div>
      <div className="role-grid">{roles.map(([role, desc, links], index) => <div key={role} className="role-card"><strong>{role}</strong><small>Role guide</small>{index === 2 && <span className="current-role">Your current role</span>}<p>{desc}</p><span>{links}</span></div>)}</div>
    </section>
  </>
}
