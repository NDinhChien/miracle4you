package mfy.server.global.auth;

import mfy.server.domain.user.dto.UserResponseDto.TokenResponseDto;
import mfy.server.domain.user.entity.User;
import mfy.server.domain.user.repository.UserRepository;
import mfy.server.global.exception.BusinessException;
import mfy.server.global.exception.ErrorConfig.ErrorMessage;
import mfy.server.global.exception.TokenException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESH_TOKEN_HEADER = "Refresh-Token";
    public static final String BEARER_PREFIX = "Bearer ";

    private static final long ACCESS_TOKEN_TIME = 24 * 60 * 60 * 1000L;
    private static final long REFRESH_TOKEN_TIME = 7 * 24 * 60 * 60 * 1000L;
    private static final long RESET_TOKEN_TIME = 15 * 60 * 1000L;
    private static final long VERIFY_TOKEN_TIME = 15 * 60 * 1000L;

    @Autowired
    private Environment environment;

    @Value("${security.jwt.secret}")
    private String secretKey;

    private final UserRepository userRepository;

    private SecretKey getDynamicSecretKey(User user) {
        String secret = this.secretKey + user.getTokenSecret();
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public User validateToken(String token, String desireType) throws BusinessException, TokenException {
        UserInfo userInfo = decodeJwtPayload(token);
        if (!userInfo.getType().equals(desireType)) {
            throw new TokenException(String.format("Invalid %s token", desireType));
        }

        String userEmail = userInfo.getEmail();
        User user = validateUser(userEmail);
        validateToken(token, desireType, user);
        return user;
    }

    private void validateToken(String token, String desireType, User user) throws TokenException {
        try {
            Jwts.parser()
                    .verifyWith(getDynamicSecretKey(user))
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new TokenException(String.format("Expired %s token", desireType));
        } catch (Exception e) {
            throw new TokenException(String.format("Invalid %s token", desireType));
        }
    }

    private UserInfo decodeJwtPayload(String jwtToken) throws TokenException {
        try {
            String[] parts = jwtToken.split("\\.");
            if (parts.length != 3) {
                throw new TokenException("Invalid JWT Format");
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode payload = new ObjectMapper().readTree(payloadJson);
            String type = payload.get("type").asText();
            String email = payload.get("email").asText();
            String role = payload.get("role").asText();
            return new UserInfo(type, email, role);

        } catch (Exception e) {
            log.info("Failed to decode token: {}", e.getMessage());
            throw new TokenException("Invalid JWT Token");
        }
    }

    public String createAccessToken(User user) {
        return createToken(user, "access");
    }

    public String createRefreshToken(User user) {
        return createToken(user, "refresh");
    }

    public String createResetToken(User user) {
        return createToken(user, "reset");
    }

    public String createVerifyToken(User user) {
        return createToken(user, "verify");
    }

    public String createToken(User user, String type) {
        long duration = 0;
        switch (type) {
            case "access":
                duration = ACCESS_TOKEN_TIME;
                break;
            case "refresh":
                duration = REFRESH_TOKEN_TIME;
                break;
            case "reset":
                duration = RESET_TOKEN_TIME;
                break;
            case "verify":
                duration = VERIFY_TOKEN_TIME;
                break;
            default:
                log.error("Invalid token type {}", type);
                break;
        }

        return Jwts.builder()
                .claim("type", type)
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + duration))
                .signWith(getDynamicSecretKey(user))
                .compact();
    }

    @Transactional
    public TokenResponseDto addTokenToResponse(User user, HttpServletResponse response) {
        user.updateLoginSucces();
        user = userRepository.save(user);

        String accessToken = createAccessToken(user);
        String refreshToken = createRefreshToken(user);

        addAccessTokenToCookie(accessToken, response);
        return new TokenResponseDto(accessToken, refreshToken);
    }

    public String getAccessTokenFromRequest(HttpServletRequest request) {
        String token = getAccessTokenFromHeader(request);
        if (!StringUtils.hasText(token)) {
            token = getAccesTokenFromCookie(request);
        }
        return token;
    }

    public void addAccessTokenToCookie(String token, HttpServletResponse response) {
        token = URLEncoder.encode(token, StandardCharsets.UTF_8);

        boolean isProd = isProdEnv();
        Cookie cookie = new Cookie(AUTHORIZATION_HEADER, token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", isProd ? "None" : "Lax");
        cookie.setMaxAge((int) (ACCESS_TOKEN_TIME / 1000));
        cookie.setSecure(isProd);
        response.addCookie(cookie);
    }

    public void removeTokenFromResponse(User user, HttpServletResponse response) {
        user.updateLogout();
        userRepository.save(user);
        removeAccessTokenFromCookie(response);
    }

    public void removeAccessTokenFromCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(AUTHORIZATION_HEADER, null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String getAccesTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(AUTHORIZATION_HEADER)) {
                    return URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
                }
            }
        }
        return null;
    }

    public String getAccessTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        return getTokenFromBearer(bearerToken);
    }

    public String getRefreshTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(REFRESH_TOKEN_HEADER);
        return getTokenFromBearer(bearerToken);
    }

    public String getTokenFromBearer(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    public User validateUser(String email) throws BusinessException {
        return userRepository.findByEmail(email).orElseThrow(
                () -> {
                    return new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.USER_NOT_FOUND);
                });
    }

    public boolean isProdEnv() {
        return environment.getActiveProfiles()[0] == "prod";
    }
}