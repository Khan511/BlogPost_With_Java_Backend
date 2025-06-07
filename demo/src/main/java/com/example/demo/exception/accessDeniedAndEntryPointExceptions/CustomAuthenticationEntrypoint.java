package com.example.demo.exception.accessDeniedAndEntryPointExceptions;

import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.customeExceptions.JwtVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationEntrypoint implements AuthenticationEntryPoint {
    private final GlobalExceptionHandler exceptionHandler;

    public CustomAuthenticationEntrypoint(GlobalExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        WebRequest webRequest = new ServletWebRequest(request);
        ProblemDetail pd = exceptionHandler
                .handleJwtException(new JwtVerificationException(authException.getMessage()), webRequest);

        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setStatus(pd.getStatus());

        new ObjectMapper().writeValue(response.getWriter(), pd);
    }

}
