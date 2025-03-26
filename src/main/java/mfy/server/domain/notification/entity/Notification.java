package mfy.server.domain.notification.entity;

import java.time.Instant;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mfy.server.domain.user.entity.User;

import jakarta.persistence.Index;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "notifs_recipient_id_idx", columnList = "recipientId"),
        @Index(name = "notifs_created_at_idx", columnList = "createdAt"),
})
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String content;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Column(name = "recipient_id", insertable = false, updatable = false)
    private Long recipientId;

    @Column(nullable = false)
    private Boolean isRead;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
        if (this.isRead == null) {
            this.isRead = false;
        }
    }

    public Notification updateIsRead() {
        this.isRead = true;
        return this;
    }
}
