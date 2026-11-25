import { useId, useState, type FormEvent } from 'react'
import {
  ApiError,
  DESCRIPTION_MAX,
  DESCRIPTION_MIN,
  NAME_MAX,
  SKILL_TYPES,
  TYPE_LABELS,
  describeError,
  validateSkillInput,
  type FieldErrors,
  type SkillInput,
  type SkillType,
} from '../api/catalogTypes'

export type SkillFormValues = { skillName: string; skillDescription: string; skillType: SkillType | '' }

/** Shared by Add Skill and Edit Skill. Validates 1-50 char names and 10-500 char descriptions (TC-WP-01 / TC-WP-02). */
export function SkillForm({ initial, submitLabel, onSubmit, onCancel }: {
  initial: SkillFormValues
  submitLabel: string
  onSubmit: (input: SkillInput) => Promise<void>
  onCancel: () => void
}) {
  const [name, setName] = useState(initial.skillName)
  const [description, setDescription] = useState(initial.skillDescription)
  const [type, setType] = useState<SkillType | ''>(initial.skillType)
  const [errors, setErrors] = useState<FieldErrors>({})
  const [submitError, setSubmitError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const id = useId()

  const nameLength = name.trim().length
  const descriptionLength = description.trim().length

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSubmitError('')
    const found = validateSkillInput({ skillName: name, skillDescription: description, skillType: type })
    if (Object.keys(found).length > 0 || type === '') {
      setErrors(found)
      return
    }
    setErrors({})
    setSubmitting(true)
    try {
      await onSubmit({ skillName: name.trim(), skillDescription: description.trim(), skillType: type })
    } catch (error) {
      if (error instanceof ApiError && error.status === 409) setErrors({ skillName: error.message })
      else setSubmitError(describeError(error))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="skill-form" onSubmit={handleSubmit} noValidate>
      <div className="field">
        <label htmlFor={`${id}-name`}>Skill Name</label>
        <input id={`${id}-name`} name="skillName" placeholder="e.g., Python" value={name} onChange={(event) => setName(event.target.value)}
          aria-invalid={Boolean(errors.skillName)} aria-describedby={`${id}-name-meta`} autoComplete="off" />
        <div className="field-meta" id={`${id}-name-meta`}>
          {errors.skillName ? <span className="field-error" role="alert">{errors.skillName}</span> : <span />}
          <span className={nameLength > NAME_MAX ? 'field-count over' : 'field-count'}>{nameLength}/{NAME_MAX}</span>
        </div>
      </div>

      <div className="field">
        <label htmlFor={`${id}-type`}>Skill Type</label>
        <select id={`${id}-type`} name="skillType" value={type} onChange={(event) => setType(event.target.value as SkillType | '')}
          aria-invalid={Boolean(errors.skillType)} aria-describedby={`${id}-type-meta`}>
          <option value="" disabled>Select a skill type</option>
          {SKILL_TYPES.map((item) => <option key={item} value={item}>{TYPE_LABELS[item]}</option>)}
        </select>
        <div className="field-meta" id={`${id}-type-meta`}>{errors.skillType && <span className="field-error" role="alert">{errors.skillType}</span>}</div>
      </div>

      <div className="field">
        <label htmlFor={`${id}-description`}>Description</label>
        <textarea id={`${id}-description`} name="skillDescription" rows={4} placeholder="Describe the skill"
          value={description} onChange={(event) => setDescription(event.target.value)}
          aria-invalid={Boolean(errors.skillDescription)} aria-describedby={`${id}-description-meta`} />
        <div className="field-meta" id={`${id}-description-meta`}>
          {errors.skillDescription ? <span className="field-error" role="alert">{errors.skillDescription}</span> : <span className="field-hint">{DESCRIPTION_MIN}–{DESCRIPTION_MAX} characters</span>}
          <span className={descriptionLength > DESCRIPTION_MAX ? 'field-count over' : 'field-count'}>{descriptionLength}/{DESCRIPTION_MAX}</span>
        </div>
      </div>

      {submitError && <p className="form-error" role="alert">{submitError}</p>}
      <div className="form-actions">
        <button className="button button-secondary" type="button" onClick={onCancel} disabled={submitting}>Cancel</button>
        <button className="button button-primary" type="submit" disabled={submitting}>{submitting ? 'Saving…' : submitLabel}</button>
      </div>
    </form>
  )
}
