package com.group9.skillshub.organizations.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "person")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int personId;

    @NotBlank
    @Column(nullable = false)
    private String personName;

    private int managerPersonId;

    private int memberOfOrganizationId;

    /** Stored as an int (0/1) per the Detailed Design; exposed as a boolean through isAdmin(). */
    @Column(nullable = false)
    private int isAdmin;

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

    public int getManagerPersonId() {
        return managerPersonId;
    }

    public void setManagerPersonId(int managerPersonId) {
        this.managerPersonId = managerPersonId;
    }

    public int getMemberOfOrganizationId() {
        return memberOfOrganizationId;
    }

    public void setMemberOfOrganizationId(int memberOfOrganizationId) {
        this.memberOfOrganizationId = memberOfOrganizationId;
    }

    public boolean isAdmin() {
        return isAdmin == 1;
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin ? 1 : 0;
    }
}