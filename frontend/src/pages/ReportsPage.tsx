import { useEffect, useState } from 'react'
import { getInventorySummary, type InventorySummary } from '../api/home'
import { getReportsData, type ReportsData } from '../api/reports'
import { MiniMetric, PageIntro, StatCard } from '../components/ui'

export function ReportsPage() {
  const [summary, setSummary] = useState<InventorySummary | null>(null)
  const [reports, setReports] = useState<ReportsData | null>(null)

  useEffect(() => {
    let active = true
    Promise.all([getInventorySummary(), getReportsData()]).then(([summaryData, reportsData]) => {
      if (active) {
        setSummary(summaryData)
        setReports(reportsData)
      }
    })
    return () => {
      active = false
    }
  }, [])

  if (!summary || !reports) {
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

  return <>
    <PageIntro title="Data Reports" description="Read-only analytics for organizational leaders. This dashboard summarizes approved assignments, department coverage, and illustrative historical proficiency trends.">
      <span className="report-role">Role: Organizational Leader</span><span className="report-role">Reporting period: {summary.periodLabel}</span>
    </PageIntro>
    <div className="report-filters">{['All departments', 'All teams', 'All skills', 'All categories', 'All proficiencies', 'Approved only', 'Q3 2026'].map((item, index) => <select key={item} aria-label={`Report filter ${index + 1}`} defaultValue={item}><option>{item}</option>{['Engineering', 'Product', 'Operations', 'Finance'].map((choice) => <option key={choice}>{choice}</option>)}</select>)}</div>
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
    <section className="panel coverage-panel"><h2>Skill coverage / capacity overview</h2><p>Distinct employees with approved assignments and Advanced/Expert assignment counts by department. Pending assignments are shown separately, outside the Approved-only filter.</p><div className="table-scroll"><table><thead><tr><th>Department</th><th>Employees</th><th>Adv/Expert pairs</th><th>Pending pairs*</th><th>Takeaway</th></tr></thead><tbody>{[['Engineering', '42', '18', '6', 'Highest advanced/expert count and largest employee base.'], ['Product', '36', '14', '4', '14 Advanced/Expert assignments; 4 pending assignments.'], ['Operations', '28', '10', '8', 'Smaller advanced/expert base with more pending reviews.'], ['Finance', '22', '15', '3', 'Highest Advanced/Expert assignment density per employee.']].map((row) => <tr key={row[0]}>{row.map((cell, index) => <td key={cell}>{index === 0 ? <strong>{cell}</strong> : cell}</td>)}</tr>)}</tbody></table></div><div className="mini-metrics"><MiniMetric label="Capacity note" value="Observed coverage only" note="" /><MiniMetric label="Pending context" value="21 pending" note="" /><MiniMetric label="Compact takeaway" value="Engineering has the largest employee base and the highest advanced/expert count." note="" /></div></section>
  </>
}