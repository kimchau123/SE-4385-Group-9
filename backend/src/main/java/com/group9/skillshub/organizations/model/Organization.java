package com.group9.skillshub.organizations.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "organization")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int organizationId;

    @NotBlank
    @Column(nullable = false)
    private String organizationName;

    private int organizationManagerPersonId;

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

    public int getOrganizationManagerPersonId() {
        return organizationManagerPersonId;
    }

    public void setOrganizationManagerPersonId(int organizationManagerPersonId) {
        this.organizationManagerPersonId = organizationManagerPersonId;
    }
}
