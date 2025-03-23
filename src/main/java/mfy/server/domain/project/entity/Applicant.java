package mfy.server.domain.project.entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mfy.server.domain.user.entity.User;
import mfy.server.domain.project.entity.type.TranslatorBase;

@Getter
@NoArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@Entity
@Table(name = "applicants", uniqueConstraints = { @UniqueConstraint(columnNames = { "project_id", "user_id" }) })
public class Applicant extends TranslatorBase {
    public Applicant(User user, Project project) {
        super(user, project);
    }
}
