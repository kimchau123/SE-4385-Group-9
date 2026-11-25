import { catalogApi } from '../api/catalog'
import type { CatalogSkill } from '../api/catalogTypes'
import { SkillForm } from '../components/SkillForm'

export function AddSkillPage({ onCreated, onCancel }: { onCreated: (skill: CatalogSkill) => void; onCancel: () => void }) {
  return <div className="add-skill-wrap"><div className="panel add-skill-form">
    <h1>Add New Skill</h1><p>Create a new entry in the Skills Catalog. New skills are submitted for review before they become available.</p>
    <SkillForm
      initial={{ skillName: '', skillDescription: '', skillType: '' }}
      submitLabel="Submit"
      onCancel={onCancel}
      onSubmit={async (input) => {
        onCreated(await catalogApi.createSkill(input))
      }}
    />
  </div></div>
}
