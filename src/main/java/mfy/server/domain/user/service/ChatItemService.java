package mfy.server.domain.user.service;

import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.domain.project.entity.Project;
import mfy.server.domain.user.dto.UserResponseDto.IChatItemDto;
import mfy.server.domain.user.entity.ChatItem;
import mfy.server.domain.user.entity.User;
import mfy.server.domain.user.repository.ChatItemRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatItemService {
    private final ChatItemRepository chatItemRepository;

    public List<IChatItemDto> getByUser(User user) {
        return this.chatItemRepository.findBySender(user);
    }

    public boolean safelyAddChatItem(User sender, User recipient, Project project) {
        ChatItem item = new ChatItem(sender, recipient, project);
        boolean exists = chatItemRepository.exists(Example.of(item));
        if (!exists) {
            chatItemRepository.save(item);
            return true;
        }
        return false;
    }
}
