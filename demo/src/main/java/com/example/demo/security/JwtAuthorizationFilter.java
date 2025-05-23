package com.example.demo.security;

import static com.example.demo.enumeration.TokenType.ACCESS;
import java.io.IOException;
import java.util.Objects;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.domain.Token;
import com.example.demo.domain.TokenData;
import com.example.demo.enumeration.TokenType;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklist tokenBlacklist;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        // Skip authorization check for login endpoint
        if ("/api/auth/login".equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            // Extract the access token from the reqeust using the jwtConfiguration
            var accessToken = jwtUtil.getTokenFromRequestCookie(request, TokenType.ACCESS.getValue());

            // Check if accesstoken is black listed
            // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
            if (accessToken.isPresent() && tokenBlacklist.contains(accessToken.get())) {

                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalidated");
                return;
            }
            // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

            // Check if the access token is present and valid
            if (accessToken.isPresent() && jwtUtil.extractTokenData(accessToken.get(), TokenData::isValid)) {

                // if valid, set the authetication context with the user's detials
                SecurityContextHolder.getContext()
                        .setAuthentication(getAuthentication(accessToken.get(), request, response));
            } else {
                // if access token is not present or invalid, try to extract the refresh token.
                var refreshToken = jwtUtil.getTokenFromRequestCookie(request, TokenType.REFRESH.getValue());

                // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
                if (refreshToken.isPresent() && tokenBlacklist.contains(refreshToken.get())) {
                    SecurityContextHolder.clearContext();
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalidated");
                    return;
                }
                // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

                // check if the refresh token is present and valid
                if (refreshToken.isPresent() && jwtUtil.extractTokenData(refreshToken.get(), TokenData::isValid)) {
                    // Extract the user details from the refresh token
                    var user = jwtUtil.extractTokenData(refreshToken.get(), TokenData::getLoginResponseDto);

                    // Add null/roles check here
                    // Security check: Validate user roles exist
                    if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) {
                        SecurityContextHolder.clearContext();
                        filterChain.doFilter(request, response);
                        return;
                    }
                    // 5. Generate new access token using refresh token credentials
                    String newAccessToken = jwtUtil.generateTokenForuser(user, Token::getAccess);
                    // Set authenticaiton only if token is valid
                    if (newAccessToken != null) {
                        // Set new authentication context and update cookies
                        SecurityContextHolder.getContext()
                                .setAuthentication(getAuthentication(newAccessToken, request, response));
                        // Add new access token as a cookie in the response
                        jwtUtil.setTokenCookieInResponse(response, user, ACCESS);
                        filterChain.doFilter(request, response);
                        return;
                    } else {
                        // No valid tokens found - clear security context
                        SecurityContextHolder.clearContext();
                    }
                } else {
                    // if neighter access nor refresh tokens are vallid, clear prevent access
                    SecurityContextHolder.clearContext();
                }
            }
            // Continue the filter chain to allow the request to proceed to
            // the next filter or resource.
            filterChain.doFilter(request, response);
            return;
        } catch (Exception e) {
            // Handle any authentication errors
            throw new RuntimeException("Error Authentication user in subsequent request", e);
        }
    }

    // Creates authentication token from JWT
    // @return Authentication token with user details and authorities
    private UsernamePasswordAuthenticationToken getAuthentication(String token, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        if (Objects.nonNull(token)) {
            // 1. Extract email from token claims
            String email = jwtUtil.extractEmailFromToken(token);

            if (Objects.nonNull(email)) {
                try {
                    // 2. Load user details from database
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    // Check if the token is valid
                    // Validate token against user details
                    if (jwtUtil.validateToken(token, userDetails)) {
                        // If valid, create and return the UsernamePasswordAuthenticationTOken
                        // 4. Create fully authenticated token with:
                        // - Principal: user details
                        // - Credentials: null (sensitive data)
                        // - Authorities: user roles/permissions
                        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    } else {
                        log.warn("Token validation failed for user: {}", email);
                    }
                } catch (UsernameNotFoundException e) {
                    log.error("User not found with email: {}", email, e);
                }
            }
        }
        // Return null if authentication couldn't be established
        return null;
    }
}
