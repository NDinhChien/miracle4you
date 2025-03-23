package mfy.server.domain.user.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import mfy.server.domain.project.dto.ProjectResponseDto.IProjectBasicDto;
import mfy.server.domain.project.dto.ProjectResponseDto.IProjectDto;
import mfy.server.domain.project.dto.ProjectResponseDto.ITranslatorBaseDto;
import mfy.server.domain.project.dto.ProjectResponseDto.ProjectDto;
import mfy.server.domain.project.dto.ProjectResponseDto.TranslatorBaseDto;
import mfy.server.domain.user.entity.ChatItem;
import mfy.server.domain.user.entity.User;
import mfy.server.domain.user.entity.type.Gender;
import mfy.server.domain.user.entity.type.Role;

public class UserResponseDto {

    @Getter
    public static class TokenResponseDto {
        private final String accessToken;
        private final String refreshToken;

        public TokenResponseDto(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }

    @Getter
    public static class UserProfileResponseDto {
        private final UserDto user;
        private final List<IChatItemDto> chatItems;
        private final List<ProjectDto> projects;

        public UserProfileResponseDto(User user, List<IChatItemDto> chatItems, List<ProjectDto> projects) {
            this.user = UserDto.fromEntity(user);
            this.chatItems = chatItems;
            this.projects = projects;
        }
    }

    public static record UserBasicDto(
            Long id,
            String nickname,
            String avatar,
            String fullName,
            Integer score,
            LocalDateTime joinedAt) {

        public static UserBasicDto fromEntity(User user) {
            return new UserBasicDto(
                    user.getId(),
                    user.getNickname(),
                    user.getAvatar(),
                    user.getFullName(),
                    user.getScore(),
                    user.getJoinedAt());
        }
    }

    public static record UserPublicDto(
            Long id,
            String nickname,
            String avatar,
            String fullName,
            Integer score,
            LocalDateTime joinedAt,

            LocalDateTime birthday,
            Gender gender,
            String bio,
            String diocese,
            String parish,
            Role role) {

        public static UserPublicDto fromEntity(User user) {
            return new UserPublicDto(
                    user.getId(),
                    user.getNickname(),
                    user.getAvatar(),
                    user.getFullName(),
                    user.getScore(),
                    user.getJoinedAt(),
                    user.getBirthday(),
                    user.getGender(),
                    user.getBio(),
                    user.getDiocese(),
                    user.getParish(),
                    user.getRole());
        }
    }

    public static record UserDto(
            Long id,
            String nickname,
            String avatar,
            String fullName,
            Integer score,
            LocalDateTime joinedAt,

            LocalDateTime birthday,
            Gender gender,
            String bio,
            String diocese,
            String parish,
            Role role,

            String email,
            Boolean isVerified,
            Boolean isBanned,
            LocalDateTime lastUpdateProfile,
            List<TranslatorBaseDto> translators,
            List<TranslatorBaseDto> applicants) {

        public static UserDto fromEntity(User user) {
            return new UserDto(
                    user.getId(),
                    user.getNickname(),
                    user.getAvatar(),
                    user.getFullName(),
                    user.getScore(),
                    user.getJoinedAt(),
                    user.getBirthday(),
                    user.getGender(),
                    user.getBio(),
                    user.getDiocese(),
                    user.getParish(),
                    user.getRole(),
                    user.getEmail(),
                    user.getIsVerified(),
                    user.getIsBanned(),
                    user.getLastUpdateProfile(),
                    user.getTranslators() != null ? user.getTranslators().stream()
                            .map(TranslatorBaseDto::fromEntity)
                            .collect(Collectors.toList())
                            : null,
                    user.getApplicants() != null ? user.getApplicants().stream()
                            .map(TranslatorBaseDto::fromEntity)
                            .collect(Collectors.toList())
                            : null);
        }
    }

    public static interface IUserBasicDto {
        Long getId();

        String getNickname();

        String getAvatar();

        Integer getScore();

        String getFullName();

        LocalDateTime getJoinedAt();
    }

    public static interface IUserPublicDto extends IUserBasicDto {

        LocalDateTime getBirthday();

        Gender getGender();

        String getBio();

        String getDiocese();

        String getParish();

        List<ITranslatorBaseDto> getTranslators();
    }

    public static interface IUserDto extends IUserBasicDto {

        String getEmail();

        Boolean getIsVerified();

        Boolean getIsBanned();

        Role getRole();

        LocalDateTime getLastUpdateNickname();

        LocalDateTime getLastUpdateProfile();

        LocalDateTime getLastLoginSuccessAt();

        List<ITranslatorBaseDto> getApplicants();

        List<IProjectDto> getProjects();
    }

    public static record ChatItemDto(
            Long senderId,
            Long recipientId,
            Long projectId) {
        public static ChatItemDto fromEntity(ChatItem chatItem) {
            return new ChatItemDto(chatItem.getSenderId(), chatItem.getRecipientId(), chatItem.getProjectId());
        }
    }

    public static interface IChatItemDto {
        Long getSenderId();

        Long getProjectId();

        Long getRecipientId();

        IUserBasicDto getRecipient();

        IProjectBasicDto getProject();
    }
}
