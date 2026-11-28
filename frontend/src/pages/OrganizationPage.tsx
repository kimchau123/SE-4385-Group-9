import type { Page } from '../data'
import { Icon, PageIntro } from '../components/ui'
import { useEffect, useState } from 'react'
import { getOrganizationHierarchy, getOrganizationMembers, getPersonSkillDetails, getSkillDistribution, getSkillSummary, filterOrganizationMembers, getProficiencyLevels, getPersonById, getSkillCapacity, getOverallSkillCapacity } from '../api/organization'
import type { OrganizationHierarchy, Person, OrganizationPersonSkill, OrganizationSkillDistribution, OrganizationProficiencyLevel, OrganizationSkillCapacity } from '../api/organization'

export function OrganizationPage({ employeeQuery, setEmployeeQuery, selectedOrg, setSelectedOrg, selectedEmployee, setSelectedEmployee, navigate }: {
  employeeQuery: string; setEmployeeQuery: (value: string) => void; selectedOrg: string; setSelectedOrg: (value: string) => void; selectedEmployee: string; setSelectedEmployee: (value: string) => void; navigate: (page: Page) => void
}) {
  const [organizations, setOrganizations] = useState<OrganizationHierarchy[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [selectedOrgId, setSelectedOrgId] = useState<number | null>(null)
  const[people, setPeople] = useState<Person[]>([])
  const[membersLoading, setMembersLoading] = useState(false)
  const[membersError, setMembersError] = useState('')
  const[selectedPersonId, setSelectedPersonId] = useState<number | null>(null)
  const [personSkills, setPersonSkills] =useState<OrganizationPersonSkill[]>([])
  const [skillsLoading,setSkillsLoading] = useState(false)
  const [skillsError, setSkillsError] = useState('')
  const [skillDistribution, setSkillDistribution] = useState<OrganizationSkillDistribution[]>([])
  const [distributionLoading, setDistributionLoading] =useState(false)
  const [distributionError, setDistributionError] =useState('')
  const [skillSummary, setSkillSummary] = useState<OrganizationSkillDistribution[]>([]) 
  const [summaryLoading, setSummaryLoading] = useState(false) 
  const [summaryError, setSummaryError] = useState('')
  const [selectedSkillId, setSelectedSkillId] = useState<number | null>(null) 
  const [selectedProficiencyId, setSelectedProficiencyId] = useState<number | null>(null) 
  const [matchedPeople, setMatchedPeople] = useState<Person[]>([]) 
  const [filterLoading, setFilterLoading] = useState(false) 
  const [filterError, setFilterError] = useState('')
  const [proficiencyLevels, setProficiencyLevels] =useState<OrganizationProficiencyLevel[]>([])
  const [proficiencyLoading, setProficiencyLoading] =useState(true)
  const [proficiencyError, setProficiencyError] =useState('')
  const [manager, setManager] = useState<Person | null>(null);
  const [managerLoading, setManagerLoading] = useState(false);
  const [managerError, setManagerError] = useState('');
  const [skillCapacity, setSkillCapacity] = useState<OrganizationSkillCapacity | null>(null) 
  const [capacityLoading, setCapacityLoading] = useState(false) 
  const [capacityError, setCapacityError] = useState('')
  const [showOverallCapacity, setShowOverallCapacity] = useState(false)
  useEffect(() => {
    getOrganizationHierarchy()
      .then(data => setOrganizations(data))
      .catch(err => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (selectedOrgId === null) return
    let ignore = false
    getOrganizationMembers(selectedOrgId)
      .then(data => {
        if (!ignore) setPeople(data)
      })
      .catch(err => {
        if (!ignore) setMembersError(err.message)
      })
      .finally(() => {
        if (!ignore) setMembersLoading(false)
      })
    return () => {
      ignore = true
    }
  }, [selectedOrgId])

  useEffect(() => {
    if (selectedPersonId === null) return
    let ignore = false
    getPersonSkillDetails(selectedPersonId)
      .then(data => {
        if (!ignore) {
          setPersonSkills(
            data.filter(
              skill => skill.status === 'APPROVED'
            )
          )
        }
      })
      .catch(err => {
        if (!ignore) {
          setSkillsError(err.message)
        }
      })
      .finally(() => {
        if (!ignore) {
          setSkillsLoading(false)
        }
      })

    return () => {
      ignore = true
    }
  }, [selectedPersonId])

  useEffect(() => {
    if (selectedOrgId === null) return
    let ignore = false
    getSkillDistribution(selectedOrgId, selectedSkillId, selectedProficiencyId)
      .then(data => {
        if (!ignore) {
          setSkillDistribution(data)
        }
      })
      .catch(err => {
        if (!ignore) {
          setDistributionError(err.message)
        }
      })
      .finally(() => {
        if (!ignore) {
          setDistributionLoading(false)
        }
      })
    return () => {
      ignore = true
    }
  }, [selectedOrgId, selectedSkillId, selectedProficiencyId])

  useEffect(() => { 
    if (selectedOrgId === null) return 
    let ignore = false 
      getSkillSummary(selectedOrgId) 
      .then(data => { 
        if (!ignore) { 
          setSkillSummary(data) 
        } 
      }) 
      .catch(err => { 
        if (!ignore) { 
          setSummaryError(err.message) 
        } 
      }) 
      .finally(() => { 
        if (!ignore) { 
          setSummaryLoading(false) 
        } 
      }) 
      return () => { 
        ignore = true 
      } 
  }, [selectedOrgId])

  useEffect(() => { 
    if ( selectedOrgId === null || (selectedSkillId === null && selectedProficiencyId === null) )
      return 
    let ignore = false 
    filterOrganizationMembers( selectedOrgId, selectedSkillId, selectedProficiencyId ) 
    .then(data => { 
      if (!ignore) 
        setMatchedPeople(data) 
      }) 
    .catch(err => { 
      if (!ignore) 
        setFilterError(err.message) 
      }) 
    .finally(() => { 
      if (!ignore) 
        setFilterLoading(false) 
      }) 
    return () => { 
      ignore = true 
    } 
  }, [ selectedOrgId, selectedSkillId, selectedProficiencyId ])

  useEffect(() => {
    let ignore = false
    getProficiencyLevels()
      .then(data => {
        if (!ignore) {
          setProficiencyLevels(data)
        }
      })
      .catch(err => {
        if (!ignore) {
          setProficiencyError(err.message)
        }
      })
      .finally(() => {
        if (!ignore) {
          setProficiencyLoading(false)
        }
      })

    return () => {
      ignore = true
    }
  }, [])

  useEffect(() => { 
    if (!showOverallCapacity && selectedOrgId === null)return
    let ignore = false 
    const request = showOverallCapacity ? getOverallSkillCapacity() : getSkillCapacity(selectedOrgId!) 
    request 
    .then(data => { 
      if (!ignore) 
        setSkillCapacity(data) 
      }) 
      .catch(err => { 
        if (!ignore) 
          setCapacityError(err.message) 
        }) 
      .finally(() => { 
        if (!ignore)
          setCapacityLoading(false) 
        }) 
      return () => { 
        ignore = true 
      } 
    }, [selectedOrgId, showOverallCapacity])

  const selectedPerson = people.find(person => person.personId === selectedPersonId)
  const managerId = selectedPerson?.managerPersonId ?? null
  const hasActiveFilters = selectedSkillId !== null || selectedProficiencyId !== null 
  const displayedPeople = hasActiveFilters ? matchedPeople : people 
  const filteredPeople = displayedPeople.filter(person => 
    person.personName 
      .toLowerCase() 
      .includes(employeeQuery.trim().toLowerCase()) 
    )

  useEffect(() => {
    if (managerId === null) return
    let ignore = false
    getPersonById(managerId)
      .then(data => {
        if (!ignore) setManager(data)
      })
      .catch(err => {
        if (!ignore) setManagerError(err.message)
      })
      .finally(() => {
        if (!ignore) setManagerLoading(false)
      })
    return () => {
      ignore = true
    }
  }, [managerId])

  function resetFilters(hasFilters: boolean) {
    setMatchedPeople([])
    setFilterError('')
    setSelectedPersonId(null)
    setPersonSkills([])
    setSkillsError('')
    setSkillsLoading(false)
    setFilterLoading(hasFilters)
    resetManager(null)
  }

  function resetDistribution() {
    setSkillDistribution([])
    setDistributionError('')
    setDistributionLoading(true)
  }

  function resetCapacity(shouldLoad: boolean) {
    setSkillCapacity(null)
    setCapacityError('')
    setCapacityLoading(shouldLoad)
  }

  function resetManager(nextManagerId: number | null) {
    setManager(null)
    setManagerError('')
    setManagerLoading(nextManagerId !== null)
  }
  function renderHierarchy(nodes: OrganizationHierarchy[]) {
    return nodes.map(org => (
      <div className="hierarchy-node" key={org.organizationID}>
        <button
          type="button"
          className={`hierarchy-item ${
            selectedOrgId === org.organizationID
              ? 'selected'
              : ''
          }`}
          onClick={() => {
            if (selectedOrgId === org.organizationID) return
            resetDistribution()
            resetFilters(false)
            resetCapacity(true)
            setSkillSummary([])
            setSummaryError('')
            setSummaryLoading(true)

            setMembersLoading(true)
            setMembersError('')
            setPeople([])
            setSelectedPersonId(null)

            setPersonSkills([])
            setSkillsError('')
            setSkillsLoading(false)

            setSelectedOrgId(org.organizationID)
            setSelectedOrg(org.organizationName)
            setSelectedSkillId(null)
            setSelectedProficiencyId(null)
          }}
        >
          <span className="hierarchy-marker" aria-hidden="true" />
          <span className="hierarchy-copy">
            <strong>{org.organizationName}</strong>
            <small>
              Manager: {org.managerName ?? 'Not assigned'}
            </small>
          </span>
          <span className="hierarchy-count">
            {org.memberCount} {org.memberCount === 1 ? 'member' : 'members'}
          </span>
        </button>
        {renderHierarchy(org.children)}
      </div>
    ))
  }
  return <>
    <PageIntro title="Organization" />
    <div className="organization-layout">
      <section className="panel hierarchy-panel">
        <h2>Organization hierarchy</h2>
        {loading && <p>Loading organizations...</p>}
        {error && <p role="alert">{error}</p>}
        {!loading && !error && organizations.length === 0 && (
          <p>No organizations found.</p>
        )}
        {renderHierarchy(organizations)}
      </section>
      <section className="panel org-resources">
        <label className="search-field">
          <Icon name="search" size={15} />
          <input
            value={employeeQuery}
            onChange={(event) => setEmployeeQuery(event.target.value)}
            placeholder="Search employees"
            aria-label="Search employees"
          />
        </label>
        <div className="organization-filters">
          <div className="organization-filter-header">
            <div>
              <strong>Filter resources</strong>
              {hasActiveFilters && (
                <span className="active-filter-count">
                  {[selectedSkillId, selectedProficiencyId].filter(id => id !== null).length} active
                </span>
              )}
            </div>
            <button
              className="clear-filter-button"
              type="button"
              onClick={() => {
                resetDistribution()
                setSelectedSkillId(null)
                setSelectedProficiencyId(null)
              }}
              disabled={!hasActiveFilters}
            >
              Clear
            </button>
          </div>
          <div className="organization-filter-controls">
            <label className="organization-filter-control">
              <span>Skill</span>
              <select
                value={selectedSkillId ?? ''}
                onChange={event => {
                  const nextId = event.target.value === '' ? null : Number(event.target.value)
                  if (nextId === selectedSkillId) return

                  resetDistribution()
                  resetFilters(nextId !== null || selectedProficiencyId !== null)
                  setSelectedSkillId(nextId)
                }}
                disabled={selectedOrgId === null || summaryLoading || !!summaryError}
                aria-label="Filter by skill"
              >
                <option value="">All skills</option>
                {skillSummary.map(skill => (
                  <option key={skill.skillId} value={skill.skillId}>
                    {skill.skillName}
                  </option>
                ))}
              </select>
            </label>
            <label className="organization-filter-control">
              <span>Proficiency</span>
              <select
                value={selectedProficiencyId ?? ''}
                onChange={event => {
                  const nextId = event.target.value === '' ? null : Number(event.target.value)
                  if (nextId === selectedProficiencyId) return

                  resetDistribution()
                  setSelectedProficiencyId(nextId)
                }}
                disabled={
                  selectedOrgId === null || proficiencyLoading || !!proficiencyError
                }
                aria-label="Filter by proficiency"
              >
                <option value="">All levels</option>
                {proficiencyLevels.map(level => (
                  <option
                    key={level.skillProficiencyLevelId}
                    value={level.skillProficiencyLevelId}
                  >
                    {level.skillProficiencyLevelName}
                  </option>
                ))}
              </select>
            </label>
          </div>
          {filterLoading && <p className="filter-status">Applying filters...</p>}
          {filterError && <p className="filter-error" role="alert">{filterError}</p>}
          {summaryError && <p className="filter-error" role="alert">{summaryError}</p>}
          {proficiencyError && <p className="filter-error" role="alert">{proficiencyError}</p>}
        </div>
        <div className="section-heading compact">
          <h2>Resources and skills</h2>
          <span className="count-pill">
            {filteredPeople.length} resources
          </span>
        </div>

        {membersLoading && <p>Loading employees...</p>}
        {membersError && <p role="alert">{membersError}</p>}        
        {selectedOrgId === null && !showOverallCapacity && (
          <p>Select an organization to view employees.</p>
        )}
        {selectedOrgId !== null && !membersLoading && !membersError && filteredPeople.length === 0 && (<p>No employees found.</p>)}

        <div style={{ maxHeight: '400px', overflowY: 'auto'}}>
          {filteredPeople.map(person => (
            <button 
              className={`org-person ${
                selectedPersonId === person.personId ? 'selected' : ''
              }`}
              type="button"
              key={person.personId}
              onClick={() => {
                if (selectedPersonId === person.personId) return

                const nextManagerId = person.managerPersonId ?? null
                if (nextManagerId !== managerId) {
                  resetManager(nextManagerId)
                }

                setPersonSkills([])
                setSkillsError('')
                setSkillsLoading(true)

                setSelectedPersonId(person.personId)
                setSelectedEmployee(person.personName)
              }}
            >
              <span className="avatar">
                {person.personName
                  .split(' ')
                    .map(name => name.charAt(0))
                    .slice(0,2)
                    .join('')}
              </span>

              <span>
                <strong>{person.personName}</strong>
                <small>Employee ID: {person.personId}</small>
              </span>

              <span className="count-pill">
                {selectedPersonId === person.personId
                  ? 'Selected'
                  : 'View skills'}
              </span>
            </button>
          ))}
        </div>

        {selectedPersonId !== null && (
          <div className="selected-person-skills">
            <div className="selected-person-skills-header">
              <div>
                <span>Approved skills</span>
                <h3>{selectedEmployee}</h3>
              </div>
              {!skillsLoading && !skillsError && (
                <span className="skill-count-pill">{personSkills.length}</span>
              )}
            </div>
            <div className="employee-reporting">
              <strong>Reports to: </strong>

              {managerLoading && <span>Loading manager...</span>}

              {managerError && (
                <span role="alert">{managerError}</span>
              )}

              {!managerLoading && !managerError && manager && (
                <span>
                  {manager.personName} (ID: {manager.personId})
                </span>
              )}

              {!managerLoading && !managerError && managerId === null && (
                <span>No direct manager</span>
              )}
            </div>
            {skillsLoading && <p>Loading skills...</p>}
            {skillsError && (<p role="alert">{skillsError}</p>)}
            {!skillsLoading && !skillsError && personSkills.length === 0 && (<p>No approved skills found.</p>)}
            <div className="employee-skills-list">
              {personSkills.map(skill => (
                <div className="employee-skill" key={skill.skillId}>
                  <div className="employee-skill-main">
                    <span className="employee-skill-icon">
                      <Icon name="layers" size={12} />
                    </span>
                    <strong>{skill.skillName}</strong>
                  </div>
                  <div className="employee-skill-details">
                    <span className="proficiency-badge">
                      {skill.skillProficiencyLevelName}
                    </span>
                    <span className="approved-badge">
                      <i />
                      Approved
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )
        }
      </section>

      <aside className="panel distribution-panel">
        <div className="panel-heading">
          <h2>Department skills distribution</h2>
          <span className="count-pill">Top 5 skills</span>
        </div>

        {selectedOrgId === null && (<p>Select an organization to view skills distribution.</p>)}
        {selectedOrgId !== null && <p>{selectedOrg}</p>}
        {(distributionLoading || membersLoading) && (<p>Loading skills distribution...</p>)}
        {distributionError && (<p role="alert">{distributionError}</p>)}
        {membersError && (<p role="alert">Unable to calculate employee percentages.</p>)}
        {selectedOrgId !== null && !distributionLoading && !membersLoading && !distributionError && !membersError && skillDistribution.length === 0 && (<p>No approval skills found in this organization.</p>)}
        {!distributionLoading && !membersLoading && !distributionError && !membersError && skillDistribution.map(skill => {
          const percentage = people.length > 0? (skill.employeeCount / people.length) * 100: 0
          return (
            <div className="distribution-item" key ={skill.skillId}>
              <div> 
                <span>{skill.skillName}</span>
                <small>
                  {skill.employeeCount} {skill.employeeCount === 1 ? 'employee' : 'employees'} ({percentage.toFixed(1)}%)
                </small>
              </div>

              <div className="progress-track">
              <span style={{width: `${percentage}%` }} />
              </div>
            </div>
          )
        })}

        <div className="distribution-note">
          <strong>Distribution summary</strong>
          <p>Top five approved skills by employee count in the selected organization</p>
        </div>

        <div className="distribution-note"> 
          <h2>Skill Summary</h2> 
          {selectedOrgId === null && ( <p>Select an organization to view its skill summary.</p> )} 
          {(summaryLoading || membersLoading) && ( <p>Loading skill summary...</p> )} 
          {summaryError && ( <p role="alert">{summaryError}</p> )} 
          {membersError && ( <p role="alert">Unable to load employee counts.</p> )} 
          {selectedOrgId !== null && !summaryLoading && !membersLoading && !summaryError && !membersError && ( 
            <> 
            <p>Total employees: {people.length}</p> 
            <p>Distinct approved skills: {skillSummary.length}</p> 
            {skillSummary.length === 0 && ( 
              <p>No approved skills found.</p> 
            )} 
            <div style={{ maxHeight: '300px', overflowY: 'auto' }}> 
              {skillSummary.map(skill => { 
                const percentage = people.length > 0 
                ? (skill.employeeCount / people.length) * 100 : 0 
                return ( 
                <div className="distribution-item" key={skill.skillId} > 
                <div> 
                  <span>{skill.skillName}</span> 
                  <small> {skill.employeeCount}{' '} 
                    {skill.employeeCount === 1 ? 'employee' : 'employees'} 
                    {' '}({percentage.toFixed(1)}%) 
                    </small> 
                  </div> 
                </div> 
              ) 
            })} 
            </div> 
            </> 
          )} 
        </div>
        <div className="distribution-note">
          <h2>Skill Capacity</h2>
          <select
            className="skill-capacity-select"
            value={showOverallCapacity ? 'all' : 'department'}
            onChange={event => {
              const nextOverall = event.target.value === 'all'
              if (nextOverall === showOverallCapacity) return

              resetCapacity(nextOverall || selectedOrgId !== null)
              setShowOverallCapacity(nextOverall)
            }}
            aria-label="Skill capacity scope"
          >
            <option value="department">Selected department</option> 
            <option value="all">All organizations</option> 
          </select>
          {selectedOrgId === null && (<p>Select an organization to view skill capacity.</p>)}
          {capacityLoading && <p>Loading skill capacity...</p>}
          {capacityError && (<p role="alert">{capacityError}</p>)}
          {!capacityLoading && !capacityError && skillCapacity && (<>
              <p>Total employees: {skillCapacity.totalEmployees}</p>
              <p>With approved skills: {skillCapacity.employeesWithSkills}</p>
              <p>Without approved skills: {skillCapacity.employeesWithoutSkills}</p>
              <p>Distinct skills: {skillCapacity.distinctSkills}</p>
              <div className="distribution-item">
                <div>
                  <strong>Skill coverage</strong>
                  <small>{skillCapacity.skillCoveragePercentage.toFixed(1)}%</small>
                </div>

                <div className="progress-track">
                  <span style={{width: `${skillCapacity.skillCoveragePercentage}%`}} />
                </div>
              </div>

              <h3>Proficiency distribution</h3>

              {Object.entries(skillCapacity.proficiencyDistribution)
                .map(([level, count]) => (
                  <div className="distribution-item" key={level}>
                    <div>
                      <span>{level}</span>
                      <small>{count} assignments</small>
                    </div>
                  </div>
                ))}
            </>
          )}
        </div>
        <button className="report-link" type="button" onClick={() => navigate('Data Reports')}>
          <Icon name="arrow" size={15} />
          Open Data Reports for deeper analytics
        </button>
      </aside>
    </div>
  </>
}
