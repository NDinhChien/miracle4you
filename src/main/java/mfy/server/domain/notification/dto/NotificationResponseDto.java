package mfy.server.domain.notification.dto;

import java.time.Instant;
import java.util.List;

import lombok.Getter;

public class NotificationResponseDto {
    @Getter
    public static class GetNotificationResponseDto {
        private int page;
        private List<INotification> messages;
        private int totalPage;

        public GetNotificationResponseDto(int page, List<INotification> messages, int totalPage) {
            this.page = page;
            this.messages = messages;
            this.totalPage = totalPage;
        }
    }

    public static interface INotification {
        Long getId();

        String getContent();

        Instant getCreatedAt();

        Long getRecipientId();

        Long getIsDeleted();

        Long getIsRead();
    }
}
