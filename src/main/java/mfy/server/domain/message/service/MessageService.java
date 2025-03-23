package mfy.server.domain.message.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.domain.message.dto.MessageRequestDto.AttachtmentRequestDto;
import mfy.server.domain.message.dto.MessageRequestDto.DownloadAttachmentDto;
import mfy.server.domain.message.dto.MessageRequestDto.SendMessageRequestDto;
import mfy.server.domain.message.dto.MessageRequestDto.UpdateAttachmentDto;
import mfy.server.domain.message.dto.MessageRequestDto.UpdateAttachmentsDto;
import mfy.server.domain.message.dto.MessageResponseDto.GetMessageResponseDto;
import mfy.server.domain.message.dto.MessageResponseDto.GlobalMessageDto;
import mfy.server.domain.message.dto.MessageResponseDto.PrivateMessageDto;
import mfy.server.domain.message.dto.MessageResponseDto.ProjectMessageDto;
import mfy.server.domain.message.dto.MessageResponseDto.SystemMessageDto;
import mfy.server.domain.message.entity.PrivateMessage;
import mfy.server.domain.message.entity.ProjectMessage;
import mfy.server.domain.message.entity.GlobalMessage;
import mfy.server.domain.message.entity.SystemMessage;
import mfy.server.domain.message.entity.type.Attachment;
import mfy.server.domain.message.entity.type.MessageType;
import mfy.server.domain.message.repository.PrivateMessageRepository;
import mfy.server.domain.message.repository.ProjectMessageRepository;
import mfy.server.domain.message.repository.GlobalMessageRepository;
import mfy.server.domain.message.repository.SystemMessageRepository;
import mfy.server.domain.message.service.type.OnlineUser;
import mfy.server.domain.project.dto.ProjectResponseDto.IProjectBasicDto;
import mfy.server.domain.project.entity.Project;
import mfy.server.domain.project.repository.ProjectRepository;
import mfy.server.domain.project.service.TranslatorService;
import mfy.server.domain.user.dto.UserResponseDto.IUserBasicDto;
import mfy.server.domain.user.entity.User;
import mfy.server.domain.user.entity.type.Role;
import mfy.server.domain.user.repository.UserRepository;
import mfy.server.global.exception.BusinessException;
import mfy.server.global.exception.ErrorConfig.ErrorMessage;
import mfy.server.global.service.S3Service;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {
    private static final int PAGE_SIZE = 10;
    private static final int MAX_ATTACHMENT_TOTAL_SIZE = 15 * 1024 * 1024;
    private static final int MAX_ATTACHMENT_UPLOAD_COUNT = 5;

    private final S3Service s3Service;
    private final TranslatorService translatorService;
    private final SystemMessageRepository systemMessageRepository;
    private final GlobalMessageRepository globalMessageRepository;
    private final PrivateMessageRepository privateMessageRepository;
    private final ProjectMessageRepository projectMessageRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final MessageManager messageManager;

    @Qualifier("systemMessagesCache")
    @Autowired
    private Cache systemMessagesCache;

    private <T> GetMessageResponseDto<T> getMessages(Pageable pageable, Page<T> result) {
        List<T> messages = result.toList();
        int totalPage = result.getTotalPages();
        int page = pageable.getPageNumber();
        var responseDto = new GetMessageResponseDto<T>(page, messages, totalPage);
        addUsersAndProjects(responseDto);
        return responseDto;
    }

    private <T> void addUsersAndProjects(GetMessageResponseDto<T> responseDto) {
        Set<Long> projectIds = new HashSet<Long>();
        Set<Long> userIds = new HashSet<Long>();
        List<IProjectBasicDto> projects = new ArrayList<IProjectBasicDto>();
        List<IUserBasicDto> users = new ArrayList<IUserBasicDto>();

        getUsersAndProjects(userIds, projectIds, responseDto.getMessages());
        if (userIds.size() > 0)
            users = userRepository.findInIds(userIds);
        if (projectIds.size() > 0)
            projects = projectRepository.findInIds(projectIds);

        responseDto.setUsers(users);
        responseDto.setProjects(projects);
    }

    private <T> void getUsersAndProjects(Set<Long> userIds, Set<Long> projectIds, List<T> messages) {
        if (messages.size() <= 0)
            return;
        T firstMessagge = messages.get(0);
        if (firstMessagge instanceof GlobalMessageDto) {
            for (T message : messages) {
                userIds.add(((GlobalMessageDto) message).senderId());
            }
        } else if (firstMessagge instanceof ProjectMessageDto) {
            projectIds.add(((ProjectMessageDto) firstMessagge).projectId());
            for (T message : messages) {
                userIds.add(((ProjectMessageDto) message).senderId());
            }
        } else if (firstMessagge instanceof PrivateMessageDto) {
            userIds.add(((PrivateMessageDto) firstMessagge).senderId());
            userIds.add(((PrivateMessageDto) firstMessagge).recipientId());
        }
    }

    @Transactional
    public GetMessageResponseDto<Object> getUnreadMessages(User user) {
        LocalDateTime lastOnline = user.getLastOnline();
        List<GlobalMessageDto> globalMessages = globalMessageRepository.findBySenderAndCreatedAtGreaterThan(user,
                lastOnline);
        List<SystemMessageDto> systemMessages = systemMessageRepository.findByCreatedAtGreaterThan(lastOnline);
        List<PrivateMessageDto> privateMessages = privateMessageRepository.findByRecipientAndCreatedAtGreaterThan(user,
                lastOnline);
        List<Project> projects = translatorService.getProjects(user);
        List<ProjectMessageDto> projectMessages = projectMessageRepository
                .findByProjectInAndCreatedAtGreaterThan(projects, lastOnline);
        List<Object> messages = Stream.of(globalMessages, systemMessages, privateMessages, projectMessages)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        var responseDto = new GetMessageResponseDto<>(0, messages, 0);
        addUsersAndProjects(responseDto);
        updateLastOnline(user);
        return responseDto;
    }

    public List<OnlineUser> getOnlineUsers() {
        return messageManager.getUsers();
    }

    @Cacheable(value = "SystemMessages", key = "'getTodaySystemMessages'", cacheManager = "caffeinCacheManager")
    public List<SystemMessageDto> getTodaySystemMessages() {
        LocalDateTime today = LocalDateTime.now();
        return systemMessageRepository.findByCreatedAtGreaterThanOrIsLastingTrue(today.minusDays(1));
    }

    @Cacheable(value = "SystemMessages", key = "#page", unless = "#result.page != #result.totalPage - 1", cacheManager = "caffeinCacheManager")
    public GetMessageResponseDto<SystemMessageDto> getSystemMessages(int page) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), PAGE_SIZE, Sort.Direction.ASC, "createdAt");
        var result = systemMessageRepository.findAllBy(pageable);
        return getMessages(pageable, result);
    }

    @Cacheable(value = "GlobalMessages", key = "#page", unless = "#result.page != #result.totalPage - 1", cacheManager = "caffeinCacheManager")
    public GetMessageResponseDto<GlobalMessageDto> getGlobalMessages(int page) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), PAGE_SIZE, Sort.Direction.ASC, "createdAt");
        var result = globalMessageRepository.findAllBy(pageable);
        return getMessages(pageable, result);
    }

    @Cacheable(value = "PrivateMessages", key = "#pairId + ':' + #page", unless = "#result.page != #result.totalPage - 1", cacheManager = "caffeinCacheManager")
    public GetMessageResponseDto<PrivateMessageDto> getPrivateMessages(Long pairId, int page) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), PAGE_SIZE, Sort.Direction.ASC, "createdAt");
        var result = privateMessageRepository.findAllByPairId(pairId, pageable);
        return getMessages(pageable, result);
    }

    @Cacheable(value = "ProjectMessages", key = "#projectId + ':' + #page", unless = "#result.page != #result.totalPage - 1", cacheManager = "caffeinCacheManager")
    public GetMessageResponseDto<ProjectMessageDto> getProjectMessages(Long projectId, int page) {
        Project project = validateProject(projectId);
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), PAGE_SIZE, Sort.Direction.ASC, "createdAt");
        var result = projectMessageRepository.findAllByProject(project, pageable);

        return getMessages(pageable, result);
    }

    public List<Attachment> uploadAttachments(List<MultipartFile> attachments) {
        if (attachments.size() < 1)
            return List.of();
        if (attachments.size() > MAX_ATTACHMENT_UPLOAD_COUNT) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.MAXIMUM_ATTACHMENT_FILES);
        }
        Long totalSize = attachments.stream().map(a -> a.getSize()).reduce(0l, (a, b) -> a + b);
        if (totalSize > MAX_ATTACHMENT_TOTAL_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.MAXIMUM_ATTACHMENT_TOTAL_SIZE);
        }

        return s3Service.uploadAttachments(attachments);
    }

    private List<Attachment> presignedAttachments(List<AttachtmentRequestDto> attachments) {
        if (attachments.size() < 1)
            return List.of();
        if (attachments.size() > MAX_ATTACHMENT_UPLOAD_COUNT) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.MAXIMUM_ATTACHMENT_FILES);
        }
        Long totalSize = attachments.stream().map(a -> a.getSize()).reduce(0l, (a, b) -> a + b);
        if (totalSize > MAX_ATTACHMENT_TOTAL_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.MAXIMUM_ATTACHMENT_TOTAL_SIZE);
        }

        return s3Service.presignedAttachments(attachments);
    }

    @Transactional
    public SystemMessage sendSystemMessage(User user, SendMessageRequestDto requestDto) {
        if (user.getRole() != Role.ADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN, ErrorMessage.ADMIN_ONLY);
        }
        var attachments = presignedAttachments(requestDto.getAttachments());
        var message = new SystemMessage(requestDto.getContent(), attachments);
        message = systemMessageRepository.save(message);
        messageManager.sendSystem(message);
        systemMessagesCache.evict("getTodaySystemMessages");
        return message;
    }

    @Transactional
    public GlobalMessage sendGlobalMessage(User user, SendMessageRequestDto requestDto) {
        var attachments = presignedAttachments(requestDto.getAttachments());
        log.info("attachments", attachments);
        var message = new GlobalMessage(user, requestDto.getContent(), attachments);
        message = globalMessageRepository.save(message);
        messageManager.sendGlobal(message);
        return message;
    }

    @Transactional
    public PrivateMessage sendPrivateMessage(User sender, Long recipientId, SendMessageRequestDto requestDto) {
        User recipient = validateUser(recipientId);
        var attachments = presignedAttachments(requestDto.getAttachments());
        var message = new PrivateMessage(sender, recipient, requestDto.getContent(), attachments);
        message = privateMessageRepository.save(message);
        messageManager.sendToUser(sender, recipient, message);
        return message;
    }

    @Transactional
    public ProjectMessage sendProjectMessage(Long projectId, User sender, SendMessageRequestDto requestDto) {
        Project project = validateProject(projectId);
        boolean isMember = translatorService.isMember(project, sender);
        if (!isMember) {
            throw new BusinessException(HttpStatus.FORBIDDEN, ErrorMessage.MEMBER_ONLY);
        }
        List<Attachment> attachments = presignedAttachments(requestDto.getAttachments());
        var message = new ProjectMessage(project, sender, requestDto.getContent(), attachments);
        message = projectMessageRepository.save(message);
        messageManager.sendToProject(project, message);
        return message;
    }

    private Attachment getAttachment(List<Attachment> attachments, Long attachmentId) {
        return attachments.stream().filter(a -> a.getId() == attachmentId).findFirst().orElse(null);
    }

    private void updateAttachments(List<Attachment> attachments, List<UpdateAttachmentDto> updateDto) {
        attachments.stream().forEach(attachment -> {
            for (UpdateAttachmentDto dto : updateDto) {
                if (dto.getId() == attachment.getId()) {
                    attachment.setIsSuccess(dto.getIsSuccess());
                    attachment.setUploadedAt(dto.getUploadedAt());
                    break;
                }
            }
        });
    }

    public Object updateAttachments(User user, UpdateAttachmentsDto requestDto) {
        Long id = requestDto.getMessageId();
        MessageType type = requestDto.getMessageType();
        List<UpdateAttachmentDto> updateDto = requestDto.getAttachments();
        try {
            if (type == MessageType.GLOBAL) {
                var message = globalMessageRepository.findById(id).get();
                updateAttachments(message.getAttachments(), updateDto);
                message = globalMessageRepository.save(message);
                messageManager.sendGlobal(message);
                return message;
            } else if (type == MessageType.SYSTEM) {
                var message = systemMessageRepository.findById(id).get();
                updateAttachments(message.getAttachments(), updateDto);
                message = systemMessageRepository.save(message);
                messageManager.sendSystem(message);
                return message;
            } else if (type == MessageType.PROJECT) {
                var message = projectMessageRepository.findById(id).get();
                Project project = message.getProject();
                updateAttachments(message.getAttachments(), updateDto);
                message = projectMessageRepository.save(message);
                messageManager.sendToProject(project, message);
                return message;
            } else if (type == MessageType.PRIVATE) {
                var message = privateMessageRepository.findById(id).get();
                User recipient = message.getRecipient();
                updateAttachments(message.getAttachments(), updateDto);
                message = privateMessageRepository.save(message);
                messageManager.sendToUser(user, recipient, message);
                return message;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public String getDownloadUrl(User user, DownloadAttachmentDto requestDto) {
        Long id = requestDto.getMessageId();
        MessageType type = requestDto.getMessageType();
        Attachment attachment = null;
        boolean hasRight = false;
        try {
            if (type == MessageType.GLOBAL) {
                var message = globalMessageRepository.findById(id).get();
                attachment = getAttachment(message.getAttachments(), requestDto.getAttachmentId());
                hasRight = true;
            } else if (type == MessageType.SYSTEM) {
                var message = systemMessageRepository.findById(id).get();
                attachment = getAttachment(message.getAttachments(), requestDto.getAttachmentId());
                hasRight = true;
            } else if (type == MessageType.PROJECT) {
                var message = projectMessageRepository.findById(id).get();
                Project project = message.getProject();
                attachment = getAttachment(message.getAttachments(), requestDto.getAttachmentId());
                hasRight = translatorService.isMember(project, user);
            } else if (type == MessageType.PRIVATE) {
                var message = privateMessageRepository.findById(id).get();
                attachment = getAttachment(message.getAttachments(), requestDto.getAttachmentId());
                hasRight = message.getSenderId() == user.getId() || message.getRecipientId() == user.getId();
            }
            if (attachment != null && hasRight) {
                return s3Service.generateGetUrl(attachment.getKey());
            }
        } catch (Exception e) {
        }
        return null;
    }

    private void updateLastOnline(User user) {
        userRepository.updateLastOnline(user.getId(), LocalDateTime.now());
    }

    private User validateUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.USER_NOT_FOUND);
        });
    }

    private Project validateProject(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.PROJECT_NOT_FOUND);
        });
    }

}
