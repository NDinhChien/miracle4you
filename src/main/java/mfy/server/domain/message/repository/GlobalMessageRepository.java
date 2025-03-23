package mfy.server.domain.message.repository;

import mfy.server.domain.message.dto.MessageResponseDto.GlobalMessageDto;
import mfy.server.domain.message.entity.GlobalMessage;
import mfy.server.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalMessageRepository extends JpaRepository<GlobalMessage, Long> {

    Page<GlobalMessageDto> findAllBy(Pageable pageable);

    List<GlobalMessageDto> findBySenderAndCreatedAtGreaterThan(User sender, LocalDateTime createdAt);
}
