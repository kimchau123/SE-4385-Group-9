import { catalogApi } from '../api/catalog'
import type { CatalogSkill } from '../api/catalogTypes'
import { Dialog } from './Dialog'
import { SkillForm } from './SkillForm'

export function EditSkillDialog({ skill, onClose, onSaved }: { skill: CatalogSkill; onClose: () => void; onSaved: (skill: CatalogSkill) => void }) {
  return (
    <Dialog title={`Edit ${skill.skillName}`} onClose={onClose}>
      <SkillForm
        initial={{ skillName: skill.skillName, skillDescription: skill.skillDescription ?? '', skillType: skill.skillType }}
        submitLabel="Save changes"
        onCancel={onClose}
        onSubmit={async (input) => {
          onSaved(await catalogApi.updateSkill(skill.skillId, input))
        }}
      />
    </Dialog>
  )
}
