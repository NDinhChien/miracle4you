package mfy.server.domain.message.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.validation.annotation.Validated;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import mfy.server.domain.message.entity.type.MessageType;

public class MessageRequestDto {

    @Getter
    @AllArgsConstructor
    public static class AttachtmentRequestDto {
        @NotEmpty
        private String name;
        @NotEmpty
        private String type;
        @NotNull
        private Long size;
    }

    @Getter
    @Validated
    public static class SendMessageRequestDto {

        @Schema(example = "Hello world!")
        @NotBlank
        private String content;

        private List<@Valid AttachtmentRequestDto> attachments;

    }

    @AllArgsConstructor
    @Getter
    public static class DownloadAttachmentDto {
        @NotNull
        Long messageId;

        @NotNull
        MessageType messageType;

        @NotNull
        Long attachmentId;
    }

    @Getter
    @Validated
    public static class UpdateAttachmentsDto {
        @NotNull
        Long messageId;

        @NotNull
        MessageType messageType;

        List<@Valid UpdateAttachmentDto> attachments;
    }

    @Getter
    public static class UpdateAttachmentDto {
        @NotNull
        Long id;

        @NotNull
        Boolean isSuccess;

        @NotNull
        LocalDateTime uploadedAt;
    }
}
