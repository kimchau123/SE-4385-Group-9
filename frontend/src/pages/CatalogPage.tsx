import { useEffect, useState } from 'react'
import type { Page } from '../data'
import { catalogApi, isMockApi } from '../api/catalog'
import {
  SKILL_TYPES,
  STATUS_LABELS,
  TYPE_LABELS,
  describeError,
  type ApprovalStatus,
  type CatalogSkill,
  type ProficiencyLevel,
  type SkillResource,
  type SkillType,
} from '../api/catalogTypes'
import { Icon, PageIntro, StatCard, StatusBadge } from '../components/ui'
import { EditSkillDialog } from '../components/EditSkillDialog'
import { ProficiencyLevelsDialog } from '../components/ProficiencyLevelsDialog'
import { downloadSkillsCsv } from '../utils/exportCsv'

const PAGE_SIZE = 8
const STATUS_TABS: ('ALL' | ApprovalStatus)[] = ['ALL', 'APPROVED', 'REQUESTED', 'CANCELED']

type LoadResult = { key: string; all: CatalogSkill[]; visible: CatalogSkill[]; levels: ProficiencyLevel[]; error?: string }
type ResourceResult = { skillId: number; items: SkillResource[] | null }

const makeKey = (query: string, type: string, status: string, reload: number) => `${query}|${type}|${status}|${reload}`

function pageNumbers(current: number, count: number): number[] {
  const start = Math.max(1, Math.min(current - 2, count - 4))
  const end = Math.min(count, start + 4)
  return Array.from({ length: end - start + 1 }, (_, index) => start + index)
}

