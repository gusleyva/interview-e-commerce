package com.ecommerce.util;

import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class PatchUtil {

    public static <T> void copyNonNullProperties(T source, T target) {
        Arrays.stream(source.getClass().getDeclaredFields())
                .forEach(field -> {
                    field.setAccessible(true);
                    try {
                        Object value = field.get(source);
                        if (value != null) {
                            field.set(target, value);
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Error copying field: " + field.getName(), e);
                    }
                });
    }
}
