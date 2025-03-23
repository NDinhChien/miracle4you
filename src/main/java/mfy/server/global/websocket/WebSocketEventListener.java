package mfy.server.global.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.domain.message.service.MessageManager;
import mfy.server.global.security.UserDetailsImpl;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketEventListener {
    private final MessageManager messageManager;

    @EventListener
    public void sessionConnectedEvent(SessionConnectedEvent event) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        var userDetails = getUserDetails(event);
        if (userDetails != null) {
            messageManager.addUser(userDetails.getUser());
        }
        log.info("Session {} ({}) connected", accessor.getSessionId(),
                userDetails == null ? "" : userDetails.getUsername(), accessor.getDestination());
    }

    @EventListener
    public void sessionSubscribeEvent(SessionSubscribeEvent event) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        log.info("Session {} subscribed {}", accessor.getSessionId(), accessor.getDestination());
    }

    @EventListener
    public void sessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        log.info("Session {} unsubscribed {}", accessor.getSessionId(), accessor.getDestination());
    }

    @EventListener
    public void sessionDisconnectEvent(SessionDisconnectEvent event) {
        var userDetails = getUserDetails(event);
        if (userDetails != null) {
            messageManager.removeUser(userDetails.getUser());
        }
        log.info("Session {} ({}) disconnected", event.getSessionId(),
                userDetails == null ? "" : userDetails.getUsername());
    }

    private UserDetailsImpl getUserDetails(AbstractSubProtocolEvent event) {
        try {
            Authentication authentication = (Authentication) event.getMessage().getHeaders().get("simpUser");

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userDetails;
        } catch (Exception e) {
            log.info("Could not find authentication from message header");
            return null;
        }
    }
}