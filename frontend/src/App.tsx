import { useMemo, useState, type FormEvent } from 'react'
import { employees, initialSkills, navItems, type Page, type Skill } from './data'
import { Icon } from './components/ui'
import { AddSkillPage } from './pages/AddSkillPage'
import { CatalogPage } from './pages/CatalogPage'
import { HomePage } from './pages/HomePage'
import { InventoryPage } from './pages/InventoryPage'
import { OrganizationPage } from './pages/OrganizationPage'
import { ReportsPage } from './pages/ReportsPage'
import './App.css'

function App() {
  const [page, setPage] = useState<Page>('Home')
  const [skills, setSkills] = useState(initialSkills)
  const [query, setQuery] = useState('')
  const [category, setCategory] = useState('All categories')
  const [statusFilter, setStatusFilter] = useState('All')
  const [selectedSkill, setSelectedSkill] = useState('React')
  const [employeeQuery, setEmployeeQuery] = useState('')
  const [selectedEmployee, setSelectedEmployee] = useState('Jordan Davis')
  const [selectedOrg, setSelectedOrg] = useState('Revenue Strategy')
  const [formMessage, setFormMessage] = useState('')

  const filteredSkills = useMemo(() => skills.filter((skill) => {
    const matchesText = `${skill.name} ${skill.category}`.toLowerCase().includes(query.toLowerCase())
    return matchesText && (category === 'All categories' || skill.category === category) && (statusFilter === 'All' || skill.status === statusFilter)
  }), [skills, query, category, statusFilter])

  const filteredEmployees = employees.filter((employee) =>
    `${employee.name} ${employee.email} ${employee.department} ${employee.team}`.toLowerCase().includes(employeeQuery.toLowerCase()),
  )

  const navigate = (nextPage: Page) => {
    setPage(nextPage)
    setQuery('')
    setFormMessage('')
  }

  const submitSkill = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const name = String(form.get('skillName')).trim()
    if (skills.some((skill) => skill.name.toLowerCase() === name.toLowerCase())) {
      setFormMessage('That skill is already in the catalog.')
      return
    }
    const newSkill: Skill = {
      name,
      category: String(form.get('category')),
      type: String(form.get('skillType')),
      status: 'Pending',
      resources: 0,
      description: String(form.get('description')).trim() || 'New skill awaiting review.',
    }
    setSkills((current) => [newSkill, ...current])
    setSelectedSkill(name)
    setFormMessage('Skill submitted for review.')
    setPage('Skills Catalog')
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <button className="brand" type="button" onClick={() => navigate('Home')} aria-label="Skills Hub home">
          <span className="brand-mark"><Icon name="cube" size={20} /></span>
          <span className="brand-copy"><strong>Skills Hub</strong><small>SABRE CORPORATION</small></span>
        </button>
        <div className="nav-label">WORKSPACE</div>
        <nav className="primary-nav" aria-label="Workspace">
          {navItems.map(({ page: item, icon }) => (
            <button key={item} type="button" className={`nav-item ${page === item || (page === 'Add Skill' && item === 'Skills Catalog') ? 'active' : ''}`} onClick={() => navigate(item)}>
              <Icon name={icon} /><span>{item}</span>
            </button>
          ))}
        </nav>
        <div className="workspace-profile">
          <div className="avatar avatar-dark">S</div>
          <span><strong>Sabre workspace</strong><small>Administrator</small></span>
        </div>
      </aside>

      <main className="main-shell">
        <header className="topbar">
          <div className="breadcrumbs"><span>Workspace</span><span className="crumb-chevron">›</span><strong>{page === 'Add Skill' ? 'Skills Catalog　›　Add Skill' : page}</strong></div>
          <div className="topbar-actions"><button type="button" className="icon-button" aria-label="Notifications"><Icon name="bell" /></button><button type="button" className="profile-button"><span className="avatar">JD</span><span className="profile-caret">⌄</span></button></div>
        </header>

        <div className="page-content">
          {page === 'Home' && <HomePage navigate={navigate} />}
          {page === 'Skills Catalog' && <CatalogPage skills={filteredSkills} allSkills={skills} selectedSkill={selectedSkill} setSelectedSkill={setSelectedSkill} query={query} setQuery={setQuery} category={category} setCategory={setCategory} statusFilter={statusFilter} setStatusFilter={setStatusFilter} navigate={navigate} />}
          {page === 'Add Skill' && <AddSkillPage onSubmit={submitSkill} onCancel={() => navigate('Skills Catalog')} />}
          {page === 'Skills Inventory' && <InventoryPage employeeQuery={employeeQuery} setEmployeeQuery={setEmployeeQuery} filteredEmployees={filteredEmployees} selectedEmployee={selectedEmployee} setSelectedEmployee={setSelectedEmployee} />}
          {page === 'Organization' && <OrganizationPage employeeQuery={employeeQuery} setEmployeeQuery={setEmployeeQuery} filteredEmployees={filteredEmployees} selectedOrg={selectedOrg} setSelectedOrg={setSelectedOrg} selectedEmployee={selectedEmployee} setSelectedEmployee={setSelectedEmployee} navigate={navigate} />}
          {page === 'Data Reports' && <ReportsPage />}
        </div>
      </main>
      {formMessage && <div className="toast" role="status">{formMessage}</div>}
    </div>
  )
}

export default App
