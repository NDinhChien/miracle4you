package mfy.server.global.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import mfy.server.global.dto.BaseResponse;
import mfy.server.global.exception.ErrorConfig.ErrorCode;
import mfy.server.global.exception.ErrorConfig.ErrorMessage;

@Slf4j(topic = "CustomExceptionHandler")
@ResponseStatus(HttpStatus.BAD_REQUEST)
@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> handleBusinessException(BusinessException e) {
        log.error("handleBusinessException: {}", e.getMessage());
        return BaseResponse.error(e.getCode(), e.getMessage(), e.getData());
    }

    @ExceptionHandler(TokenException.class)
    public BaseResponse<?> handleTokenException(HttpServletRequest request, TokenException e) {
        log.error("handleTokenException: {}", e.getMessage());
        return BaseResponse.error(ErrorCode.TOKEN_EXCEPTION, e.getMessage());
    }

    @ExceptionHandler(AwsS3Exception.class)
    public BaseResponse<?> handleAWSS3Exeption(HttpServletRequest request, AwsS3Exception e) {
        log.error("handleAWSS3Exeption: {}", e.getMessage());
        return BaseResponse.error(ErrorCode.S3_EXCEPTION, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> handleMethodArgumentNotValidException(HttpServletRequest request,
            MethodArgumentNotValidException e) {
        Map<String, String> errorMap = new HashMap<>();
        BindingResult result = e.getBindingResult();

        for (FieldError error : result.getFieldErrors()) {
            errorMap.put(error.getField(), error.getDefaultMessage());
        }

        log.error("handleMethodArgumentNotValidException: {}", e.getMessage());
        return BaseResponse.error(HttpStatus.BAD_REQUEST, errorMap);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> handleRuntimeException(RuntimeException e) {
        log.error("handleRuntimeException", e);
        return BaseResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.SERVER_ERROR);
    }

}
