package mfy.server.domain.user.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.domain.user.dto.UserRequestDto.LoginRequestDto;
import mfy.server.domain.user.dto.UserRequestDto.RefreshTokenDto;
import mfy.server.domain.user.dto.UserRequestDto.RegisterRequestDto;
import mfy.server.domain.user.dto.UserRequestDto.ResetPasswordDto;
import mfy.server.domain.user.dto.UserRequestDto.UpdatePasswordDto;
import mfy.server.domain.user.dto.UserResponseDto.TokenResponseDto;
import mfy.server.domain.user.entity.User;
import mfy.server.domain.user.repository.UserRepository;
import mfy.server.global.exception.BusinessException;
import mfy.server.global.exception.ErrorConfig.ErrorMessage;
import mfy.server.global.util.CommonUtil;
import mfy.server.global.util.ResponseUtil;
import mfy.server.global.auth.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    public static final int MAX_LOGIN_ATTEMPT = 5;
    public static final int RESET_LOGIN_AFTER_MINUS = 120;
    public static final int EMAIL_SEND_DISTANCE_MINUS = 3;

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${server.config.verification.required}")
    private boolean isEmailVerificationRequired;
    @Value("${server.config.verification.html}")
    private boolean isHtmlTemplateEnabled;

    @Transactional
    public User register(RegisterRequestDto requestDto) {
        String password = requestDto.getPassword();
        String email = requestDto.getEmail();
        String nickname = requestDto.getNickname();

        if (!password.equals(requestDto.getConfirmPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.PASSWORD_MISMATCH);
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.EMAIL_REGISTERED);
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.NICKNAME_EXISTS);
        }
        User user = new User(nickname, email, password);
        if (isEmailVerificationRequired) {
            sendAccountVerification(user);
        } else {
            user.updateIsVerified();
        }
        return userRepository.save(user);
    }

    public TokenResponseDto refresh(RefreshTokenDto requestDto, HttpServletResponse response) {
        String accessToken = requestDto.getAccessToken();
        String refreshToken = requestDto.getRefreshToken();

        User user = tokenProvider.validateToken(accessToken, "access");
        User refreshUser = tokenProvider.validateToken(refreshToken, "refresh");

        if (!refreshUser.getEmail().equals(user.getEmail())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.TOKEN_MISMATCH);
        }
        return tokenProvider.addTokenToResponse(user, response);
    }

    public TokenResponseDto login(LoginRequestDto requestDto, HttpServletResponse response) {
        String email = requestDto.getEmail();
        String password = requestDto.getPassword();

        User user = validateUser(email);

        if (!user.getIsVerified()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ErrorMessage.UNVERIFIED_ACCOUNT);
        }

        if (!user.canDoLogin()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, String.format("You can login again after %s",
                    CommonUtil.formatDate(user.getLastLoginFailureAt().plusMinutes(RESET_LOGIN_AFTER_MINUS))));
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            user.updateLoginFailure();
            userRepository.save(user);
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.INCORRECT_PASSWORD);
        }

        return tokenProvider.addTokenToResponse(user, response);

    }

    public void logout(User user, HttpServletResponse response) {
        tokenProvider.removeTokenFromResponse(user, response);
    }

    public void updatePassword(User user, UpdatePasswordDto requestDto) {
        String currentPassword = requestDto.getCurrentPassword();
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.INCORRECT_PASSWORD);
        }

        if (currentPassword.equals(requestDto.getNewPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.NEW_PASSWORD_UNSECURED);
        }
        updateNewPassword(user, requestDto.getNewPassword(), requestDto.getConfirmNewPassword());
    }

    public void resetPassword(ResetPasswordDto requestDto) {
        String resetToken = requestDto.getToken();
        User user = tokenProvider.validateToken(resetToken, "reset");
        updateNewPassword(user, requestDto.getNewPassword(), requestDto.getConfirmNewPassword());
    }

    private void updateNewPassword(User user, String newPassword, String confirmNewPassword) {
        if (!newPassword.equals(confirmNewPassword)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.PASSWORD_MISMATCH);
        }
        user.updatePassword(newPassword);
        userRepository.save(user);
    }

    public void verifyAccount(String token) {
        User user = tokenProvider.validateToken(token, "verify");
        if (user.getIsVerified()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.ACCOUNT_ALREADY_VERIFIED);
        }
        user.updateIsVerified();
        userRepository.save(user);
    }

    public void sendPasswordReset(String email) {
        User user = validateUser(email);
        if (user.canSendEmail()) {
            sendPasswordReset(user);
        } else {
            throw new BusinessException(HttpStatus.FORBIDDEN, ErrorMessage.SEND_EMAIL_LATER);
        }
    }

    public void sendAccountVerification(String email) {
        User user = validateUser(email);
        if (user.getIsVerified()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.ACCOUNT_ALREADY_VERIFIED);
        }
        if (user.canSendEmail()) {
            sendAccountVerification(user);
        } else {
            throw new BusinessException(HttpStatus.FORBIDDEN, ErrorMessage.SEND_EMAIL_LATER);
        }
    }

    private void sendAccountVerification(User user) {
        String verificationToken = tokenProvider.createVerifyToken(user);
        log.info("Verification token: {}", verificationToken);

        emailService.sendAccountVerification(user, verificationToken, isHtmlTemplateEnabled);
        user.updateLastSendEmail();
        userRepository.save(user);
    }

    private void sendPasswordReset(User user) {
        String resetToken = tokenProvider.createResetToken(user);
        log.info("Reset token: {}", resetToken);

        emailService.sendPasswordReset(user, resetToken, isHtmlTemplateEnabled);
        user.updateLastSendEmail();
        userRepository.save(user);
    }

    private User validateUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> {
            return new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.USER_NOT_FOUND);
        });
    }

}
