package com.group9.skillshub.organizations.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "organization")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int organizationId;

    @Column(nullable = false)
    private String organizationName;

    /** Person who manages this organization; nullable so unmanaged orgs can exist. */
    private Integer organizationManagerPersonId;

    public int getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(int organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public Integer getOrganizationManagerPersonId() {
        return organizationManagerPersonId;
    }

    public void setOrganizationManagerPersonId(Integer organizationManagerPersonId) {
        this.organizationManagerPersonId = organizationManagerPersonId;
    }
}
