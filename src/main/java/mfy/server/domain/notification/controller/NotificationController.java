package mfy.server.domain.notification.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.domain.notification.dto.NotificationRequestDto.UpdateNotificationRequestDto;
import mfy.server.domain.notification.dto.NotificationResponseDto.GetNotificationResponseDto;
import mfy.server.domain.notification.service.NotificationService;
import mfy.server.global.dto.BaseResponse;
import mfy.server.global.security.UserDetailsImpl;

@Slf4j
@RequiredArgsConstructor
@Validated
@Tag(name = "notification", description = "Notification Related API")
@RequestMapping("/api/v1/notification")
@RestController
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get notification by page")
    @GetMapping("/all")
    BaseResponse<GetNotificationResponseDto> getAll(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var responseDto = notificationService.getPage(userDetails.getUser(), page);
        return BaseResponse.success("Notifications", responseDto);
    }

    @Operation(summary = "Delete all notifications")
    @DeleteMapping("/all")
    BaseResponse<Object> deleteAll(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        notificationService.deleteByUser(userDetails.getUser());
        return BaseResponse.success("Deleted");
    }

    @Operation(summary = "Mark notifications as read")
    @PutMapping("/update/read")
    BaseResponse<Object> updateRead(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid UpdateNotificationRequestDto requestDto) {
        notificationService.updateRead(userDetails.getUser(), requestDto);
        return BaseResponse.success("Update read");
    }

}
