package mfy.server.global.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonUtil {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public static DateTimeFormatter defaultFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static String formatDate(LocalDateTime date) {
        return formatDate(date, null);
    }

    public static String formatDate(LocalDateTime date, String dateFormat) {
        try {
            DateTimeFormatter formatter = defaultFormatter;
            if (dateFormat != null) {
                formatter = DateTimeFormatter.ofPattern(dateFormat);
            }
            return date.format(formatter);
        } catch (Exception e) {
            log.info("Failed to format date", e.getMessage());
            return null;
        }
    }

    public static LocalDateTime parseDateString(String dateString) {
        return parseDateString(dateString, null);
    }

    public static LocalDateTime parseDateString(String dateString, String dateFormat) {
        try {
            DateTimeFormatter formatter = defaultFormatter;
            if (dateFormat != null) {
                formatter = DateTimeFormatter.ofPattern(dateFormat);
            }
            return LocalDateTime.parse(dateString, formatter);
        } catch (Exception e) {
            log.info("Failed to parse date string: {}", e.getMessage());
            return null;
        }
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateSecureString(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

    public static long toId(String s) {
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return -1;
        }
    }
}
