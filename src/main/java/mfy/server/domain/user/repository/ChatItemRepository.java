package mfy.server.domain.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import mfy.server.domain.user.dto.UserResponseDto.IChatItemDto;
import mfy.server.domain.user.entity.ChatItem;
import mfy.server.domain.user.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatItemRepository extends JpaRepository<ChatItem, Long> {

    List<IChatItemDto> findBySender(User sender);

}
