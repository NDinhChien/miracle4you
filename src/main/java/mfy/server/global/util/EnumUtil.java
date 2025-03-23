package mfy.server.global.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnumUtil {
    public static <T extends Enum<T>> T fromString(Class<T> enumType, String value) {
        try {
            if (value == null || value.isEmpty()) return null;
            return Enum.valueOf(enumType, value.toUpperCase());
        } catch(Exception e) {
            log.info("Failed to convert value={} to enum {}", value, enumType.getName());        
        }
        return null;
    }

    public static <T extends Enum<T>> String toString(T e) {
        if (e == null) return null;
        return e.name();
    }

}
