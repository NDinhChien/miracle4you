package mfy.server.domain.user.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.domain.user.dto.UserRequestDto.LoginRequestDto;
import mfy.server.domain.user.dto.UserRequestDto.RefreshTokenDto;
import mfy.server.domain.user.dto.UserRequestDto.RegisterRequestDto;
import mfy.server.domain.user.dto.UserRequestDto.SendEmailDto;
import mfy.server.domain.user.dto.UserRequestDto.ResetPasswordDto;
import mfy.server.domain.user.dto.UserRequestDto.UpdatePasswordDto;
import mfy.server.domain.user.dto.UserResponseDto.TokenResponseDto;
import mfy.server.domain.user.dto.UserResponseDto.UserPublicDto;
import mfy.server.domain.user.entity.User;
import mfy.server.domain.user.service.AuthService;
import mfy.server.domain.user.strategy.GoogleLoginStrategy;
import mfy.server.global.dto.BaseResponse;
import mfy.server.global.security.UserDetailsImpl;

@Slf4j
@RequiredArgsConstructor
@Validated
@Tag(name = "auth", description = "Auth Related API")
@RequestMapping("/api/v1/auth")
@RestController
public class AuthController {

    private final AuthService authService;
    private final GoogleLoginStrategy googleLoginStrategy;

    @Value("${client.url}")
    private String clientUrl;

    @Operation(summary = "Check Auth User")
    @GetMapping("/check")
    public BaseResponse<UserPublicDto> checkAuth(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User currentUser = userDetails.getUser();
        return BaseResponse.success("User basic info", UserPublicDto.fromEntity(currentUser));
    }

    @Operation(summary = "Login User")
    @PostMapping("/login")
    public BaseResponse<TokenResponseDto> login(
            @RequestBody @Valid LoginRequestDto requestDto,
            HttpServletResponse response) {

        TokenResponseDto responseDto = authService.login(requestDto, response);
        return BaseResponse.success("Login success", responseDto);

    }

    @Operation(summary = "Register Account")
    @PostMapping("/register")
    public BaseResponse<UserPublicDto> register(
            @RequestBody @Valid RegisterRequestDto requestDto) {
        User registeredUser = authService.register(requestDto);
        return BaseResponse.success("Register success", UserPublicDto.fromEntity(registeredUser));
    }

    @Operation(summary = "Verify Account")
    @GetMapping("/verify")
    public BaseResponse<Object> verify(
            @RequestParam String token) {
        authService.verifyAccount(token);
        return BaseResponse.success("Account verified", null);
    }

    @Operation(summary = "Reset Password")
    @PostMapping("/password/reset")
    public BaseResponse<Object> resetPassword(
            @RequestBody @Valid ResetPasswordDto requestDto) {
        authService.resetPassword(requestDto);
        return BaseResponse.success("Password reset");
    }

    @Operation(summary = "Update Password")
    @PutMapping("/password")
    public BaseResponse<Object> updatePassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid UpdatePasswordDto requestDto) {
        authService.updatePassword(userDetails.getUser(), requestDto);
        return BaseResponse.success("Password updated");
    }

    @Operation(summary = "Refresh Access Token")
    @PutMapping("/refresh")
    public BaseResponse<TokenResponseDto> refreshToken(
            HttpServletResponse response,
            @RequestBody @Valid RefreshTokenDto requestDto) {
        var responseDto = authService.refresh(requestDto, response);
        return BaseResponse.success("Token reissued", responseDto);
    }

    @Operation(summary = "Request Password Reset")
    @PostMapping("/send-password-reset")
    public BaseResponse<Object> requestPasswordRequest(
            @RequestBody @Valid SendEmailDto requestDto) {
        authService.sendPasswordReset(requestDto.getEmail());
        return BaseResponse.success("Please check your mailbox to proceed.");
    }

    @Operation(summary = "Request Account Verification")
    @PostMapping("/send-account-verification")
    public BaseResponse<Object> sendAccountVerification(
            @RequestBody @Valid SendEmailDto requestDto) {
        authService.sendAccountVerification(requestDto.getEmail());
        return BaseResponse.success("Please check your mailbox to proceed.");
    }

    @Operation(summary = "Logout User")
    @DeleteMapping("/logout")
    public BaseResponse<Object> logout(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request,
            HttpServletResponse response) {
        authService.logout(userDetails.getUser(), response);
        return BaseResponse.success("Logged out");
    }

    @Operation(summary = "Google login callback")
    @GetMapping("/google/callback")
    public ModelAndView googleLogin(
            @RequestParam String code,
            HttpServletResponse response,
            ModelMap model) throws JsonProcessingException {
        TokenResponseDto responseDto = googleLoginStrategy.socialLogin(code, response);
        String redirectUrl = String.format("%s/google/callback", clientUrl);
        model.addAttribute("accessToken", responseDto.getAccessToken());
        model.addAttribute("refreshToken", responseDto.getRefreshToken());
        return new ModelAndView("redirect:" + redirectUrl, model);
    }

}
