package mfy.server.domain.user.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import mfy.server.domain.user.entity.type.Gender;

public class UserRequestDto {

    public static final String PASSWORD_MESSAGE = "Password must contain at least one lowercase letter, one uppercase letter, one number and one special character (@$!%*?&)";
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$";
    public static final String NICKNAME_MESSAGE = "Nicknames must contain only letters and numbers";
    public static final String NICKNAME_PATTERN = "^\\w*$";

    public static final String FULLNAME_MESSAGE = "Fullname must contain only alphabetic characters and spaces";
    public static final String FULLNAME_PATTERN = "^[a-zA-Z\\s]*$";

    @Getter
    public static class LoginRequestDto {

        @Email
        @NotBlank
        @Schema(description = "Enter your email.", example = "test@email.com")
        private String email;

        @Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
        @Size(min = 6, max = 50)
        @NotBlank
        @Schema(description = "Enter your password.", example = "Aa123456&")
        private String password;

    }

    @Getter
    public static class RegisterRequestDto {

        @Email
        @NotBlank
        @Schema(description = "An email can only register one acccount", example = "test@email.com")
        private String email;

        @Pattern(regexp = NICKNAME_PATTERN, message = NICKNAME_MESSAGE)
        @Size(min = 4, max = 20)
        @NotBlank
        @Schema(description = "Enter an unique nickname", example = "test")
        private String nickname;

        @Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
        @Size(min = 6, max = 50)
        @NotBlank
        @Schema(description = "Enter password", example = "Aa123456&")
        private String password;

        @NotBlank
        @Schema(description = "Enter password again to confirm", example = "Aa123456&")
        private String confirmPassword;
    }

    @Getter
    public static class UpdateRequestDto {

        @Pattern(regexp = NICKNAME_PATTERN, message = NICKNAME_MESSAGE)
        @Size(min = 4, max = 20)
        @Schema(description = "Nickname could be updated after at least 7 days", example = "dinhchien")
        private String nickname;

        @Pattern(regexp = FULLNAME_PATTERN, message = FULLNAME_MESSAGE)
        @Size(max = 50)
        @Schema(description = "Enter your full name.", example = "Nguyen Dinh Chien")
        private String fullName;

        @Schema(description = "Male or female", example = "male")
        private Gender gender;

        @Size(max = 50)
        @Schema(description = "Diocese where you live.", example = "Xuan Loc")
        private String diocese;

        @Size(max = 50)
        @Schema(description = "Parish where you belong to", example = "Dong Hoa")
        private String parish;

        @Size(max = 200)
        @Schema(description = "Introduce yourself", example = "I am a developer")
        private String bio;

        @Schema(description = "Your date of birth", example = "")
        private Instant birthday;

    }

    @Getter
    public static class ResetPasswordDto {

        @NotBlank
        private String token;

        @Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
        @Size(min = 6, max = 50)
        @NotBlank
        private String newPassword;

        @NotBlank
        private String confirmNewPassword;

    }

    @Getter
    public static class UpdatePasswordDto {

        @Schema(description = "Your current password", example = "Aa123456&")
        @NotBlank
        private String currentPassword;

        @Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
        @Size(min = 6, max = 50)
        @NotBlank
        private String newPassword;

        @NotBlank
        private String confirmNewPassword;

    }

    @Getter
    public static class SendEmailDto {

        @Email
        @NotBlank
        private String email;

    }

    @Getter
    public static class GoogleTokenRequestDto {
        @NotBlank
        private String token;
    }

    @Getter
    public static class RefreshTokenDto {
        @NotBlank
        private String accessToken;

        @NotBlank
        private String refreshToken;
    }

    @Getter
    public static class AddChatItemRequestDto {

        private Long recipientId;

        private Long projectId;
    }
}
