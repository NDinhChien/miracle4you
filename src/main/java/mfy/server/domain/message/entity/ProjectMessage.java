package mfy.server.domain.message.entity;

import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.util.List;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mfy.server.domain.project.entity.Project;
import mfy.server.domain.user.entity.User;
import mfy.server.domain.message.entity.type.Attachment;
import mfy.server.domain.message.entity.type.MessageBase;
import mfy.server.global.serializer.ProjectSerializer;
import mfy.server.global.serializer.UserSerializer;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "projectmessages", indexes = {
        @Index(name = "projectmessage_createdAt_idx", columnList = "createdAt"),
        @Index(name = "projectmessage_projectId_idx", columnList = "projectId"),
})
public class ProjectMessage extends MessageBase {

    @JsonSerialize(using = ProjectSerializer.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "project_id", insertable = false, updatable = false)
    private Long projectId;

    @JsonSerialize(using = UserSerializer.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(name = "sender_id", insertable = false, updatable = false)
    private Long senderId;

    public ProjectMessage(Project project, User sender, String content, List<Attachment> attachments) {
        super(content, attachments);
        this.project = project;
        this.projectId = project.getId();
        this.sender = sender;
        this.senderId = sender.getId();
    }
}
