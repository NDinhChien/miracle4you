package mfy.server.domain.message.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mfy.server.domain.message.entity.type.Attachment;
import mfy.server.domain.project.dto.ProjectResponseDto.IProjectBasicDto;
import mfy.server.domain.user.dto.UserResponseDto.IUserBasicDto;

public class MessageResponseDto {

    @Getter
    @NoArgsConstructor
    public static class MessageResponse {
        private List<IUserBasicDto> users;
        private List<IProjectBasicDto> projects;

        public void setUsers(List<IUserBasicDto> users) {
            this.users = users;
        }

        public void setProjects(List<IProjectBasicDto> projects) {
            this.projects = projects;
        }
    }

    @Getter
    public static class GetMessageResponseDto<T> extends MessageResponse {
        private int page;
        private List<T> messages;
        private int totalPage;

        public GetMessageResponseDto(int page, List<T> messages, int totalPage) {
            super();
            this.page = page;
            this.messages = messages;
            this.totalPage = totalPage;
        }
    }

    public static interface IMessage {
        Long getId();

        String getContent();

        LocalDateTime getCreatedAt();

        LocalDateTime getUpdatedAt();

        Boolean getIsDeleted();

        List<Attachment> getAttachments();
    }

    public static interface ISystemMessage extends IMessage {
    };

    public static interface IGlobalMessage extends IMessage {
        Long getSenderId();

    };

    public static interface IProjectMessage extends IMessage {
        Long getSenderId();

        Long getProjectId();
    };

    public static interface IPrivateMessage extends IMessage {
        Long getPairId();

        Long getSenderId();

        Long getRecipientId();
    };

    public static record MessageBaseDto(
            Long id,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean isDeleted,
            List<Attachment> attachments) {
    }

    public static record SystemMessageDto(
            Long id,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean isDeleted,
            Boolean isLasting,
            List<Attachment> attachments) {
    }

    // Global Message
    public static record GlobalMessageDto(
            Long id,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean isDeleted,
            List<Attachment> attachments,
            Long senderId) {
    }

    // Project Message
    public static record ProjectMessageDto(
            Long id,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean isDeleted,
            List<Attachment> attachments,
            Long senderId,
            Long projectId) {
    }

    // Private Message
    public static record PrivateMessageDto(
            Long id,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean isDeleted,
            List<Attachment> attachments,
            Long pairId,
            Long senderId,
            Long recipientId) {
    }

}
