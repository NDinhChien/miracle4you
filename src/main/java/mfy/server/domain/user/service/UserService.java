package mfy.server.domain.user.service;

import java.util.Arrays;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.domain.project.dto.ProjectResponseDto.IProjectDto;
import mfy.server.domain.project.dto.ProjectResponseDto.ProjectDto;
import mfy.server.domain.project.entity.Project;
import mfy.server.domain.project.repository.ProjectRepository;
import mfy.server.domain.project.service.TranslatorService;
import mfy.server.domain.user.dto.UserRequestDto.AddChatItemRequestDto;
import mfy.server.domain.user.dto.UserRequestDto.UpdateRequestDto;
import mfy.server.domain.user.dto.UserResponseDto.IChatItemDto;
import mfy.server.domain.user.dto.UserResponseDto.IUserBasicDto;
import mfy.server.domain.user.dto.UserResponseDto.UserProfileResponseDto;
import mfy.server.domain.user.entity.User;
import mfy.server.domain.user.repository.UserRepository;
import mfy.server.global.exception.BusinessException;
import mfy.server.global.exception.ErrorConfig.ErrorMessage;
import mfy.server.global.service.S3Service;
import mfy.server.global.util.CommonUtil;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private static final int PAGE_SIZE = 10;
    private static final List<String> RESERVED_NAMES = Arrays.asList(
            "admin", "system", "translator", "reviewer");
    public static final int UPDATE_NICKNAME_DISTANCE_DAYS = 7;

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final ChatItemService chatItemService;
    private final TranslatorService translatorService;

    public UserProfileResponseDto getProfile(Long id) {
        User user = validateUser(id);
        List<IChatItemDto> chatItems = getChatItems(user);
        List<ProjectDto> projects = getWorkingProjects(user);
        return new UserProfileResponseDto(user, chatItems, projects);
    }

    public boolean addChatItem(User sender, AddChatItemRequestDto requestDto) {
        Long recipientId = requestDto.getRecipientId();
        User recipient = recipientId != null ? validateUser(recipientId) : null;
        Long projectId = requestDto.getProjectId();
        Project project = projectId != null ? validateProject(projectId) : null;
        return chatItemService.safelyAddChatItem(sender, recipient, project);
    }

    public List<IChatItemDto> getChatItems(User user) {
        return chatItemService.getByUser(user);
    }

    public List<IProjectDto> getProjects(User user) {
        return projectRepository.findByAdmin(user);
    }

    public List<ProjectDto> getWorkingProjects(User user) {
        return translatorService.getProjects(user).stream().map(ProjectDto::fromEntity).toList();
    }

    public IUserBasicDto getPublicProfile(Long id) {
        return userRepository.findUserById(id).orElseThrow(
                () -> {
                    return new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.USER_NOT_FOUND);
                });
    }

    public User updateProfile(User user, UpdateRequestDto requestDto, MultipartFile avatar) {
        String nickname = requestDto.getNickname();

        if (user.canUpdateNickname() && StringUtils.hasText(nickname)) {
            if (RESERVED_NAMES.contains(nickname) || userRepository.existsByNickname(nickname)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.NICKNAME_EXISTS);
            } else {
                user.updateNickname(nickname);
            }
        }

        if (avatar != null && !avatar.isEmpty()) {
            String profileUrl = s3Service.uploadAvatar(avatar);
            if (profileUrl != null) {
                user.updateAvatar(profileUrl);
            }
        }

        user.updateProfile(requestDto);
        return userRepository.save(user);
    }

    @Cacheable(value = "Users", cacheManager = "caffeinCacheManager")
    public List<IUserBasicDto> searchUsers(String name, int page) {
        long id = CommonUtil.toId(name);
        if (id >= 0) {
            return Arrays.asList(userRepository.findUserBasicById(id).orElse(null));
        }

        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                PAGE_SIZE,
                Sort.Direction.ASC,
                "nickname");

        Page<IUserBasicDto> result;
        if (name == null) {
            result = userRepository.findAllBy(pageable);
        } else {
            result = userRepository.searchUsers(name, pageable);
        }
        return result.stream().toList();
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
