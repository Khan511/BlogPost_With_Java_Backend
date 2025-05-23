
package com.example.demo.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {

    private String userId;
    private String username;
    private String email;
    private String password;
    private List<String> roleNames;
    private String imageUrl;
    private boolean mfa;
    private String qrCodeSecret;
    private String qrCodeImageUri;

}
