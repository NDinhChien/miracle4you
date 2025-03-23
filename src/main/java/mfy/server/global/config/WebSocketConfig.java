package mfy.server.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import mfy.server.global.websocket.WebSocketInterceptor;
import mfy.server.global.websocket.exception.CustomStompExceptionHandler;
import mfy.server.global.websocket.principal.CustomHandshakeHandler;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketInterceptor webSocketInterceptor;
    private final CustomStompExceptionHandler webSocketExceptionHandler;

    public WebSocketConfig(WebSocketInterceptor webSocketInterceptor, CustomStompExceptionHandler webSocketExceptionHandler) {
        this.webSocketInterceptor = webSocketInterceptor;
        this.webSocketExceptionHandler = webSocketExceptionHandler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry
                .setErrorHandler(webSocketExceptionHandler)
                .addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new CustomHandshakeHandler())
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/queue"); 
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketInterceptor);
    }
}