package mfy.server.global.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.global.exception.BusinessException;

@Slf4j
@RequiredArgsConstructor
@Component
public class ValidatorService {
    private final Validator validator;

    public void validate(Object o) throws BusinessException {
        Map<String, String> errorMap = new HashMap<>();
        Set<ConstraintViolation<Object>> violations = validator.validate(o);

        if (!violations.isEmpty()) {
            for (ConstraintViolation<Object> violation : violations) {
                errorMap.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new BusinessException(HttpStatus.BAD_REQUEST, errorMap);
        }
    }
}
