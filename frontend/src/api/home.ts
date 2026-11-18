export type InventorySummary = {
  employeesRepresented: number
  skillsRepresented: number
  approvedAssignments: number
  departmentsRepresented: number
  periodLabel: string
}

// TODO: sync with backend
export async function getInventorySummary(): Promise<InventorySummary> {
  return {
    employeesRepresented: 128,
    skillsRepresented: 42,
    approvedAssignments: 184,
    departmentsRepresented: 4,
    periodLabel: 'Q3 2026 · quarter ended 30 Sep',
  }
}