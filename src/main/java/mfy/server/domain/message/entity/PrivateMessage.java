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
@Table(name = "privatemessages", indexes = {
        @Index(name = "privatemessage_createdAt_idx", columnList = "createdAt"),
        @Index(name = "privatemessage_pairId_idx", columnList = "pairId"),
})
public class PrivateMessage extends MessageBase {

    @Column(nullable = false)
    private Long pairId;

    @JsonSerialize(using = UserSerializer.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(name = "sender_id", insertable = false, updatable = false)
    private Long senderId;

    @JsonSerialize(using = UserSerializer.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Column(name = "recipient_id", insertable = false, updatable = false)
    private Long recipientId;

    public PrivateMessage(User sender, User recipient, String content, List<Attachment> attachments) {
        super(content, attachments);
        this.sender = sender;
        this.senderId = sender.getId();
        this.recipient = recipient;
        this.recipientId = recipient.getId();
        this.pairId = PrivateMessage.calcPairId(sender.getId(), recipient.getId());
    }

    public static Long calcPairId(Long id, Long id2) {
        var idString = "";
        if (id > id2) {
            idString = Long.toString(id2) + Long.toString(id);
        } else {
            idString = Long.toString(id) + Long.toString(id2);
        }
        return Long.parseLong(idString);
    }
}
