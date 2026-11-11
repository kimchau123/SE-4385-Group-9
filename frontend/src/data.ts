export type Page = 'Home' | 'Skills Catalog' | 'Skills Inventory' | 'Organization' | 'Data Reports' | 'Add Skill'
export type SkillStatus = 'Active' | 'Pending' | 'Deprecated'
export type Skill = {
  name: string
  category: string
  type: string
  status: SkillStatus
  resources: number
  description: string
}

export const initialSkills: Skill[] = [
  { name: 'React', category: 'Software Engineering', type: 'Technical', status: 'Active', resources: 34, description: 'A JavaScript library for building reusable UI components. It helps teams create fast, declarative, and maintainable interfaces for web applications.' },
  { name: 'Java', category: 'Software Engineering', type: 'Technical', status: 'Deprecated', resources: 7, description: 'An object-oriented programming language used to build reliable enterprise applications.' },
  { name: 'Spring Boot', category: 'Software Engineering', type: 'Technical', status: 'Pending', resources: 9, description: 'A framework for building production-ready Spring applications.' },
  { name: 'C++', category: 'Software Engineering', type: 'Technical', status: 'Pending', resources: 21, description: 'A general-purpose language used for high-performance software.' },
  { name: 'Python', category: 'Data & Analytics', type: 'Technical', status: 'Active', resources: 28, description: 'A versatile language used for automation, analytics, and application development.' },
  { name: 'AWS', category: 'Cloud & DevOps', type: 'Technical', status: 'Active', resources: 16, description: 'Cloud infrastructure and services for secure, scalable workloads.' },
  { name: 'SQL', category: 'Data & Analytics', type: 'Technical', status: 'Active', resources: 19, description: 'A language for querying, transforming, and managing relational data.' },
  { name: 'Communication', category: 'Professional Skills', type: 'Behavioral', status: 'Active', resources: 12, description: 'Clear, considerate communication across teams and stakeholders.' },
]

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
