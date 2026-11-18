export type DepartmentCount = { department: string; employees: number }
export type ProficiencyCount = { level: string; assignments: number }
export type QuarterValue = { quarter: string; approvedAssignments: number }
export type CoverageRow = { department: string; employees: number; advancedExpertPairs: number; pendingPairs: number; takeaway: string }

export type ReportsData = {
  departments: DepartmentCount[]
  proficiencies: ProficiencyCount[]
  trend: QuarterValue[]
  coverage: CoverageRow[]
}

// TODO: pull from backend eventually
export async function getReportsData(): Promise<ReportsData> {
  return {
    departments: [
      { department: 'Engineering', employees: 42 },
      { department: 'Product', employees: 36 },
      { department: 'Operations', employees: 28 },
      { department: 'Finance', employees: 22 },
    ],

    proficiencies: [
      { level: 'Beginner', assignments: 36 },
      { level: 'Intermediate', assignments: 91 },
      { level: 'Advanced', assignments: 40 },
      { level: 'Expert', assignments: 17 },
    ],

    trend: [
      { quarter: 'Q2 2025', approvedAssignments: 120 },
      { quarter: 'Q3 2025', approvedAssignments: 132 },
      { quarter: 'Q4 2025', approvedAssignments: 148 },
      { quarter: 'Q1 2026', approvedAssignments: 156 },
      { quarter: 'Q2 2026', approvedAssignments: 168 },
      { quarter: 'Q3 2026', approvedAssignments: 184 },
    ],
    
    coverage: [
      { department: 'Engineering', employees: 42, advancedExpertPairs: 18, pendingPairs: 6, takeaway: 'Highest advanced/expert count and largest employee base.' },
      { department: 'Product', employees: 36, advancedExpertPairs: 14, pendingPairs: 4, takeaway: '14 Advanced/Expert assignments; 4 pending assignments.' },
      { department: 'Operations', employees: 28, advancedExpertPairs: 10, pendingPairs: 8, takeaway: 'Smaller advanced/expert base with more pending reviews.' },
      { department: 'Finance', employees: 22, advancedExpertPairs: 15, pendingPairs: 3, takeaway: 'Highest Advanced/Expert assignment density per employee.' },
    ],
  }
}
