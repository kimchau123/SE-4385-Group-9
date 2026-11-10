import type { FormEvent } from 'react'

export function AddSkillPage({ onSubmit, onCancel }: { onSubmit: (event: FormEvent<HTMLFormElement>) => void; onCancel: () => void }) {
  return <div className="add-skill-wrap"><form className="panel add-skill-form" onSubmit={onSubmit}>
    <h1>Add New Skill</h1><p>Create a new entry in the Skills Catalog</p>
    <label>Skill Name<input required name="skillName" placeholder="e.g., Python" /></label>
    <label>Skill Type<select required name="skillType" defaultValue=""><option value="" disabled>Select a skill type</option><option>Technical</option><option>Behavioral</option><option>Language</option><option>Leadership</option></select></label>
    <label>Category<select required name="category" defaultValue="Software Engineering"><option>Software Engineering</option><option>Data & Analytics</option><option>Cloud & DevOps</option><option>Professional Skills</option></select></label>
    <label>Description<textarea name="description" rows={3} placeholder="Describe the skill" /></label>
    <fieldset><legend>Proficiency Level</legend>{['Beginner', 'Intermediate', 'Advanced', 'Expert'].map((level, index) => <label className="radio-option" key={level}><input type="radio" name="proficiency" value={level} defaultChecked={index === 0} />{level}</label>)}</fieldset>
    <div className="form-actions"><button className="button button-secondary" type="button" onClick={onCancel}>Cancel</button><button className="button button-primary" type="submit">Submit</button></div>
  </form></div>
}
