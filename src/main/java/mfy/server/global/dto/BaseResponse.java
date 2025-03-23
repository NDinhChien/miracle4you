package mfy.server.global.dto;

import lombok.Data;

import org.springframework.http.HttpStatus;


@Data
public class BaseResponse<T> {

    private int code;

    private String message;

    private T data;

    public BaseResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BaseResponse(int code, String message) {
        this(code, message, null);
    }

    public static BaseResponse<?> success() {
        return new BaseResponse<>(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase());
    }
    
    public static BaseResponse<Object> success(String message) {
        return new BaseResponse<>(HttpStatus.OK.value(), message);
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), data);
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(HttpStatus.OK.value(), message, data);
    }
    
    public static BaseResponse<?> error(HttpStatus status) {
        return new BaseResponse<>(status.value(), status.getReasonPhrase());
    }

    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, message);
    }

    public static BaseResponse<?> error(HttpStatus status, String message) {
        return new BaseResponse<>(status.value(), message);
    }

    public static <T> BaseResponse<T> error(HttpStatus status, T data) {
        return new BaseResponse<>(status.value(), status.getReasonPhrase(),  data);
    }

    public static <T> BaseResponse<T> error(int code, String message, T data) {
        return new BaseResponse<>(code, message, data);
    }

    public static <T> BaseResponse<T> error(HttpStatus status, String message, T data) {
        return new BaseResponse<>(status.value(), message,  data);
    }
}
