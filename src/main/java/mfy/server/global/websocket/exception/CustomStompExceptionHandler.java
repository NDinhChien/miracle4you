package mfy.server.global.websocket.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mfy.server.global.dto.BaseResponse;
import mfy.server.global.exception.ErrorConfig.ErrorCode;
import mfy.server.global.exception.ErrorConfig.ErrorMessage;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j(topic = "CustomStompExceptionHandler")
@Component
public class CustomStompExceptionHandler extends StompSubProtocolErrorHandler {
    private static final byte[] EMPTY_PAYLOAD = new byte[0];

    public CustomStompExceptionHandler() {
        super();
    }

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable e) {
        log.error("handleClientMessageProcessingError: {}", e.getMessage());

        Throwable exception = converterThrowException(e);

        if (exception != null) {
            return handleStompException(clientMessage, ErrorMessage.SERVER_ERROR);
        }
        return super.handleClientMessageProcessingError(clientMessage, e);
    }

    private Throwable converterThrowException(final Throwable exception) {
        if (exception instanceof MessageDeliveryException) {
            return exception.getCause();
        }
        return exception;
    }

    private Message<byte[]> handleStompException(Message<byte[]> clientMessage, String errorMessage) {
        // SET response Dto
        BaseResponse<?> errorResponse = BaseResponse.error(ErrorCode.STOMP_EXCEPTION, errorMessage);

        // SET Header
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setLeaveMutable(true);
        accessor.setContentType(MimeTypeUtils.APPLICATION_JSON);

        setReceiptIdForClient(clientMessage, accessor);

        // BINDING toJSONString
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = null;
        try {
            responseJSON = objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return MessageBuilder.createMessage(
                responseJSON != null ? responseJSON.getBytes(StandardCharsets.UTF_8) : EMPTY_PAYLOAD,
                accessor.getMessageHeaders());
    }

    private void setReceiptIdForClient(final Message<byte[]> clientMessage, final StompHeaderAccessor accessor) {

        if (Objects.isNull(clientMessage)) {
            return;
        }

        final StompHeaderAccessor clientHeaderAccessor = MessageHeaderAccessor.getAccessor(clientMessage,
                StompHeaderAccessor.class);

        final String receiptId = Objects.isNull(clientHeaderAccessor) ? null : clientHeaderAccessor.getReceipt();

        if (receiptId != null) {
            accessor.setReceiptId(receiptId);
        }
    }

}