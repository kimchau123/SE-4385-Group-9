export type Page = 'Home' | 'Skills Catalog' | 'Skills Inventory' | 'Organization' | 'Data Reports' | 'Add Skill'
export const employees = [
  { name: 'Jordan Davis', initials: 'JD', email: 'jordan.davis@example.com', department: 'Engineering', team: 'Platform', title: 'Senior Engineer', count: 4 },
  { name: 'Nina Kim', initials: 'NK', email: 'nina.kim@example.com', department: 'Product', team: 'Growth', title: 'Product Manager', count: 3 },
  { name: 'Marcus Reed', initials: 'MR', email: 'marcus.reed@example.com', department: 'Operations', team: 'Support', title: 'Operations Analyst', count: 2 },
  { name: 'Maya Patel', initials: 'MP', email: 'maya.patel@example.com', department: 'Commercial Operations', team: 'Revenue Strategy', title: 'Revenue Manager', count: 3 },
  { name: 'Lucas Chen', initials: 'LC', email: 'lucas.chen@example.com', department: 'Commercial Operations', team: 'Revenue Strategy', title: 'Operations Analyst', count: 2 },
]

export const assignments = [
  { skill: 'Cloud Security', category: 'Security · Infrastructure', level: 'Advanced', approval: 'Approved', date: '12 Oct 2026' },
  { skill: 'API Design', category: 'Engineering · Platform', level: 'Intermediate', approval: 'Pending', date: '09 Oct 2026' },
  { skill: 'Observability', category: 'Operations · Monitoring', level: 'Beginner', approval: 'Approved', date: '02 Oct 2026' },
  { skill: 'Terraform', category: 'Engineering · Infrastructure', level: 'Expert', approval: 'Approved', date: '28 Sep 2026' },
]

export const navItems: { page: Page; icon: string }[] = [
  { page: 'Home', icon: 'home' },
  { page: 'Skills Catalog', icon: 'catalog' },
  { page: 'Skills Inventory', icon: 'layers' },
  { page: 'Organization', icon: 'organization' },
  { page: 'Data Reports', icon: 'chart' },
]
