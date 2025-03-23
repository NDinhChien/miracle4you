package mfy.server.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.global.dto.BaseResponse;
import mfy.server.global.util.ResponseUtil;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@NoArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

        @Override
        public void handle(
                        @NotNull HttpServletRequest request,
                        @NotNull HttpServletResponse response,
                        @NotNull AccessDeniedException accessDeniedException) throws IOException {

                ResponseUtil.fail(response, HttpStatus.FORBIDDEN, accessDeniedException.getMessage());
        }
}