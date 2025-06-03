package com.preeti.sansarcart.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.preeti.sansarcart.exception.custom.ValidationException;
import com.preeti.sansarcart.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.FeatureDescriptor;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import static com.preeti.sansarcart.common.I18n.i18n;

@Slf4j
public class Util {
    private static final ObjectMapper mapper = new ObjectMapper();
    public static void validateEmail(String email) {
        // change this regex
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ValidationException(i18n("validation.email.invalid"));
        }
    }

    public static <T> void applyIfPresent(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    // custom error response for filter
    public static void sendErrorResponse(HttpServletResponse response, int status, String messageKey){
        String localizedMessage = i18n(messageKey);
        ApiResponse<Void> errorResponse = ApiResponse.error(localizedMessage, null);

        try {
            response.setStatus(status);
            response.setContentType("application/json");
            String json = new ObjectMapper().writeValueAsString(errorResponse);
            response.getWriter().write(json);
        } catch (IOException e) {
            log.error("Failed to send error response: {}" ,e.getMessage());
        }
    }

    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        return Arrays.stream(src.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(name -> src.getPropertyValue(name) == null)
                .toArray(String[]::new);
    }

    public static String sanitize(String input) {
        return input == null ? null : input.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    public static JsonNode sanitizeJsonNode(JsonNode metaData) {
        if (metaData == null || !metaData.isObject()) return metaData;

        Map<String, JsonNode> sanitizedMap = new TreeMap<>();

        metaData.fieldNames().forEachRemaining(field -> {
            String cleanKey = sanitize(field);
            JsonNode valueNode = metaData.get(field);

            if (valueNode != null && valueNode.isTextual()) {
                sanitizedMap.put(cleanKey, mapper.getNodeFactory().textNode(sanitize(valueNode.asText())));
            } else {
                sanitizedMap.put(cleanKey, valueNode);
            }
        });

        ObjectNode orderedSanitized = mapper.createObjectNode();
        sanitizedMap.forEach(orderedSanitized::set);
        return orderedSanitized;
    }


    public static void validateRequestStrings(String... args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException(i18n("validation.string.invalid"));
        }
        final String VALID_PATTERN = "^(?=.*[a-zA-Z])[a-zA-Z0-9\\s\\-_,.()]+$";
        for (String str : args) {
            if (str == null || str.trim().isEmpty() || !str.matches(VALID_PATTERN)) {
                throw new IllegalArgumentException(i18n("validation.string.invalid"));
            }
        }
    }

}
