package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import com.example.demo.cache.CacheStore;
import com.example.demo.dto.UserDto;
import com.example.demo.entities.ConfirmationEntity;
import com.example.demo.entities.RoleEntity;
import com.example.demo.entities.UserEntity;
import com.example.demo.enumeration.EventType;
import com.example.demo.enumeration.LoginType;
import com.example.demo.event.UserEvent;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repo.ConfirmationRepo;
import com.example.demo.repo.RoleRepository;
import com.example.demo.repo.UserRepository;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;

import static com.example.demo.utils.UserUtils.qrCodeImageUri;
import static com.example.demo.utils.UserUtils.qrCodeSecret;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CacheStore<String, Integer> userCache;
    private final ConfirmationRepo confirmationRepo;
    private final ApplicationEventPublisher publisher;

    // public UserEntity createUserWithRole(UserEntity userEntity, List<String>
    // roleNames) {

    @Transactional
    public void createUserWithRole(UserEntity userEntity, List<String> roleNames) {

        List<RoleEntity> roles = roleRepository.findByNameIn(roleNames);
        userEntity.getRoles().addAll(roles);
        UserEntity user = userRepository.save(userEntity);

        ConfirmationEntity confirmationEntity = new ConfirmationEntity(user);
        confirmationRepo.save(confirmationEntity);

        publisher.publishEvent(
                new UserEvent(userEntity, EventType.REGISTRATION, Map.of("key", confirmationEntity.getKey())));

    }

    @Transactional
    public void verifyAccountKey(String key) {
        ConfirmationEntity confirmationEntity = confirmationRepo.findByKey(key)
                .orElseThrow(() -> new RuntimeException("Confirmation Key not found"));

        UserEntity user = userRepository.findByEmail(confirmationEntity.getUserEntity().getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);
        confirmationRepo.delete(confirmationEntity);
    }

    public UserDto setupMfa(Long id) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User Not Found"));

        String codeSecret = qrCodeSecret.get();
        user.setQrCodeImageUri(qrCodeImageUri.apply(user.getEmail(), codeSecret));

        user.setQrCodeSecret(codeSecret);
        user.setMfa(true);
        userRepository.save(user);

        return UserMapper.toUserDto(user);
    }

    public UserDto cancelMfa(Long id) {

        UserEntity user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        user.setMfa(false);
        user.setQrCodeSecret(EMPTY);
        user.setQrCodeImageUri(EMPTY);
        userRepository.save(user);

        return UserMapper.toUserDto(user);

    }

    public UserEntity verifyQrCode(String userId, String qrCode) {
        UserEntity user = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User Not Found"));
        verifyCode(qrCode, user.getQrCodeSecret());

        return user;
    }

    private boolean verifyCode(String qrCode, String qrCodeSecret) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

        if (codeVerifier.isValidCode(qrCodeSecret, qrCode)) {
            return true;
        } else {
            throw new RuntimeException("Inavalid QE code. Please try again.");
        }
    }

    @Transactional(readOnly = true)
    public UserEntity getUserWithPostsAndComments(Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.getPosts().forEach(post -> post.getComments().size());
                    return user;
                }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void updateLoginAttempt(String email, LoginType loginType) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        switch (loginType) {
            case LOGIN_ATTEMPT -> {
                // If the user is not found in the cache (i.e., it's their first attempt),
                // reset their login attempts to 0 and unlock their account.
                if (userCache.get(userEntity.getEmail()) == null) {
                    userEntity.setLoginAttempts(0); // Reset
                    userEntity.setAccountNonLocked(true);
                }
                userEntity.setLoginAttempts(userEntity.getLoginAttempts() + 1);

                userCache.put(userEntity.getEmail(), userEntity.getLoginAttempts());
                // If the login attempts exceed 5, lock the user's account.
                if (userCache.get(userEntity.getEmail()) > 5) {
                    userEntity.setAccountNonLocked(false);
                }

            }
            case LOGIN_SUCCESS -> {
                // Handle successful login case
                // If the login is successful, ensure the account is unlocked.
                userEntity.setAccountNonLocked(true);

                // Reset the login attempt count to 0.
                userEntity.setLoginAttempts(0);

                // set last loing to now
                userEntity.setLastLogin(LocalDateTime.now());

                // Remove the user from the cache since their login was successful.
                userCache.evict(userEntity.getEmail());
            }
        }

        // Save the updated user enity back to the database
        userRepository.save(userEntity);
    }

    public UserEntity getByUserId(String userId) {

        // return userRepository.findById(userId).orElseThrow(() -> new
        // RuntimeException("User not found"));
        return userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserEntity getUserByName(String name) {
        return userRepository.findByUsername(name).orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
    }

    // UserService.java
    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public String deleteUser(Long userid) {
        userRepository.deleteById(userid);

        return "User has been deleted Succeessfully";
    }

    public void expireAccountsOlderThan(LocalDateTime threshhold) {
        // Expire accounts older than a specific date
        List<UserEntity> users = userRepository.findByLastLoginBefore(threshhold);
        users.forEach(user -> user.setAccountNonExpired(false));
        userRepository.saveAll(users);

    }

}
