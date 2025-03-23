package mfy.server.global.util;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import mfy.server.global.dto.BaseResponse;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
public class ResponseUtil {

    public static void success(HttpServletResponse response, Object dto) {
        try {
            ObjectMapper om = new ObjectMapper();
            BaseResponse<?> responseDto = BaseResponse.success("Success", dto);
            String responseBody = om.writeValueAsString(responseDto);

            response.setContentType("application/json; charset=utf-8");
            response.setStatus(HttpStatus.OK.value());
            response.getWriter().print(responseBody);
        } catch (Exception e) {
            log.error("Failed to send response {}", e.getMessage());
        }
    }

    public static void fail(HttpServletResponse response, HttpStatus httpStatus, String message) {
        try {
            ObjectMapper om = new ObjectMapper();
            BaseResponse<?> responseDto = BaseResponse.error(httpStatus, message);
            String responseBody = om.writeValueAsString(responseDto);

            response.setContentType("application/json; charset=utf-8");
            response.setStatus(httpStatus.value());
            response.getWriter().print(responseBody);
        } catch (Exception e) {
            log.error("Failed to send response {}", e.getMessage());
        }
    }
}
