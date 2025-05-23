package com.example.demo.security;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.apache.logging.log4j.util.TriConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.example.demo.domain.Token;
import com.example.demo.domain.TokenData;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.enumeration.TokenType;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static com.example.demo.constant.Constant.EMAIL;
import static com.example.demo.constant.Constant.NAJI;
import static com.example.demo.constant.Constant.ROLES;
import static com.example.demo.enumeration.TokenType.ACCESS;
import static com.example.demo.enumeration.TokenType.REFRESH;
import static java.util.Arrays.stream;
import static org.springframework.boot.web.server.Cookie.SameSite.NONE;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    // private static final long EXPIRATION_TIME = 864_000_000;
    @Value("${jwt.accessTokenExpiration}")
    private Long accessTokenExpiration;
    @Value("${jwt.refreshTokenExpiration}")
    private Long refreshTokenExpiration;
    @Value("${jwt.secret}")
    private String jwtSecret;
    private final UserService userService;

    // Decodes the base64-encoded secret and creates an HMAC-SHA SecretKey.
    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Prepares a JwtBuilder with standard headers, audience, ID, timestamps,
    // and signing key.
    private JwtBuilder jwtBuilder() {
        return Jwts.builder()
                // Standard header with type set to JWT
                .setHeader(Map.of(Header.TYPE, Header.JWT_TYPE))
                // Audience (who this token is intended for)
                .setAudience(NAJI)
                .setId(UUID.randomUUID().toString())
                // Token not valid before now
                .setIssuedAt(Date.from(Instant.now()))
                .setNotBefore(new Date())
                // Sign with HMAC-SHA512
                .signWith(getSignInKey(), SignatureAlgorithm.HS512);
    }

    // Generates either an access or a refresh token for an authenticated user.
    public String generateToken(LoginResponseDto user, TokenType tokenType) {
        // Access token: includes subject, email, roles, and shorter expiration
        return Objects.equals(tokenType, TokenType.ACCESS) ?

                jwtBuilder()
                        .setSubject(user.getUserId())
                        .claim(EMAIL, user.getEmail())
                        .claim(ROLES, user.getRoles())
                        .setExpiration(Date.from(Instant.now().plusSeconds(accessTokenExpiration)))
                        .compact()
                // Refresh token: only subject and longer expiration
                : jwtBuilder()
                        .setSubject(user.getUserId())
                        .setExpiration(Date.from(Instant.now().plusSeconds(refreshTokenExpiration)))
                        .compact();
    }

    // Generic method to extract specific claims from token
    private <T> T extractClaims(String token, Function<Claims, T> claimResolver) {
        final Claims claims = parseTOkenClaims(token);
        return claimResolver.apply(claims);
    }

    // Extracts the JWT 'sub' (subject) claim.
    public String extractSubjectFromToken(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    /** Extracts the custom 'email' claim. */
    public String extractEmailFromToken(String token) {
        return extractClaims(token, claims -> claims.get("email", String.class));
    }
    // private Date extractExpirationDate(String token) {
    // return extractClaims(token, Claims::getExpiration);
    // }

    // Builds and sets a cookie (access or refresh) on the HTTP response.
    // Using TriConsumer to bundle response, user, and tokenType.
    private final TriConsumer<HttpServletResponse, LoginResponseDto, TokenType> tokenCookieSetter = (response, user,
            tokenType) -> {
        // Switch statement to handle different token types(ACCESS and REFRESH)
        switch (tokenType) {
            case ACCESS -> {
                var accessToken = generateTokenForuser(user, Token::getAccess);
                var cookie = new Cookie(tokenType.getValue(), accessToken);
                cookie.setHttpOnly(true);// Prevent XSS access
                cookie.setSecure(true);// Only send over HTTPS
                // Shorter maxAge for access tokens
                cookie.setMaxAge(2 * 60);
                cookie.setPath("/");// Available across entire domain
                cookie.setAttribute("SameSite", NONE.name());// Cross-site cookies
                response.addCookie(cookie);
            }
            case REFRESH -> {
                var refreshToken = generateTokenForuser(user, Token::getRefresh);
                var cookie = new Cookie(tokenType.getValue(), refreshToken);
                cookie.setHttpOnly(true);
                cookie.setSecure(true);
                // Longer maxAge for access tokens
                cookie.setMaxAge(2 * 60 * 60);
                cookie.setPath("/");
                cookie.setAttribute("SameSite", NONE.name());
                response.addCookie(cookie);
            }
        }
    };

    // Generates both tokens and returns specific one via tokenFunction
    public String generateTokenForuser(LoginResponseDto user, Function<Token, String> tokenFunction) {
        var token = Token.builder()
                .access(generateToken(user, ACCESS))
                .refresh(generateToken(user, REFRESH))
                .build();
        return tokenFunction.apply(token);
    }

    // Checks if token expiration date is before current time
    private Boolean isTokenExpired(String token) throws Exception {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            throw new Exception("Error checking token expiration" + e);
        }
    }

    // Validates that the subject matches the userDetails and the token is not
    // expired.
    public Boolean validateToken(String token, UserDetails userDetails) throws Exception {
        String userIdFromToken = extractSubjectFromToken(token);
        String userIdFromDataBase = userService.getUserByName(userDetails.getUsername()).getUserId();

        return userIdFromToken.equals(userIdFromDataBase) && !isTokenExpired(token);
    }

    /** Sets the JWT cookie in the HTTP response. */
    public void setTokenCookieInResponse(HttpServletResponse response, LoginResponseDto user, TokenType tokenType) {
        tokenCookieSetter.accept(response, user, tokenType);
    }

    // Retrieves a token value from an HTTP request’s cookies by name.
    public Optional<String> getTokenFromRequestCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            return stream(request.getCookies()).filter(cookie -> Objects.equals(cookieName, cookie.getName()))
                    .map(Cookie::getValue).findAny();
        }
        return Optional.empty();
    }

    // Parses token and extracts all claims
    private Claims parseTOkenClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())// Set secret to verify signature
                .build()
                .parseClaimsJws(token)// Parse token
                .getBody(); // Get claims payload
    }

    // * Provides a generic way to extract custom TokenData through a function.
    public <T> T extractTokenData(String token, Function<TokenData, T> tokenFunction) {
        var user = userService.getByUserId(extractSubjectFromToken(token));
        boolean isValid = user != null && user.getUserId().equals(parseTOkenClaims(token).getSubject());

        // Get authorities from user's roles (database)
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_ " + role.getName())).collect(Collectors.toList());

        return tokenFunction.apply(TokenData.builder()
                .valid(isValid)
                .authorities(authorities)
                .claims(parseTOkenClaims(token))
                .loginResponseDto(UserMapper.tLoginResponseDto(user))
                .build());
    }

    // Extracts expiration date fro token
    private Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    /** Removes both access and refresh token cookies by setting maxAge=0. */
    public void removeTokenCookies(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie(TokenType.ACCESS.getValue(), null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setAttribute("SameSite", "None");
        response.addCookie(accessTokenCookie);

        // Remove Refresh Token
        Cookie refreshTokenCookie = new Cookie(TokenType.REFRESH.getValue(), null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setAttribute("SameSite", "None");
        response.addCookie(refreshTokenCookie);
    }
}
