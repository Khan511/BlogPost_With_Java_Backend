
package com.example.demo.security;

import static com.example.demo.constant.Constant.LOGIN_PATH;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

// import java.util.function.Consumer;
import static com.example.demo.utils.RequestUtils.getResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
// import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.dto.LoginrequestDto;
import com.example.demo.enumeration.LoginType;
import com.example.demo.enumeration.TokenType;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.http.HttpStatus.OK;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

//Handles user authentication process and JWT token generation.
//Processes login requests and sets JWT tokens in cookies.
@Slf4j
@Component
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private UserService userService;
    private JwtUtil jwtUtil;
    private ObjectMapper objectMapper;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, UserService userService,
            JwtUtil jwtUtil, ObjectMapper objectMapper) {

        super.setAuthenticationManager(authenticationManager);
        // Change default "/login" endpoint to your custom constant
        setFilterProcessesUrl(LOGIN_PATH);
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) throws IOException {

        String email = (String) request.getAttribute("loginEmail");

        // update loign attempt only if email was captured
        if (email != null) {
            userService.updateLoginAttempt(email, LoginType.LOGIN_FAILURE);
        }

        // Build error response
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timeStapm", LocalDateTime.now().toString());
        errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("error", "Authentication Failed");
        errorResponse.put("message", getErrorResponse(failed));
        errorResponse.put("path", request.getRequestURI());

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    private String getErrorResponse(AuthenticationException exception) {
        if (exception instanceof BadCredentialsException) {
            return "Invalid username or password";
        } else if (exception instanceof DisabledException) {
            return "Account is disabled";
        } else if (exception instanceof LockedException) {
            return "Account is locked";
        } else if (exception instanceof AccountExpiredException) {
            return "Account has expired";
        } else if (exception instanceof CredentialsExpiredException) {
            return "Credentials have expired";
        }
        return "Authenticaiton failed";
    }

    // Attempts authentication by extracting credentials from request body
    // Called when a login request is received (POST to LOGIN_PATH).
    // Reads credentials from the request body and attempts authentication.
    // Jackson's ObjectMapper to convert (deserialize) the JSON body of an HTTP
    // request into a Java object (LoginrequestDto).
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {

            // Parse JSON body into our LoginrequestDto (has email + password)
            // Parse login credentials from JSON request body
            var user = new ObjectMapper().configure(Feature.AUTO_CLOSE_SOURCE, true).readValue(request.getInputStream(),
                    LoginrequestDto.class);

            // Store email in request for use in failure handler
            request.setAttribute("loginEmail", user.getEmail());

            try {
                userService.updateLoginAttempt(user.getEmail(), LoginType.LOGIN_ATTEMPT);
            } catch (Exception e) {
                log.error("Loing attemt update failed for {}", user.getEmail(), e);
            }

            // Create authentication token with email and password
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user.getEmail(),
                    user.getPassword(), new ArrayList<>());

            // Delegate to AuthenticationManager (which will call your UserDetailsService).
            // Authenticate with Spring Security's AuthenticationManager
            return getAuthenticationManager().authenticate(authToken);

        } catch (IOException e) {
            throw new BadCredentialsException("Error authenticatin user" + e);
        }
    }

    // Called when authentication succeeds.
    // Here we generate access + refresh tokens, set them as cookies,
    // and return a JSON response body with user info.
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain, Authentication authResult)
            throws StreamReadException, DatabindException, IOException {
        // Retrieve the authenticated user's details
        var userDetails = (UserDetails) authResult.getPrincipal();

        // Map to your DTO (contains userId, email, roles)
        var user = UserMapper.tLoginResponseDto(userService.getUserByName(userDetails.getUsername()));

        userService.updateLoginAttempt(user.getEmail(), LoginType.LOGIN_SUCCESS);

        // Prepare response with tokens and user data
        var httpResponse = sendResponse(request, response, user);
        // Set response headers and write JSON body
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.OK.value());

        var out = response.getOutputStream();
        // var mapper = new ObjectMapper();
        objectMapper.writeValue(out, httpResponse);
        out.flush();
    }

    // Helper to set both access & refresh token cookies and build
    // a standard JSON response wrapper
    private Object sendResponse(HttpServletRequest request, HttpServletResponse response, LoginResponseDto user) {
        jwtUtil.setTokenCookieInResponse(response, user, TokenType.ACCESS);
        jwtUtil.setTokenCookieInResponse(response, user, TokenType.REFRESH);
        // Build a JSON response (e.g. { status: "Login Success", data: { user: {...} }
        // })
        return getResponse(request, Map.of("user", user), "Login Success", OK);
    }

}
