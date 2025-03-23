package mfy.server.domain.project.entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.Cacheable;
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
@Table(name = "translators", uniqueConstraints = { @UniqueConstraint(columnNames = { "project_id", "user_id" }) })
@Entity
public class Translator extends TranslatorBase {
    public Translator(User user, Project project) {
        super(user, project);
    }
}
