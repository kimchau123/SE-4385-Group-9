import { useEffect, useState } from 'react'
import { getInventorySummary, type InventorySummary } from '../api/home'
import { getReportFilterOptions, getReportsData, type ReportFilterOptions, type ReportsData } from '../api/reports'
import { MiniMetric, PageIntro, StatCard } from '../components/ui'
import { useAuth } from '../auth/useAuth'

export function ReportsPage() {
  const {isAdmin} = useAuth()
  const [summary, setSummary] = useState<InventorySummary | null>(null)
  const [reports, setReports] = useState<ReportsData | null>(null)
  const [filterOptions, setFilterOptions] = useState<ReportFilterOptions | null>(null)

  useEffect(() => {
    let active = true
    Promise.all([getInventorySummary(), getReportsData(), getReportFilterOptions()]).then(([summaryData, reportsData, filterData]) => {
      if (active) {
        setSummary(summaryData)
        setReports(reportsData)
        setFilterOptions(filterData)
      }
    })
    return () => {
      active = false
    }
  }, [])

  if (!summary || !reports || !filterOptions) {
    return <PageIntro title="Data Reports" description="Loading reports…" />
  }

  const percent = (part: number, total: number, digits = 0) => `${(part / total * 100).toFixed(digits)}%`
  const advancedExpert = reports.proficiencies
    .filter((item) => item.level === 'Advanced' || item.level === 'Expert')
    .reduce((sum, item) => sum + item.assignments, 0)
  const advancedExpertShare = percent(advancedExpert, summary.approvedAssignments)
  const departmentTotal = reports.departments.reduce((sum, item) => sum + item.employees, 0)
  const departmentsBySize = [...reports.departments].sort((a, b) => b.employees - a.employees)
  const largestDepartment = departmentsBySize[0]
  const smallestDepartment = departmentsBySize[departmentsBySize.length - 1]
  const proficiencyTotal = reports.proficiencies.reduce((sum, item) => sum + item.assignments, 0)
  const mostCommonLevel = [...reports.proficiencies].sort((a, b) => b.assignments - a.assignments)[0]
  const averagePerEmployee = (summary.approvedAssignments / summary.employeesRepresented).toFixed(2)
  const trendMax = Math.max(...reports.trend.map((item) => item.approvedAssignments))
  const latestTrend = reports.trend[reports.trend.length - 1]
  const pendingTotal = reports.departments.reduce((sum, row) => sum + row.pendingPairs, 0)
  const topAdvanced = [...reports.departments].sort((a, b) => b.advancedExpertPairs - a.advancedExpertPairs)[0]
  const compactTakeaway = largestDepartment.department === topAdvanced.department
    ? `${largestDepartment.department} has the largest employee base and the highest advanced/expert count.`
    : `${largestDepartment.department} has the largest employee base; ${topAdvanced.department} has the highest advanced/expert count.`

  const filters = [
    ['All departments', ...reports.departments.map((item) => item.department)],
    ['All teams', ...filterOptions.teams],
    ['All skills', ...filterOptions.skills],
    ['All categories', ...filterOptions.categories],
    ['All proficiencies', ...reports.proficiencies.map((item) => item.level)],
    ['Approved only', 'Include pending'],
    [...reports.trend].reverse().map((item) => item.quarter),
  ]

  return <>
    <PageIntro title="Data Reports" description="Read-only analytics for organizational leaders. This dashboard summarizes approved assignments, department coverage, and illustrative historical proficiency trends.">
      <span className="report-role"> Access: {isAdmin ? 'Administrator' : 'Employee'} </span><span className="report-role">Reporting period: {summary.periodLabel}</span>
    </PageIntro>
    <div className="report-filters">{filters.map((options, index) => <select key={options[0]} aria-label={`Report filter ${index + 1}`} defaultValue={options[0]}>{options.map((option) => <option key={option}>{option}</option>)}</select>)}</div>
    <div className="stats-grid report-stats">
      <StatCard label="Employees with approved assignments" value={summary.employeesRepresented.toLocaleString()} note="Distinct employees with at least one approved skill" accent="blue" />
      <StatCard label="Distinct skills" value={summary.skillsRepresented.toLocaleString()} note="Unique skills present in approved employee assignments" accent="blue" />
      <StatCard label="Approved assignments" value={summary.approvedAssignments.toLocaleString()} note="Total approved employee–skill pairs in the current reporting period" accent="blue" />
      <StatCard label="Advanced / Expert share" value={advancedExpertShare} note={`${advancedExpert} of ${summary.approvedAssignments} approved assignments are Advanced or Expert`} accent="blue" />
    </div>
    <div className="report-panels">
      <section className="panel report-panel">
        <h2>Department distribution</h2>
        <p>Unique employees with approved assignments by department. Values reconcile to the {departmentTotal} employees shown in the summary.</p>
        {reports.departments.map(({ department, employees }) => <div className="distribution-list-row" key={department}><span className="blue-square" /><strong>{department}</strong><span>{employees} employees</span><b>{percent(employees, departmentTotal)}</b></div>)}
        <div className="mini-metrics">
          <MiniMetric label="Largest department" value={largestDepartment.department} note={`${largestDepartment.employees} employees`} />
          <MiniMetric label="Smallest department" value={smallestDepartment.department} note={`${smallestDepartment.employees} employees`} />
          <MiniMetric label="Department coverage" value={`${reports.departments.length} departments`} note="All departments have approved assignments" />
        </div>
      </section>
      <section className="panel report-panel">
        <h2>Proficiency distribution</h2>
        <p>Approved assignments by proficiency level. Totals reconcile to {proficiencyTotal} approved assignments.</p>
        {reports.proficiencies.map(({ level, assignments }) => <div className="distribution-list-row" key={level}><span className="blue-square" /><strong>{level}</strong><span>{assignments} approved assignments</span><b>{percent(assignments, proficiencyTotal, 1)}</b></div>)}
        <div className="mini-metrics">
          <MiniMetric label="Most common level" value={mostCommonLevel.level} note={`${mostCommonLevel.assignments} approved assignments`} />
          <MiniMetric label="Advanced / Expert share" value={advancedExpertShare} note={`${advancedExpert} of ${proficiencyTotal} approved assignments`} />
          <MiniMetric label="Coverage note" value={`${summary.employeesRepresented} employees`} note={`${averagePerEmployee} approved assignments per employee on average`} />
        </div>
      </section>
    </div>
    <section className="panel historical-panel">
      <h2>Historical skill trend</h2>
      <div className="chart-area">{reports.trend.map(({ quarter, approvedAssignments }, index) => <div className="chart-column" key={quarter}><b>{approvedAssignments}</b><span className={`bar bar-${index}`} style={{ height: `${Math.round(approvedAssignments / trendMax * 78)}%` }} /><small>{quarter}</small></div>)}</div>
      <div className="mini-metrics">
        <MiniMetric label="Metric" value="Approved assignments" note="Approved employee–skill pairs at each quarter end." />
        <MiniMetric label="Latest value" value={String(latestTrend.approvedAssignments)} note={`${latestTrend.quarter} illustrative value.`} />
        <MiniMetric label="Historical note" value="Snapshots required" note="LastUpdated does not provide trend history." />
        <MiniMetric label="Takeaway" value="Illustrative growth" note="Proposed trend shows increasing approved assignments across quarters." />
      </div>
    </section>
    <section className="panel coverage-panel">
      <h2>Skill coverage / capacity overview</h2>
      <p>Distinct employees with approved assignments and Advanced/Expert assignment counts by department. Pending assignments are shown separately, outside the Approved-only filter.</p>
      <div className="table-scroll"><table><thead><tr><th>Department</th><th>Employees</th><th>Adv/Expert pairs</th><th>Pending pairs*</th><th>Takeaway</th></tr></thead><tbody>
        {reports.departments.map((row) => <tr key={row.department}><td><strong>{row.department}</strong></td><td>{row.employees}</td><td>{row.advancedExpertPairs}</td><td>{row.pendingPairs}</td><td>{row.takeaway}</td></tr>)}
      </tbody></table></div>
      <div className="mini-metrics">
        <MiniMetric label="Capacity note" value="Observed coverage only" note="" />
        <MiniMetric label="Pending context" value={`${pendingTotal} pending`} note="" />
        <MiniMetric label="Compact takeaway" value={compactTakeaway} note="" />
      </div>
    </section>
  </>
}