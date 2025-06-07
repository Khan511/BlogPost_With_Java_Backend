package com.example.demo.exception.accessDeniedAndEntryPointExceptions;

import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import com.example.demo.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final GlobalExceptionHandler exceptionHandler;

    public CustomAccessDeniedHandler(GlobalExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        WebRequest webRequest = new ServletWebRequest(request);

        ProblemDetail pd = exceptionHandler
                .handldAccessDenied(new AccessDeniedException(accessDeniedException.getMessage()), webRequest);

        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setStatus(pd.getStatus());
        new ObjectMapper().writeValue(response.getWriter(), pd);
    }

}
