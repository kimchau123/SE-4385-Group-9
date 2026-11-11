import type { Page, Skill } from '../data'
import { Icon, PageIntro, StatCard, StatusBadge } from '../components/ui'

export function CatalogPage({ skills, allSkills, selectedSkill, setSelectedSkill, query, setQuery, category, setCategory, statusFilter, setStatusFilter, navigate }: {
  skills: Skill[]; allSkills: Skill[]; selectedSkill: string; setSelectedSkill: (name: string) => void; query: string; setQuery: (value: string) => void; category: string; setCategory: (value: string) => void; statusFilter: string; setStatusFilter: (value: string) => void; navigate: (page: Page) => void
}) {
  const selected = allSkills.find((skill) => skill.name === selectedSkill) ?? allSkills[0]
  const categories = ['All categories', 'Software Engineering', 'Data & Analytics', 'Cloud & DevOps', 'Professional Skills']
  return <>
    <PageIntro title="Skills Catalog" description="Standard skill definitions, types and proficiency levels used across the Skills Inventory.">
      <button className="button button-secondary" type="button">Manage Skill Types</button><button className="button button-primary" type="button" onClick={() => navigate('Add Skill')}><span>＋</span> Add Skill</button>
    </PageIntro>
    <div className="stats-grid four">
      <StatCard label="Total skills" value={String(allSkills.length + 26)} note="Across 4 categories" accent="blue" />
      <StatCard label="Active" value={String(allSkills.filter((skill) => skill.status === 'Active').length + 19)} note="Available to your organization" accent="blue" />
      <StatCard label="Pending" value={String(allSkills.filter((skill) => skill.status === 'Pending').length + 5)} note="Awaiting review" accent="amber" />
      <StatCard label="Deprecated" value="3" note="No longer in use" accent="slate" />
    </div>
    <div className="catalog-layout">
      <aside className="panel category-panel"><div className="eyebrow">CATEGORIES</div>
        {categories.map((name, index) => <button key={name} type="button" className={`category-option ${category === name ? 'selected' : ''}`} onClick={() => setCategory(name)}><span>{name}</span><b>{[allSkills.length + 26, 18, 7, 5, 4][index]}</b></button>)}
        <p className="aside-note">ⓘ Organize skills by category to make them easier to discover.</p>
      </aside>
      <section className="panel catalog-table-panel">
        <div className="table-heading"><h2>All skills <span className="count-pill">{skills.length}</span></h2><button className="text-button" type="button">⇩ Export</button></div>
        <div className="catalog-controls"><label className="search-field"><Icon name="search" size={15} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search for skill" aria-label="Search for skill" /></label>
          <select value={category} onChange={(event) => setCategory(event.target.value)} aria-label="Filter by category">{categories.map((item) => <option key={item}>{item}</option>)}</select></div>
        <div className="filter-tabs">{['All', 'Active', 'Pending', 'Deprecated'].map((status) => <button key={status} className={statusFilter === status ? 'chosen' : ''} type="button" onClick={() => setStatusFilter(status)}>{status}<span>{status === 'All' ? allSkills.length + 26 : status === 'Deprecated' ? 3 : allSkills.filter((skill) => skill.status === status).length + (status === 'Active' ? 19 : 5)}</span></button>)}</div>
        <div className="table-scroll"><table><thead><tr><th>Skill</th><th>Type</th><th>Status</th><th>Resources</th><th></th></tr></thead><tbody>
          {skills.map((skill) => <tr key={skill.name} className={selectedSkill === skill.name ? 'row-selected' : ''} onClick={() => setSelectedSkill(skill.name)}><td><strong>{skill.name}</strong><small>{skill.category}</small></td><td>{skill.type}</td><td><StatusBadge status={skill.status} /></td><td>{skill.resources}</td><td><button className="row-action" type="button" aria-label={`Edit ${skill.name}`} onClick={(event) => { event.stopPropagation(); setSelectedSkill(skill.name) }}>↗</button></td></tr>)}
          {skills.length === 0 && <tr><td className="empty-row" colSpan={5}>No skills match your filters.</td></tr>}
        </tbody></table></div>
        <div className="table-footer"><span>Showing {skills.length ? 1 : 0}–{skills.length} of {skills.length} skills</span><div className="pagination"><button type="button" aria-label="Previous page">‹</button><button className="page-current" type="button">1</button><button type="button">2</button><button type="button">3</button><button type="button">4</button><button type="button">5</button><button type="button" aria-label="Next page">›</button></div></div>
      </section>
      {selected && <aside className="panel skill-detail"><h2>{selected.name}</h2><p>{selected.description}</p><div className="detail-tags"><StatusBadge status={selected.status} /><span className="tag">{selected.type}</span><span className="tag">{selected.category}</span></div><div className="resource-count"><span>Resources</span><strong>{selected.resources}</strong></div><div className="eyebrow proficiency-title">PROFICIENCY LEVELS</div>
        {['Beginner', 'Intermediate', 'Expert'].map((level, index) => <div className="level-card" key={level}><strong><span className="level-dots">{'●'.repeat(index + 1)}</span> {level}</strong><span>{[12, 34, 9][index]}</span><p>The quick brown fox jumps over the lazy dog.</p><div className="progress-track"><span style={{ width: `${[34, 78, 42][index]}%` }} /></div></div>)}
        <div className="detail-actions"><button className="button button-primary" type="button">✎ Edit Skill</button><button className="button button-secondary" type="button">Manage Skill Types</button></div>
      </aside>}
    </div>
  </>
}
