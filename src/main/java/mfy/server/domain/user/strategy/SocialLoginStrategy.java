package mfy.server.domain.user.strategy;

import mfy.server.global.auth.SociaInfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletResponse;
import mfy.server.domain.user.dto.UserResponseDto.TokenResponseDto;
import mfy.server.domain.user.entity.User;

public interface SocialLoginStrategy {

    TokenResponseDto socialLogin(String code, HttpServletResponse response) throws JsonProcessingException;

    String getToken(String code) throws JsonProcessingException;

    SociaInfo getSocialInfo(String token) throws JsonProcessingException;

    User registerUserIfNeeded(SociaInfo userInfoDto);
}
