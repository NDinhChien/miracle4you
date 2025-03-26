package mfy.server.domain.message.entity.type;

import java.time.Instant;
import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@MappedSuperclass
public class MessageBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Attachment> attachments;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
        if (this.attachments == null) {
            this.attachments = List.of();
        }
    }

    public MessageBase(String content) {
        this.content = content;
    }

    public MessageBase(String content, List<Attachment> attachments) {
        this.content = content;
        this.attachments = attachments;
    }

    public MessageBase updateContent(String content) {
        this.content = content;
        this.updatedAt = Instant.now();
        return this;
    }

    public MessageBase delete() {
        this.isDeleted = true;
        return this;
    }
}
