package com.example.demo.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginResponseDto {
    private Long id;
    private String userId;
    private String username;
    private List<String> roles;
    private String email;
    private String lastLogin;
    private boolean mfa;
    private String qrCodeSecret;
    private String qrCodeImageUri;
    private String imageUrl;

}
