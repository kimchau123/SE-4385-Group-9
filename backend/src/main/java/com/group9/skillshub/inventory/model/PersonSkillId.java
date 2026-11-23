package com.group9.skillshub.inventory.model;

import java.io.Serializable;
import java.util.Objects;

public class PersonSkillId implements Serializable {

    private int personId;
    private int skillId;

    public PersonSkillId() {
    }

    public PersonSkillId(int personId, int skillId) {
        this.personId = personId;
        this.skillId = skillId;
    }

    public int getPersonId() {
        return personId;
    }

    public int getSkillId() {
        return skillId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PersonSkillId that)) {
            return false;
        }
        return personId == that.personId && skillId == that.skillId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId, skillId);
    }
}
