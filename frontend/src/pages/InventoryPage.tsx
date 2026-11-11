import { assignments, employees } from '../data'
import { Fact, Icon, PageIntro, StatusBadge } from '../components/ui'

export function InventoryPage({ employeeQuery, setEmployeeQuery, filteredEmployees, selectedEmployee, setSelectedEmployee }: {
  employeeQuery: string; setEmployeeQuery: (value: string) => void; filteredEmployees: typeof employees; selectedEmployee: string; setSelectedEmployee: (value: string) => void
}) {
  const person = employees.find((employee) => employee.name === selectedEmployee) ?? employees[0]
  return <>
    <PageIntro title="Skills Inventory" description="Assign, review, and manage employee skills against approved catalog definitions. Illustrative data." />
    <div className="inventory-layout">
      <section className="panel resource-search"><h2>Search resource</h2><p>Find an employee by name, email, or employee ID.</p>
        <label className="search-field"><Icon name="search" size={15} /><input value={employeeQuery} onChange={(event) => setEmployeeQuery(event.target.value)} placeholder="Search employee" aria-label="Search employee" /></label>
        <select aria-label="Filter by department"><option>All departments</option>{['Engineering', 'Product', 'Operations', 'Commercial Operations'].map((item) => <option key={item}>{item}</option>)}</select>
        <select aria-label="Filter by team"><option>All teams</option>{['Platform', 'Growth', 'Support', 'Revenue Strategy'].map((item) => <option key={item}>{item}</option>)}</select>
        <div className="eyebrow matching-label">MATCHING RESOURCES</div>
        <div className="employee-list">{filteredEmployees.map((employee) => <button className={`employee-card ${person.name === employee.name ? 'selected' : ''}`} key={employee.name} type="button" onClick={() => setSelectedEmployee(employee.name)}><div className="employee-title"><span className="avatar">{employee.initials}</span><span><strong>{employee.name}</strong><small>{employee.email}</small></span></div><div className="employee-meta"><span>Department <b>{employee.department}</b></span><span>Team <b>{employee.team}</b></span><span>Skills <b>{employee.count}</b></span></div></button>)}
          {filteredEmployees.length === 0 && <p className="empty-state">No employees found.</p>}</div>
      </section>
      <section className="panel inventory-detail">
        <div className="inventory-person-heading"><div><h2>{person.name}</h2><p>Employee ID EMP-1042 (Resource Manager view)</p></div><StatusBadge status="Selected" /><span className="count-pill">{person.count} assigned skills</span></div>
        <div className="person-facts"><Fact label="Department" value={person.department} /><Fact label="Team" value={person.team} /><Fact label="Email" value={person.email} /><Fact label="Job title" value={person.title} /></div>
        <div className="inventory-callouts"><div><strong>View mode</strong><div className="toggle-pills"><button className="chosen" type="button">Resource-focused</button><button type="button">Skill-focused</button></div><small>Shows skills assigned to {person.name}.</small></div><div><strong>Skill-focused example</strong><small>Search a catalog skill to see employees with that skill, proficiency, and assignment status.</small><span className="tag">Example: Cloud Security</span></div></div>
        <div className="section-heading compact"><h2>Assigned skills</h2><div><button className="button button-primary small" type="button">Assign skill</button><span className="count-pill">4 total</span></div></div>
        <div className="table-scroll"><table className="inventory-table"><thead><tr><th>Skill name</th><th>Proficiency</th><th>Approval</th><th>Last updated</th><th>Actions</th></tr></thead><tbody>{assignments.map((item) => <tr key={item.skill}><td><strong>{item.skill}</strong><small>{item.category}</small></td><td><span className={`level-badge ${item.level.toLowerCase()}`}>{item.level}</span></td><td><span className={`approval-badge ${item.approval.toLowerCase()}`}>{item.approval}</span></td><td>{item.date}</td><td><button className="row-action" aria-label={`Edit ${item.skill}`} type="button">✎</button><button className="row-action" aria-label={`Remove ${item.skill}`} type="button">⌫</button></td></tr>)}</tbody></table></div>
        <p className="footnote">Assignments reference approved catalog skills only. Removing an assignment does not delete the master skill.</p>
        <div className="section-heading compact certifications-title"><h2>Certifications</h2><button className="button button-secondary small" type="button">Maintain details</button><span className="count-pill">3 total</span></div>
        <div className="table-scroll"><table className="inventory-table certification-table"><thead><tr><th>Certification</th><th>Issuer</th><th>Issue</th><th>Expiry</th><th>Verification</th></tr></thead><tbody>{[['AWS Certified Security', 'AWS', '14 Oct 2024', '14 Oct 2027', 'Verified'], ['Certified Scrum Master', 'Scrum Alliance', '18 Mar 2025', '18 Mar 2027', 'Unverified'], ['ITIL Foundation', 'PeopleCert', '07 Jan 2024', '07 Jan 2026', 'Expired']].map(([name, issuer, issue, expiry, verification]) => <tr key={name}><td><strong>{name}</strong><small>{issuer}</small></td><td>{issuer}</td><td>{issue}</td><td>{expiry}</td><td><span className={`approval-badge ${verification.toLowerCase()}`}>{verification}</span></td></tr>)}</tbody></table></div>
      </section>
    </div>
  </>
}
