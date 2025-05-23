package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.domain.Response;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.dto.QrCodeRequest;
import com.example.demo.dto.UserDto;
import com.example.demo.entities.UserEntity;
import com.example.demo.enumeration.TokenType;
import com.example.demo.mapper.UserMapper;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.UserService;
import static com.example.demo.utils.RequestUtils.getResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/create")
    // public UserDto createUser(@RequestBody UserDto userdto) {
    public ResponseEntity<?> createUser(@RequestBody UserDto userdto, HttpServletRequest request) {

        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(UUID.randomUUID().toString());
        userEntity.setUsername(userdto.getUsername());
        userEntity.setEmail(userdto.getEmail());
        userEntity.setPassword(passwordEncoder.encode(userdto.getPassword()));
        userEntity.setImageUrl(userdto.getImageUrl());

        userService.createUserWithRole(userEntity, userdto.getRoleNames());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User Created Successfully. Please check your email for account confirmation"));
    }

    @GetMapping("/verify/account")
    public ResponseEntity<?> verifyAccount(@RequestParam("key") String key, HttpServletRequest request)
            throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
        userService.verifyAccountKey(key);

        return ResponseEntity.ok().body(Map.of("message", "Account verified"));
    }

    @PatchMapping("/mfa/setup")
    public ResponseEntity<Response> setUpMfa(@AuthenticationPrincipal UserDetails userdetails,
            HttpServletRequest request) {
        UserEntity getUser = userService.getUserByName(userdetails.getUsername());

        UserDto user = userService.setupMfa(getUser.getId());

        return ResponseEntity.ok()
                .body(getResponse(request, Map.of("user", user), "MFA set up successfully", HttpStatus.OK));
    }

    @PatchMapping("/cancel/mfa")
    public ResponseEntity<Response> cancelMfa(@AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        UserEntity getUser = userService.getUserByName(userDetails.getUsername());

        UserDto userDto = userService.cancelMfa(getUser.getId());

        return ResponseEntity.ok()
                .body(getResponse(request, Map.of("user", userDto), "MFA Canceled successfully", HttpStatus.OK));

    }

    @PostMapping("/verify/qrcode")
    public ResponseEntity<Response> verifyQrCode(@RequestBody QrCodeRequest qrCodeRequest, HttpServletRequest request,
            HttpServletResponse response) {

        LoginResponseDto user = UserMapper
                .tLoginResponseDto(userService.verifyQrCode(qrCodeRequest.getUserId(), qrCodeRequest.getQrCode()));

        jwtUtil.setTokenCookieInResponse(response, user, TokenType.ACCESS);
        jwtUtil.setTokenCookieInResponse(response, user, TokenType.REFRESH);

        return ResponseEntity.ok().body(getResponse(request, Map.of("user", user), "QR code verified", HttpStatus.OK));
    }

    @GetMapping("/find/{userId}")
    public UserDto findUserByID(@PathVariable String userId) {

        return UserMapper.toUserDto(userService.getByUserId(userId));
    }

    @DeleteMapping("/delete/{userId}")
    public String DeleteUserEntity(@PathVariable Long userId) {
        return userService.deleteUser(userId);
    }

    @Scheduled(cron = "0 0 0 * * ?") // Run daily at midnight
    public void expireInActiveAccounts() {
        LocalDateTime treshold = LocalDateTime.now().minusDays(1); // 90 days inactive
        userService.expireAccountsOlderThan(treshold);
    }
}
