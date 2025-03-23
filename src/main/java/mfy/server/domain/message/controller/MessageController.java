package mfy.server.domain.message.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.domain.message.dto.MessageRequestDto.DownloadAttachmentDto;
import mfy.server.domain.message.dto.MessageRequestDto.SendMessageRequestDto;
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
import mfy.server.domain.message.entity.type.MessageType;
import mfy.server.domain.message.service.MessageService;
import mfy.server.domain.message.service.type.OnlineUser;
import mfy.server.global.dto.BaseResponse;
import mfy.server.global.security.UserDetailsImpl;

@Slf4j
@RequiredArgsConstructor
@Validated
@Tag(name = "message", description = "Message Related API")
@RequestMapping("/api/v1/message")
@RestController
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "Get Online Users")
    @GetMapping("/online")
    BaseResponse<List<OnlineUser>> getOnlineUsers() {
        var onlineUsers = messageService.getOnlineUsers();
        return BaseResponse.success("Online users", onlineUsers);
    }

    @Operation(summary = "Get Today System Messages")
    @GetMapping("/system/today")
    BaseResponse<List<SystemMessageDto>> getTodaySystemMessage() {
        return BaseResponse.success("Today system messages", messageService.getTodaySystemMessages());
    }

    @Operation(summary = "Get System Messages")
    @GetMapping("/system")
    BaseResponse<GetMessageResponseDto<SystemMessageDto>> getSystemMessage(
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        return BaseResponse.success("System messages", messageService.getSystemMessages(page));
    }

    @Operation(summary = "Get Global Messages")
    @GetMapping("/global")
    BaseResponse<GetMessageResponseDto<GlobalMessageDto>> getGlobalMessage(
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        return BaseResponse.success("Global message", messageService.getGlobalMessages(page));
    }

    @Operation(summary = "Get Private Messages")
    @GetMapping("/private")
    public BaseResponse<GetMessageResponseDto<PrivateMessageDto>> getPrivateMessage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam Long recipientId,
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        Long pairId = PrivateMessage.calcPairId(recipientId, userDetails.getUser().getId());
        return BaseResponse.success("Private messages", messageService.getPrivateMessages(pairId, page));
    }

    @Operation(summary = "Get Project Messages")
    @GetMapping("/project")
    BaseResponse<GetMessageResponseDto<ProjectMessageDto>> getProjectMessage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam Long projectId,
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        return BaseResponse.success("Project messages", messageService.getProjectMessages(projectId, page));
    }

    @Operation(summary = "Get Unread Messages")
    @GetMapping("/unread")
    BaseResponse<GetMessageResponseDto<Object>> getUnreadMessage(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var responseDto = messageService.getUnreadMessages(userDetails.getUser());
        return BaseResponse.success("Unread message", responseDto);
    }

    @Operation(summary = "Send System Message")
    @PostMapping(path = "/system")
    public BaseResponse<SystemMessage> sendSystemMessage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid SendMessageRequestDto requestDto) {
        var message = messageService.sendSystemMessage(userDetails.getUser(), requestDto);
        return BaseResponse.success("Send success", message);
    }

    @Operation(summary = "Send Global Message")
    @PostMapping(path = "/global")
    BaseResponse<GlobalMessage> sendGlobalMessage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid SendMessageRequestDto requestDto) {
        var message = messageService.sendGlobalMessage(userDetails.getUser(), requestDto);
        return BaseResponse.success("Send success", message);
    }

    @Operation(summary = "Send Private Message")
    @PostMapping(path = "/private")
    BaseResponse<PrivateMessage> sendPrivateMessage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid SendMessageRequestDto requestDto,
            @RequestParam Long recipientId) {
        var message = messageService.sendPrivateMessage(userDetails.getUser(), recipientId, requestDto);
        return BaseResponse.success("Send success", message);
    }

    @Operation(summary = "Send Project Message")
    @PostMapping(path = "/project")
    public BaseResponse<ProjectMessage> sendProjectMessage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid SendMessageRequestDto requestDto,
            @RequestParam Long projectId) {
        var message = messageService.sendProjectMessage(projectId, userDetails.getUser(), requestDto);
        return BaseResponse.success("Send success", message);
    }

    @Operation(summary = "Get attachment's download url")
    @GetMapping(path = "/attachment")
    public BaseResponse<Object> getDownloadUrl(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam long messageId,
            @RequestParam long attachmentId,
            @RequestParam MessageType messageType) {
        String url = messageService.getDownloadUrl(userDetails.getUser(),
                new DownloadAttachmentDto(messageId, messageType, attachmentId));
        return BaseResponse.success("Download url", url);
    }

    @Operation(summary = "Update attachment list")
    @PutMapping(path = "/attachment")
    public BaseResponse<Object> updateMessageAttachments(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid UpdateAttachmentsDto requestDto) {
        var result = messageService.updateAttachments(userDetails.getUser(), requestDto);
        return BaseResponse.success("Update success", result);
    }
}
