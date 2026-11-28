package com.group9.skillshub.organizations.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "person")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int personId;

    @Column(nullable = false)
    private String personName;

    private Integer managerPersonId;

    private Integer memberOfOrganizationId;

    @Column(nullable = false)
    private boolean isAdmin;

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public Integer getManagerPersonId() {
        return managerPersonId;
    }

    public void setManagerPersonId(Integer managerPersonId) {
        this.managerPersonId = managerPersonId;
    }

    public Integer getMemberOfOrganizationId() {
        return memberOfOrganizationId;
    }

    public void setMemberOfOrganizationId(Integer memberOfOrganizationId) {
        this.memberOfOrganizationId = memberOfOrganizationId;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
