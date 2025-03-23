package mfy.server.domain.message.repository;

import mfy.server.domain.message.dto.MessageResponseDto.GlobalMessageDto;
import mfy.server.domain.message.dto.MessageResponseDto.PrivateMessageDto;
import mfy.server.domain.message.entity.PrivateMessage;
import mfy.server.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {
    Page<PrivateMessageDto> findAllByPairId(Long pairId, Pageable pageable);

    List<PrivateMessageDto> findByRecipientAndCreatedAtGreaterThan(User recipient, LocalDateTime createdAt);

    long countByPairId(Long pairId);
}
