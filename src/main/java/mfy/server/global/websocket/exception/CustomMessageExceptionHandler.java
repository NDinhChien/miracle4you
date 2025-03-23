package mfy.server.global.websocket.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mfy.server.global.dto.BaseResponse;
import mfy.server.global.exception.AwsS3Exception;
import mfy.server.global.exception.BusinessException;
import mfy.server.global.exception.TokenException;
import mfy.server.global.exception.ErrorConfig.ErrorCode;
import mfy.server.global.exception.ErrorConfig.ErrorMessage;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Slf4j(topic = "CustomMessageExceptionHandler")
@RestControllerAdvice
public class CustomMessageExceptionHandler {
    private final SimpMessageSendingOperations messageTemplate;

    public CustomMessageExceptionHandler(SimpMessageSendingOperations messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    @MessageExceptionHandler(BusinessException.class)
    public void handleBusinessException(BusinessException e, Principal principal) {
        log.error("handleBusinessException: {}", e.getMessage());
        this.customExceptionHandler(principal, e.getCode(), e.getMessage(), e.getData());
    }

    @MessageExceptionHandler(AwsS3Exception.class)
    public void handleAWSS3Exeption(AwsS3Exception e, Principal principal) {
        log.error("handleAWSS3Exeption: {}", e.getMessage());
        this.customExceptionHandler(principal, ErrorCode.S3_EXCEPTION, e.getMessage(), null);
    }

    @MessageExceptionHandler(TokenException.class)
    public void handleTokenExeption(TokenException e, Principal principal) {
        log.error("handleTokenExeption: {}", e.getMessage());
        this.customExceptionHandler(principal, ErrorCode.TOKEN_EXCEPTION, e.getMessage(), null);
    }

    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    public void handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, Principal principal) throws JsonProcessingException {
        Map<String, String> errorMap = new HashMap<>();
        BindingResult result = e.getBindingResult();

        log.error("handleMethodArgumentNotValidException: {}", e.getMessage());
        for (FieldError error : result.getFieldErrors()) {
            log.error("name: {}, message: {}", error.getField(), error.getDefaultMessage());
            errorMap.put(error.getField(), error.getDefaultMessage());
        }
        this.customExceptionHandler(principal, HttpStatus.BAD_REQUEST.value(), e.getMessage(), errorMap);
    }

    @MessageExceptionHandler(RuntimeException.class)
    public void handleRuntimeException(RuntimeException e, Principal principal)
            throws JsonProcessingException {

        log.error("handleRuntimeException: {}", e);
        this.customExceptionHandler(principal, HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorMessage.SERVER_ERROR,
                null);
    }

    private void customExceptionHandler(Principal principal, int code, String message, Object data) {
        try {
            BaseResponse<?> errorResponse = BaseResponse.error(code, message, data);

            ObjectMapper objectMapper = new ObjectMapper();
            String responseJson = objectMapper.writeValueAsString(errorResponse);
            messageTemplate.convertAndSendToUser(principal.getName(), "/queue/error", responseJson);
        } catch (Exception e) {
            log.error("Failed to send error: {} ", e.getMessage());
        }
    }

}