export function CatalogPage({ navigate, notify }: { navigate: (page: Page) => void; notify: (message: string) => void }) {
  const [query, setQuery] = useState('')
  const [debouncedQuery, setDebouncedQuery] = useState('')
  const [type, setType] = useState<SkillType | 'ALL'>('ALL')
  const [status, setStatus] = useState<ApprovalStatus | 'ALL'>('ALL')
  const [page, setPage] = useState(1)
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [reload, setReload] = useState(0)
  const [result, setResult] = useState<LoadResult | null>(null)
  const [resources, setResources] = useState<ResourceResult | null>(null)
  const [editing, setEditing] = useState<CatalogSkill | null>(null)
  const [showLevels, setShowLevels] = useState(false)
  const [busy, setBusy] = useState(false)

  const requestKey = makeKey(debouncedQuery, type, status, reload)

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedQuery(query.trim()), 250)
    return () => clearTimeout(timer)
  }, [query])

  // The full list feeds the counts; the filtered list (server-side: type, status, name search) feeds the table.
  useEffect(() => {
    let active = true
    const key = makeKey(debouncedQuery, type, status, reload)
    const filtered = debouncedQuery !== '' || type !== 'ALL' || status !== 'ALL'
    Promise.all([
      catalogApi.listSkills({ query: '', type: 'ALL', status: 'ALL' }),
      filtered ? catalogApi.listSkills({ query: debouncedQuery, type, status }) : null,
      catalogApi.listProficiencyLevels(),
    ])
      .then(([all, visible, levels]) => {
        if (active) setResult({ key, all, visible: visible ?? all, levels })
      })
      .catch((error: unknown) => {
        if (active) setResult({ key, all: [], visible: [], levels: [], error: describeError(error) })
      })
    return () => {
      active = false
    }
  }, [debouncedQuery, type, status, reload])

  const all = result?.all ?? []
  const visible = [...(result?.visible ?? [])].sort((a, b) => b.skillId - a.skillId)
  const levels = result?.levels ?? []
  const loading = result?.key !== requestKey
  const selected = all.find((skill) => skill.skillId === selectedId) ?? visible[0]
  const selectedSkillId = selected?.skillId

  useEffect(() => {
    if (selectedSkillId === undefined) return
    let active = true
    catalogApi.listSkillResources(selectedSkillId)
      .then((items) => {
        if (active) setResources({ skillId: selectedSkillId, items })
      })
      .catch(() => {
        if (active) setResources({ skillId: selectedSkillId, items: null })
      })
    return () => {
      active = false
    }
  }, [selectedSkillId, reload])

  const resourceItems = resources?.skillId === selectedSkillId ? resources?.items : undefined
  const activeResources = resourceItems?.filter((item) => item.status !== 'CANCELED')
  const countAtLevel = (levelId: number) => activeResources?.filter((item) => item.skillProficiencyLevelId === levelId).length ?? 0
  const resourceTotal = activeResources?.length ?? 0

  const countStatus = (value: ApprovalStatus) => all.filter((skill) => skill.status === value).length
  const countType = (value: SkillType) => all.filter((skill) => skill.skillType === value).length

  const pageCount = Math.max(1, Math.ceil(visible.length / PAGE_SIZE))
  const currentPage = Math.min(page, pageCount)
  const rows = visible.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE)
  const firstShown = visible.length === 0 ? 0 : (currentPage - 1) * PAGE_SIZE + 1
  const lastShown = Math.min(currentPage * PAGE_SIZE, visible.length)

  const chooseType = (value: SkillType | 'ALL') => {
    setType(value)
    setPage(1)
  }
  const chooseStatus = (value: ApprovalStatus | 'ALL') => {
    setStatus(value)
    setPage(1)
  }

  const review = async (skill: CatalogSkill, decision: 'approve' | 'deny') => {
    setBusy(true)
    try {
      if (decision === 'approve') await catalogApi.approveSkill(skill.skillId)
      else await catalogApi.denySkill(skill.skillId)
      notify(`${skill.skillName} ${decision === 'approve' ? 'approved' : 'denied'}.`)
      setReload((current) => current + 1)
    } catch (error) {
      notify(describeError(error))
    } finally {
      setBusy(false)
    }
  }

  return <>
    <PageIntro title="Skills Catalog" description="Standard skill definitions, types and proficiency levels used across the Skills Inventory.">
      <button className="button button-secondary" type="button" onClick={() => setShowLevels(true)}>Proficiency Levels</button>
      <button className="button button-primary" type="button" onClick={() => navigate('Add Skill')}><span>＋</span> Add Skill</button>
    </PageIntro>
    {result?.error && <div className="panel catalog-error" role="alert"><span>{result.error}</span><button className="button small" type="button" onClick={() => setReload((current) => current + 1)}>Try again</button></div>}
    <div className="stats-grid four">
      <StatCard label="Total skills" value={String(all.length)} note={`Across ${SKILL_TYPES.length} skill types`} accent="blue" />
      <StatCard label="Approved" value={String(countStatus('APPROVED'))} note="Available in the Skills Inventory" accent="blue" />
      <StatCard label="Requested" value={String(countStatus('REQUESTED'))} note="Awaiting review" accent="amber" />
      <StatCard label="Canceled" value={String(countStatus('CANCELED'))} note="Denied or withdrawn" accent="slate" />
    </div>
    <div className="catalog-layout">
      <aside className="panel category-panel"><div className="eyebrow">SKILL TYPES</div>
        <button type="button" className={`category-option ${type === 'ALL' ? 'selected' : ''}`} onClick={() => chooseType('ALL')}><span>All types</span><b>{all.length}</b></button>
        {SKILL_TYPES.map((name) => <button key={name} type="button" className={`category-option ${type === name ? 'selected' : ''}`} onClick={() => chooseType(name)}><span>{TYPE_LABELS[name]}</span><b>{countType(name)}</b></button>)}
        <p className="aside-note">ⓘ A skill's type groups it for search and filtering. Set it when adding or editing a skill.</p>
      </aside>
      <section className="panel catalog-table-panel" aria-busy={loading}>
        <div className="table-heading"><h2>All skills <span className="count-pill">{visible.length}</span> {isMockApi && <span className="subtle-tag">Demo data</span>}</h2><button className="text-button" type="button" disabled={visible.length === 0} onClick={() => downloadSkillsCsv(visible)}>⇩ Export</button></div>
        <div className="catalog-controls"><label className="search-field"><Icon name="search" size={15} /><input value={query} onChange={(event) => { setQuery(event.target.value); setPage(1) }} placeholder="Search for skill" aria-label="Search for skill" /></label>
          <select value={type} onChange={(event) => chooseType(event.target.value as SkillType | 'ALL')} aria-label="Filter by skill type"><option value="ALL">All types</option>{SKILL_TYPES.map((item) => <option key={item} value={item}>{TYPE_LABELS[item]}</option>)}</select></div>
        <div className="filter-tabs">{STATUS_TABS.map((tab) => <button key={tab} className={status === tab ? 'chosen' : ''} type="button" onClick={() => chooseStatus(tab)}>{tab === 'ALL' ? 'All' : STATUS_LABELS[tab]}<span>{tab === 'ALL' ? all.length : countStatus(tab)}</span></button>)}</div>
        <div className="table-scroll"><table><thead><tr><th>Skill</th><th>Type</th><th>Status</th><th></th></tr></thead><tbody>
          {rows.map((skill) => <tr key={skill.skillId} tabIndex={0} className={selected?.skillId === skill.skillId ? 'row-selected' : ''} onClick={() => setSelectedId(skill.skillId)} onKeyDown={(event) => { if (event.key === 'Enter') setSelectedId(skill.skillId) }}><td><strong>{skill.skillName}</strong></td><td>{TYPE_LABELS[skill.skillType]}</td><td><StatusBadge status={STATUS_LABELS[skill.status]} /></td><td><button className="row-action" type="button" aria-label={`Edit ${skill.skillName}`} onClick={(event) => { event.stopPropagation(); setSelectedId(skill.skillId); setEditing(skill) }}>↗</button></td></tr>)}
          {rows.length === 0 && <tr><td className="empty-row" colSpan={4}>{loading ? 'Loading skills…' : 'No skills match your filters.'}</td></tr>}
        </tbody></table></div>
        <div className="table-footer"><span>Showing {firstShown}–{lastShown} of {visible.length} skills</span><div className="pagination"><button type="button" aria-label="Previous page" disabled={currentPage === 1} onClick={() => setPage(currentPage - 1)}>‹</button>{pageNumbers(currentPage, pageCount).map((number) => <button key={number} className={number === currentPage ? 'page-current' : ''} type="button" aria-label={`Page ${number}`} aria-current={number === currentPage ? 'page' : undefined} onClick={() => setPage(number)}>{number}</button>)}<button type="button" aria-label="Next page" disabled={currentPage === pageCount} onClick={() => setPage(currentPage + 1)}>›</button></div></div>
      </section>
      {selected && <aside className="panel skill-detail"><h2>{selected.skillName}</h2><p>{selected.skillDescription ?? 'No description provided.'}</p><div className="detail-tags"><StatusBadge status={STATUS_LABELS[selected.status]} /><span className="tag">{TYPE_LABELS[selected.skillType]}</span></div><div className="resource-count"><span>Resources</span><strong>{activeResources ? resourceTotal : resourceItems === null ? '—' : '…'}</strong></div><div className="eyebrow proficiency-title">PROFICIENCY LEVELS</div>
        {levels.map((level, index) => <div className="level-card" key={level.skillProficiencyLevelId}><strong><span className="level-dots">{'●'.repeat(Math.min(index + 1, 5))}</span> {level.skillProficiencyLevelName}</strong><span>{activeResources ? countAtLevel(level.skillProficiencyLevelId) : '—'}</span><div className="progress-track"><span style={{ width: `${resourceTotal ? (countAtLevel(level.skillProficiencyLevelId) / resourceTotal) * 100 : 0}%` }} /></div></div>)}
        <div className="detail-actions"><button className="button button-primary" type="button" onClick={() => setEditing(selected)}>✎ Edit Skill</button>
          {selected.status === 'REQUESTED' && <><button className="button button-secondary" type="button" disabled={busy} onClick={() => review(selected, 'approve')}>Approve</button><button className="button button-danger" type="button" disabled={busy} onClick={() => review(selected, 'deny')}>Deny</button></>}
        </div>
      </aside>}
    </div>
    {editing && <EditSkillDialog skill={editing} onClose={() => setEditing(null)} onSaved={(saved) => { setEditing(null); notify(`${saved.skillName} updated.`); setReload((current) => current + 1) }} />}
    {showLevels && <ProficiencyLevelsDialog levels={levels} onClose={() => setShowLevels(false)} onChanged={(message) => { notify(message); setReload((current) => current + 1) }} />}
  </>
}
