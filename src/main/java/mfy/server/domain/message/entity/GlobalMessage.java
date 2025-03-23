package mfy.server.domain.message.entity;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mfy.server.domain.user.entity.User;
import mfy.server.domain.message.entity.type.Attachment;
import mfy.server.domain.message.entity.type.MessageBase;
import mfy.server.global.serializer.UserSerializer;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "globalmessages", indexes = {
        @Index(name = "globalmessage_createdAt_idx", columnList = "createdAt"),
})
public class GlobalMessage extends MessageBase {

    @JsonSerialize(using = UserSerializer.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(name = "sender_id", insertable = false, updatable = false)
    private Long senderId;

    public GlobalMessage(User sender, String content, List<Attachment> attachments) {
        super(content, attachments);
        this.sender = sender;
        this.senderId = sender.getId();
    }
}
