package com.example.demo.utils;

import java.util.Map;
import java.time.LocalDateTime;
import com.example.demo.domain.Response;
import org.springframework.http.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

public class RequestUtils {

    // Method to create a Response object based on the given request, data, message,
    // and HTTP status.
    // This is used to construct a standardized response structure for the client.
    public static Response getResponse(HttpServletRequest request, Map<?, ?> data, String message, HttpStatus status) {

        return new Response(LocalDateTime.now().toString(), status.value(), request.getRequestURI(),
                HttpStatus.valueOf(status.value()), message, StringUtils.EMPTY, data);
    }

}
