package mfy.server.domain.project.entity.type;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mfy.server.domain.project.entity.Project;
import mfy.server.domain.user.entity.User;

@Getter
@EqualsAndHashCode(of = { "userId", "projectId" })
@NoArgsConstructor
@MappedSuperclass
public class TranslatorBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "project_id", insertable = false, updatable = false)
    private Long projectId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public TranslatorBase(User user, Project project) {
        this.user = user;
        this.userId = user.getId();
        this.project = project;
        this.projectId = project.getId();
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}