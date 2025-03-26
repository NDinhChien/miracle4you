package mfy.server.domain.message.repository;

import mfy.server.domain.message.dto.MessageResponseDto.SystemMessageDto;
import mfy.server.domain.message.entity.SystemMessage;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemMessageRepository extends JpaRepository<SystemMessage, Long> {

    Page<SystemMessageDto> findAllBy(Pageable pageable);

    List<SystemMessageDto> findByCreatedAtGreaterThanOrIsLastingTrue(Instant createdAt);

    List<SystemMessageDto> findByCreatedAtGreaterThan(Instant createdAt);
}