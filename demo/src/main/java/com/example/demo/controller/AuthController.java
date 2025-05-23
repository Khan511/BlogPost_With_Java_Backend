package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.demo.entities.UserEntity;
import com.example.demo.enumeration.TokenType;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.security.JwtUtil;
import com.example.demo.security.TokenBlacklist;
import com.example.demo.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtUtil jwtUtil;
    private final TokenBlacklist tokenBlacklist;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {

        try {
            if (userDetails instanceof CustomUserDetails) {
                CustomUserDetails authenticatedUser = (CustomUserDetails) userDetails;
                UserEntity user = userService.getUserByName(authenticatedUser.getUsername());

                List<String> roles = authenticatedUser.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList());

                Map<String, Object> response = new HashMap<>();
                response.put("username", user.getUsername());
                response.put("userId", user.getUserId());
                response.put("role", roles);
                response.put("imageUrl", user.getImageUrl());
                response.put("qrCodeImageUri", user.getQrCodeImageUri());
                response.put("qrCode", user.getQrCodeSecret());
                response.put("mfa", user.isMfa());
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error Authenticatin Me ===== ", e);
            return null;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {

        // Invalidte token
        jwtUtil.getTokenFromRequestCookie(request, TokenType.ACCESS.getValue()).ifPresent(tokenBlacklist::add);

        jwtUtil.getTokenFromRequestCookie(request, TokenType.REFRESH.getValue()).ifPresent(tokenBlacklist::add);

        // Clear cookies
        jwtUtil.removeTokenCookies(response);

        // Clrear security Context
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok().build();

    }

}
