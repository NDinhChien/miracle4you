package mfy.server.domain.notification.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mfy.server.domain.notification.dto.NotificationRequestDto.UpdateNotificationRequestDto;
import mfy.server.domain.notification.dto.NotificationResponseDto.GetNotificationResponseDto;

import mfy.server.domain.notification.entity.Notification;
import mfy.server.domain.notification.repository.NotificationRepository;
import mfy.server.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private static final int PAGE_SIZE = 10;
    private final NotificationRepository notificationRepository;
    
    @Cacheable(value = "Notifications", key = "#user.id + ':' + #page", unless = "#result.page != #result.totalPage - 1", cacheManager = "caffeinCacheManager")
    public GetNotificationResponseDto getPage(User user, int page) {
        if (page == 0) {
            page = Math.round(notificationRepository.countByRecipient(user) / PAGE_SIZE);
        }
        Pageable pageable = PageRequest.of(Math.max(page-1, 0), PAGE_SIZE, Direction.DESC, "createdAt");
        var result = notificationRepository.getByRecipient(user, pageable);
        return new GetNotificationResponseDto(pageable.getPageNumber(), result.toList(), result.getTotalPages());
    }

    public void updateRead(User user, UpdateNotificationRequestDto requestDto) {
        
        List<Notification> notifications = this.notificationRepository.getSomeByRecipient(user, (Long[]) requestDto.getIds().toArray());

        for(Notification notif : notifications) {
            notif.updateIsRead();
            notificationRepository.save(notif);
        }
    }

    public void deleteByUser(User user) {
        notificationRepository.deleteAllByRecipient(user);
    }
}
