import { STATUS_LABELS, TYPE_LABELS, type CatalogSkill } from '../api/catalogTypes'

// Cells starting with these characters can be run as formulas by spreadsheet apps.
const FORMULA_START = /^[=+\-@\t\r]/

function cell(value: string): string {
  const safe = FORMULA_START.test(value) ? `'${value}` : value
  return `"${safe.replace(/"/g, '""')}"`
}

export function skillsToCsv(skills: CatalogSkill[]): string {
  const rows = [['Skill', 'Type', 'Status', 'Description']]
  for (const skill of skills) {
    rows.push([skill.skillName, TYPE_LABELS[skill.skillType], STATUS_LABELS[skill.status], skill.skillDescription ?? ''])
  }
  return rows.map((row) => row.map(cell).join(',')).join('\r\n')
}

export function downloadSkillsCsv(skills: CatalogSkill[]) {
  const blob = new Blob([skillsToCsv(skills)], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'skills-catalog.csv'
  link.click()
  URL.revokeObjectURL(url)
}
