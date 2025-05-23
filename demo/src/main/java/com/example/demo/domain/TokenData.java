package com.example.demo.domain;

import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import com.example.demo.dto.LoginResponseDto;
import io.jsonwebtoken.Claims;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class TokenData {
    private LoginResponseDto loginResponseDto;
    private Claims claims;
    private boolean valid;
    private List<GrantedAuthority> authorities;

}
