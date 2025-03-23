package mfy.server.domain.user.strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.domain.user.dto.UserResponseDto.TokenResponseDto;
import mfy.server.global.auth.SociaInfo;
import mfy.server.domain.user.entity.User;
import mfy.server.domain.user.entity.type.Role;
import mfy.server.domain.user.repository.UserRepository;
import mfy.server.global.auth.TokenProvider;
import mfy.server.global.exception.BusinessException;
import mfy.server.global.exception.ErrorConfig.ErrorMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleLoginStrategy implements SocialLoginStrategy {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final TokenProvider tokenProvider;

    @Value("${google.client-id}")
    private String googleClientId;
    @Value("${google.client-secret}")
    private String googleClientSecret;
    @Value("${google.redirect-uri}")
    private String googleRedirectUri;

    @Transactional
    @Override
    public TokenResponseDto socialLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        try {
            String token = getToken(code);
            SociaInfo userInfoDto = getSocialInfo(token);
            User googleUser = registerUserIfNeeded(userInfoDto);
            return this.tokenProvider.addTokenToResponse(googleUser, response);
        } catch (Exception e) {
            log.info("Failed to do social login ", e);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.SERVER_ERROR);
        }
    }

    @Override
    public String getToken(String code) throws JsonProcessingException {
        URI uri = UriComponentsBuilder
                .fromUriString("https://oauth2.googleapis.com/token")
                .encode()
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", googleClientId);
        body.add("client_secret", googleClientSecret);
        body.add("redirect_uri", googleRedirectUri);
        body.add("code", code);

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(uri)
                .headers(headers)
                .body(body);

        ResponseEntity<String> response = restTemplate.exchange(
                requestEntity,
                String.class);

        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        return jsonNode.get("access_token").asText();
    }

    @Override
    public SociaInfo getSocialInfo(String token) throws JsonProcessingException {
        URI uri = UriComponentsBuilder
                .fromUriString("https://www.googleapis.com/oauth2/v2/userinfo")
                .queryParam("access_token", token)
                .encode()
                .build()
                .toUri();

        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        String id = jsonNode.get("id").asText();
        String email = jsonNode.get("email").asText();
        String avatar = jsonNode.get("picture").asText();

        return new SociaInfo(id, email, avatar);
    }

    @Override
    public User registerUserIfNeeded(SociaInfo userInfoDto) {
        String googleId = userInfoDto.getId();
        User googleUser = userRepository.findByGoogleId(googleId).orElse(null);

        if (googleUser == null) {
            String googleEmail = userInfoDto.getEmail();
            User sameEmailUser = userRepository.findByEmail(googleEmail).orElse(null);

            if (sameEmailUser != null) {
                googleUser = sameEmailUser;
                googleUser = googleUser.updateGoogleId(googleId);
            } else {
                String password = UUID.randomUUID().toString();
                String email = userInfoDto.getEmail();

                Integer maxSequence = userRepository.findMaxNicknameSequence();
                int nextSequenceNumber = maxSequence != null ? maxSequence + 1 : 1;
                String nickname = String.format("User%03d", nextSequenceNumber);

                googleUser = User.builder()
                        .email(email)
                        .nickname(nickname)
                        .password(password)
                        .role(Role.TRANSLATOR)
                        .googleId(googleId)
                        .avatar(userInfoDto.getAvatar())
                        .build();
            }
            return userRepository.save(googleUser);
        }
        return googleUser;
    }

}
