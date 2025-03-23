package mfy.server.domain.message.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mfy.server.domain.message.entity.type.Attachment;
import mfy.server.domain.message.entity.type.MessageBase;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "systemmessages", indexes = {
        @Index(name = "systemmessage_createdAt_idx", columnList = "createdAt"),
})
public class SystemMessage extends MessageBase {

    @Column(nullable = false)
    private boolean isLasting = false;

    public SystemMessage(String content) {
        super(content);
    }

    public SystemMessage(String content, List<Attachment> attachments) {
        super(content, attachments);
    }

    public SystemMessage(String content, boolean isLasting) {
        super(content);
        this.isLasting = isLasting;
    }

    public SystemMessage updateIsLasting(boolean isLasting) {
        this.isLasting = isLasting;
        return this;
    }
}
