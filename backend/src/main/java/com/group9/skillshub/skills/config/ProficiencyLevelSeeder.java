package com.group9.skillshub.skills.config;

import com.group9.skillshub.skills.model.SkillProficiencyLevel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seeds proficiency levels on an empty table so ids are 1-5 (Beginner = 1, Expert = 5),
 * which TC-RM-01 and TC-WP-05 rely on. Hibernate creates the table (ddl-auto=update),
 * so this runs as code instead of a data.sql script. Levels 2 and 4 are placeholder names.
 */
@Component
public class ProficiencyLevelSeeder implements ApplicationRunner {

    static final List<String> LEVELS =
            List.of("Beginner", "Elementary", "Intermediate", "Advanced", "Expert");

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Long existing = entityManager
                .createQuery("select count(l) from SkillProficiencyLevel l", Long.class)
                .getSingleResult();
        if (existing > 0) {
            return;
        }
        for (String name : LEVELS) {
            SkillProficiencyLevel level = new SkillProficiencyLevel();
            level.setSkillProficiencyLevelName(name);
            entityManager.persist(level);
        }
    }
}
