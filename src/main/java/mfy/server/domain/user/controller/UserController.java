package mfy.server.domain.user.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.domain.user.dto.UserResponseDto.UserPublicDto;
import mfy.server.domain.project.dto.ProjectResponseDto.IProjectDto;
import mfy.server.domain.user.dto.UserRequestDto.AddChatItemRequestDto;
import mfy.server.domain.user.dto.UserRequestDto.UpdateRequestDto;
import mfy.server.domain.user.dto.UserResponseDto.IChatItemDto;
import mfy.server.domain.user.dto.UserResponseDto.IUserBasicDto;
import mfy.server.domain.user.dto.UserResponseDto.UserProfileResponseDto;
import mfy.server.domain.user.entity.User;
import mfy.server.domain.user.service.UserService;
import mfy.server.global.dto.BaseResponse;
import mfy.server.global.security.UserDetailsImpl;

@Slf4j
@RequiredArgsConstructor
@Validated
@Tag(name = "user", description = "User Related API")
@RequestMapping("/api/v1/user")
@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    @Qualifier("usersCache")
    private Cache usersCache;

    @Operation(summary = "Get private profile")
    @GetMapping("/profile")
    public BaseResponse<UserProfileResponseDto> getMyProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var responseDto = userService.getProfile(userDetails.getUser().getId());
        return BaseResponse.success("User private profile", responseDto);
    }

    @Operation(summary = "Get user by id")
    @GetMapping("/id/{id}")
    public BaseResponse<IUserBasicDto> getUserProfile(
            @PathVariable Long id) {
        var responseDto = userService.getPublicProfile(id);
        return BaseResponse.success("User public profile", responseDto);
    }

    @Operation(summary = "Update profile")
    @PutMapping(path = "/profile", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public BaseResponse<UserPublicDto> updateUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestPart(name = "profile") UpdateRequestDto requestDto,
            @RequestPart(name = "avatar", required = false) MultipartFile avatar) {
        User updateUser = userService.updateProfile(userDetails.getUser(), requestDto, avatar);
        return BaseResponse.success("User profile updated", UserPublicDto.fromEntity(updateUser));
    }

    @Operation(summary = "Search users")
    @GetMapping("/search")
    public BaseResponse<List<IUserBasicDto>> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        var responseDto = userService.searchUsers(name, page);
        return BaseResponse.success("Search users", responseDto);
    }

    @Operation(summary = "Add private chat item")
    @PostMapping("/item")
    public BaseResponse<Object> addChatItem(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid AddChatItemRequestDto requestDto) {
        boolean result = userService.addChatItem(userDetails.getUser(), requestDto);
        return BaseResponse.success(result == true ? "Added" : "Not added");
    }

    @Operation(summary = "Get private chat items")
    @GetMapping("/item")
    public BaseResponse<List<IChatItemDto>> getChatItems(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var responseDto = userService.getChatItems(userDetails.getUser());
        return BaseResponse.success("Chat items", responseDto);
    }

    @Operation(summary = "Get user projects")
    @GetMapping("/project")
    public BaseResponse<List<IProjectDto>> getProjects(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var responseDto = userService.getProjects(userDetails.getUser());
        return BaseResponse.success("Projects", responseDto);
    }

}
