package mfy.server.global.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.domain.user.entity.User;
import mfy.server.global.exception.ErrorConfig.ErrorMessage;
import mfy.server.global.exception.TokenException;
import mfy.server.global.auth.TokenProvider;
import mfy.server.global.security.UserDetailsImpl;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketInterceptor implements ChannelInterceptor {
    private final TokenProvider tokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            try {
                setAuthenticate(accessor);
                log.info("New auth connection");
            } catch (Exception e) {
                log.info("New unauth connection.");
            }
        }
        return message;
    }

    private void setAuthenticate(final StompHeaderAccessor accessor) {
        String bearerToken = accessor.getFirstNativeHeader(TokenProvider.AUTHORIZATION_HEADER);

        String accessToken = tokenProvider.getTokenFromBearer(bearerToken);

        if (!StringUtils.hasText(accessToken)) {
            throw new TokenException(ErrorMessage.ACCESS_TOKEN_NOT_FOUND);
        }

        User user = tokenProvider.validateToken(accessToken, "access");

        Authentication authentication = createAuthentication(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        accessor.setUser(authentication);
    }

    private Authentication createAuthentication(User user) {
        final UserDetails userDetails = new UserDetailsImpl(user);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

}