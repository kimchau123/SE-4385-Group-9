import { useState } from 'react'
import { catalogApi } from '../api/catalog'
import { LEVEL_NAME_MAX, describeError, validateLevelName, type ProficiencyLevel } from '../api/catalogTypes'
import { Dialog } from './Dialog'

/** Maintain Skill Proficiency Levels (Workforce Planner, TC-WP-05): rename levels and add new ones. */
export function ProficiencyLevelsDialog({ levels, onClose, onChanged }: {
  levels: ProficiencyLevel[]
  onClose: () => void
  onChanged: (message: string) => void
}) {
  const [drafts, setDrafts] = useState<Record<number, string>>({})
  const [newName, setNewName] = useState('')
  const [busyId, setBusyId] = useState<number | 'new' | null>(null)
  const [error, setError] = useState<{ id: number | 'new'; message: string } | null>(null)

  const valueFor = (level: ProficiencyLevel) => drafts[level.skillProficiencyLevelId] ?? level.skillProficiencyLevelName

  const save = async (level: ProficiencyLevel) => {
    const id = level.skillProficiencyLevelId
    const name = valueFor(level)
    const problem = validateLevelName(name)
    if (problem) {
      setError({ id, message: problem })
      return
    }
    setBusyId(id)
    setError(null)
    try {
      await catalogApi.updateProficiencyLevel(id, name)
      setDrafts((current) => {
        const next = { ...current }
        delete next[id]
        return next
      })
      onChanged(`Level ${id} renamed to ${name.trim()}.`)
    } catch (caught) {
      setError({ id, message: describeError(caught) })
    } finally {
      setBusyId(null)
    }
  }

  const add = async () => {
    const problem = validateLevelName(newName)
    if (problem) {
      setError({ id: 'new', message: problem })
      return
    }
    setBusyId('new')
    setError(null)
    try {
      await catalogApi.createProficiencyLevel(newName)
      setNewName('')
      onChanged(`Proficiency level ${newName.trim()} added.`)
    } catch (caught) {
      setError({ id: 'new', message: describeError(caught) })
    } finally {
      setBusyId(null)
    }
  }

  return (
    <Dialog title="Proficiency Levels" onClose={onClose}>
      <p className="dialog-note">Levels are shared by every skill. Level 1 is the lowest proficiency.</p>
      <ul className="level-list">
        {levels.map((level) => {
          const id = level.skillProficiencyLevelId
          const changed = valueFor(level).trim() !== level.skillProficiencyLevelName
          return (
            <li key={id}>
              <div className="level-row">
                <span className="level-number">{id}</span>
                <input value={valueFor(level)} onChange={(event) => setDrafts((current) => ({ ...current, [id]: event.target.value }))}
                  aria-label={`Name of level ${id}`} maxLength={LEVEL_NAME_MAX + 10} />
                <button type="button" className="button button-primary small" disabled={!changed || busyId !== null} onClick={() => save(level)}>
                  {busyId === id ? 'Saving…' : 'Save'}
                </button>
              </div>
              {error?.id === id && <p className="field-error" role="alert">{error.message}</p>}
            </li>
          )
        })}
        {levels.length === 0 && <li className="empty-row">No proficiency levels yet.</li>}
      </ul>
      <div className="level-add">
        <div className="level-row">
          <span className="level-number">＋</span>
          <input value={newName} onChange={(event) => setNewName(event.target.value)} placeholder="New level name" aria-label="New level name" />
          <button type="button" className="button button-secondary small" disabled={busyId !== null} onClick={add}>{busyId === 'new' ? 'Adding…' : 'Add level'}</button>
        </div>
        {error?.id === 'new' && <p className="field-error" role="alert">{error.message}</p>}
      </div>
      <div className="form-actions"><button className="button button-primary" type="button" onClick={onClose}>Done</button></div>
    </Dialog>
  )
}
