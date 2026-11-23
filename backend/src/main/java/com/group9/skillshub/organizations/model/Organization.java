package com.group9.skillshub.organizations.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name = "organization")
public class Organization{

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int organizationID;

    @Column(nullable = false)
    private String organizationName;

    //person who manages this organization; nullable for unmanaged orgs
    private Integer organizationManagerPersonId;

    public int getOrganizationID() {
        return organizationID;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public Integer getOrganizationManagerPersonId() {
        return organizationManagerPersonId;
    }

    public void setOrganizationID(int organizationID) {
        this.organizationID = organizationID;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public void setOrganizationManagerPersonId(Integer organizationManagerPersonId) {
        this.organizationManagerPersonId = organizationManagerPersonId;
    }

}