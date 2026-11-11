import type { Page } from '../data'
import { employees } from '../data'
import { Icon, PageIntro } from '../components/ui'

export function OrganizationPage({ employeeQuery, setEmployeeQuery, filteredEmployees, selectedOrg, setSelectedOrg, selectedEmployee, setSelectedEmployee, navigate }: {
  employeeQuery: string; setEmployeeQuery: (value: string) => void; filteredEmployees: typeof employees; selectedOrg: string; setSelectedOrg: (value: string) => void; selectedEmployee: string; setSelectedEmployee: (value: string) => void; navigate: (page: Page) => void
}) {
  const teams = ['Revenue Strategy', 'Sales Enablement', 'Pricing & Yield']
  const people = employees.filter((employee) => employee.team === selectedOrg)
  return <>
    <PageIntro title="Organization" />
    <div className="organization-layout">
      <section className="panel hierarchy-panel"><h2>Organization hierarchy</h2><button className="hierarchy-item" type="button"><strong>Sample company</strong><small>Root organization</small><span>Company</span></button>
        {['Commercial Operations', ...teams].map((item, index) => <button key={item} type="button" className={`hierarchy-item ${selectedOrg === item ? 'selected' : ''}`} onClick={() => setSelectedOrg(item)}><strong>{item}</strong><small>{index === 0 ? 'Parent: Sample company' : 'Parent department: Commercial Operations'}</small><span>{index === 0 ? 'Department' : 'Team'}</span></button>)}
        <div className="reporting-chain"><strong>Reporting chain · Sample</strong><p>Avery Morgan (CEO) → Maya Patel (Revenue Manager) → Jordan Davis (Senior Analyst)</p></div>
      </section>
      <section className="panel org-resources"><label className="search-field"><Icon name="search" size={15} /><input value={employeeQuery} onChange={(event) => setEmployeeQuery(event.target.value)} placeholder="Search organization, department, team, or employee" aria-label="Search organization" /></label>
        <div className="section-heading compact"><h2>Resources and skills</h2><span className="count-pill">{(people.length || filteredEmployees.length)} resources</span></div>
        {(people.length ? people : filteredEmployees.slice(0, 4)).map((employee) => <button className={`org-person ${selectedEmployee === employee.name ? 'selected' : ''}`} type="button" key={employee.name} onClick={() => setSelectedEmployee(employee.name)}><span className="avatar">{employee.initials}</span><span><strong>{employee.name}</strong><small>{employee.title} · {employee.email}</small></span><span className="count-pill">{selectedEmployee === employee.name ? 'Selected' : `${employee.count} skills`}</span></button>)}
        <div className="selected-person-skills"><div><strong>{selectedEmployee}</strong><span className="count-pill">3 skills</span></div><div className="org-skill-row"><span>Revenue forecasting</span><span className="level-badge advanced">Advanced</span><span>Approved</span></div><div className="org-skill-row"><span>Market segmentation</span><span className="level-badge intermediate">Intermediate</span><span>Approved</span></div><div className="org-skill-row"><span>Pricing analytics</span><span className="level-badge expert">Expert</span><span>Approved</span></div><button type="button" className="text-button" onClick={() => navigate('Skills Inventory')}>View in Skills Inventory <Icon name="arrow" size={14} /></button></div>
      </section>
      <aside className="panel distribution-panel"><div className="panel-heading"><h2>Department skills distribution</h2><span className="count-pill">Top 5 skills</span></div><p>Example · Commercial Operations</p>
        {['Revenue forecasting', 'Pricing analytics', 'Market segmentation', 'Sales enablement', 'Yield management'].map((skill, index) => <div className="distribution-item" key={skill}><div><span>{skill}</span><small>{[8, 6, 5, 4, 3][index]} employees</small></div><div className="progress-track"><span style={{ width: `${100 - index * 14}%` }} /></div></div>)}
        <div className="distribution-note"><strong>Distribution summary</strong><p>Employees with each skill in Commercial Operations. Bars show the top five skills, not a total of distinct skills.</p></div>
        <button className="report-link" type="button" onClick={() => navigate('Data Reports')}><Icon name="arrow" size={15} /> Open Data Reports for deeper analytics</button>
      </aside>
    </div>
  </>
}
