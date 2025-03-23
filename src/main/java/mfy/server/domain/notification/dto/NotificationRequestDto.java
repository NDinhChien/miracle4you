package mfy.server.domain.notification.dto;

import java.util.List;

import lombok.Getter;

public class NotificationRequestDto {

    @Getter
    public static class UpdateNotificationRequestDto {
        List<Long> ids;
    }

}
