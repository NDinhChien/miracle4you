package mfy.server.domain.message.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.domain.message.entity.GlobalMessage;
import mfy.server.domain.message.entity.PrivateMessage;
import mfy.server.domain.message.entity.ProjectMessage;
import mfy.server.domain.message.entity.SystemMessage;
import mfy.server.domain.message.service.type.OnlineUser;
import mfy.server.domain.project.entity.Project;
import mfy.server.domain.project.service.TranslatorService;
import mfy.server.domain.user.entity.User;
import mfy.server.domain.user.repository.UserRepository;

@Slf4j
@RequiredArgsConstructor
@Component
public class MessageManager {

    private final Map<Long, OnlineUser> users = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> projects = new ConcurrentHashMap<>();

    private final SimpMessagingTemplate messagingTemplate;
    private final TranslatorService translatorService;
    private final UserRepository userRepository;

    public void addUser(User user) {
        OnlineUser onlineUser = users.get(user.getId());
        if (onlineUser == null) {
            onlineUser = new OnlineUser(user);
            users.put(user.getId(), onlineUser);
        } else {
            onlineUser.updateIsOnline();
        }
        sendOnline(onlineUser);
        addToProjects(user);
    }

    private void addToProjects(User user) {
        List<Long> projectIds = translatorService.getProjectIds(user);
        for (Long projectId : projectIds) {
            projects.computeIfAbsent(projectId, k -> ConcurrentHashMap.newKeySet()).add(
                    user.getEmail());

            log.info("{} joined project chat: {}", user.getEmail(), projectId);
            log.info("Total members: {}", this.getMemberCount(projectId));
        }
    }

    @Transactional
    public void removeUser(User user) {
        OnlineUser onlineUser = users.get(user.getId());
        if (onlineUser != null) {
            onlineUser.updateLastOnline();
            updateLastOnline(user);
            sendOnline(onlineUser);
        }
        removeFromProjects(user);
    }

    private void removeFromProjects(User user) {
        List<Long> projectIds = translatorService.getProjectIds(user);
        for (Long projectId : projectIds) {
            Set<String> members = projects.get(projectId);
            if (members != null) {
                members.remove(user.getEmail());
                if (members.isEmpty()) {
                    projects.remove(projectId);
                }
                log.info("{} left project chat: {}", user.getEmail(), projectId);
            }
        }
    }

    private void updateLastOnline(User user) {
        userRepository.updateLastOnline(user.getId(), Instant.now());
    }

    private int getMemberCount(Long projectId) {
        Set<String> members = projects.get(projectId);
        return members != null ? members.size() : 0;
    }

    public List<OnlineUser> getUsers() {
        return users.values().stream().toList();
    }

    public void sendToProject(Project project, ProjectMessage payload) {
        Set<String> members = projects.get(project.getId());
        if (members == null)
            return;

        for (String member : members) {
            messagingTemplate.convertAndSendToUser(member, "/queue/message/project", payload);
        }
    }

    public void sendToUser(User sender, User recipient, PrivateMessage playload) {
        String recipientEmail = recipient.getEmail();
        String senderEmail = sender.getEmail();
        messagingTemplate.convertAndSendToUser(recipientEmail, "/queue/message/private", playload);
        if (recipientEmail != senderEmail) {
            messagingTemplate.convertAndSendToUser(senderEmail, "/queue/message/private", playload);
        }
    }

    public void sendGlobal(GlobalMessage payload) {
        messagingTemplate.convertAndSend("/topic/message/global", payload);
    }

    public void sendSystem(SystemMessage payload) {
        messagingTemplate.convertAndSend("/topic/message/system", payload);
    }

    public void sendOnline(OnlineUser user) {
        messagingTemplate.convertAndSend("/topic/message/online", Arrays.asList(user));
    }

    @Scheduled(fixedRate = 2, timeUnit = TimeUnit.MINUTES)
    public void sendOnlines() {
        messagingTemplate.convertAndSend("/topic/message/online", users.values());
    }
}
