package mfy.server.domain.notification.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import mfy.server.domain.notification.dto.NotificationResponseDto.INotification;
import mfy.server.domain.notification.entity.Notification;
import mfy.server.domain.user.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository  extends JpaRepository<Notification, Long> {
    
    long countByRecipient(User recipient);

    @Query("""
        SELECT 
            n.id AS id, n.content AS content, n.createdAt AS createdAt, 
            n.recipientId AS recipientId, n.isDeleted AS isDeleted, n.isRead AS isRead 
        FROM Notification n 
        WHERE n.recipient = :recipient     
    """)
    Page<INotification> getByRecipient(User recipient, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE (n.recipient = :recipient) AND (n.id in :ids)")
    List<Notification> getSomeByRecipient(User recipient, Long[] ids);

    @Modifying
    @Query("UPDATE Notification n SET n.isDeleted = true WHERE n.recipient = :recipient")
    void softDeleteByRecipient(User recipient) ;

    void deleteAllByRecipient(User recipient);
}
