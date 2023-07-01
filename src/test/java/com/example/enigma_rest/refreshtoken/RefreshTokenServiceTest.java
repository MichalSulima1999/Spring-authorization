package com.example.enigma_rest.refreshtoken;

import com.example.enigma_rest.config.TokenProperties;
import com.example.enigma_rest.user.Role;
import com.example.enigma_rest.user.User;
import com.example.enigma_rest.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RefreshTokenServiceTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepo tokenRepo;

    @Autowired
    private RefreshTokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProperties tokenProperties;

    @BeforeEach
    public void setup() {
        User user = User.builder().username("Janek123").role(Role.USER)
                .password(passwordEncoder.encode("12345678"))
                .email("Janek123@gmail.com").build();
        userRepository.save(user);

        RefreshToken refreshToken = new RefreshToken(null, user, "testToken", Instant.now().plusMillis(tokenProperties.getRefreshTokenExpirationMs()));
        tokenRepo.save(refreshToken);
    }

    @AfterEach
    public void clearDb() {
        userRepository.deleteAll();
    }

    @Test
    void findByToken() {
        Optional<RefreshToken> token = tokenService.findByToken("testToken");
        assertTrue(token.isPresent(),
                "Token should be found");
        assertEquals(token.get().getUser().getEmail(), "Janek123@gmail.com",
                "User emails should be equal");
        assertFalse(tokenService.findByToken("testToken123").isPresent(),
                "Token should not be found");
    }

    @Test
    void createRefreshToken() {
        User user = User.builder().username("Jakub23").role(Role.USER)
                .password(passwordEncoder.encode("12345678"))
                .email("Jakub23@gmail.com").build();
        userRepository.save(user);
        RefreshToken token = tokenService.createRefreshToken("Jakub23");

        assertEquals(token.getUser().getUsername(),
                "Jakub23", "Should create token for correct user");
        assertNotNull(userRepository.findByUsername("Jakub23").orElseThrow().getRefreshToken(),
                "User should have token");
    }

    @Test
    void verifyExpiration() throws TokenRefreshException {
        RefreshToken notExpiredRefreshToken = new RefreshToken(null, null, "testToken", Instant.now().plusMillis(tokenProperties.getRefreshTokenExpirationMs()));

        assertEquals(tokenService.verifyExpiration(notExpiredRefreshToken), notExpiredRefreshToken, "Token should be returned");

        RefreshToken expiredRefreshToken = new RefreshToken(null, null, "testToken", Instant.now().minusMillis(tokenProperties.getRefreshTokenExpirationMs()));
        TokenRefreshException exception = assertThrows(TokenRefreshException.class, () -> {
            tokenService.verifyExpiration(expiredRefreshToken);
        }, "Should throw exception for expired token");

        String expectedMessage = "Refresh token was expired. Please make a new sign in request";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage), "Should throw exception with correct message");
    }

    @Test
    void deleteByUserUsername() {
        User user = User.builder().username("Piotr123").role(Role.USER)
                .password(passwordEncoder.encode("12345678"))
                .email("Piotr123@gmail.com").build();
        userRepository.save(user);

        RefreshToken refreshToken = new RefreshToken(null, user, "testToken911", Instant.now().plusMillis(tokenProperties.getRefreshTokenExpirationMs()));
        RefreshToken savedToken = tokenRepo.save(refreshToken);

        tokenService.deleteByUserUsername(user.getUsername());
        assertFalse(tokenRepo.findByToken(savedToken.getToken()).isPresent(), "Should not find deleted token");
    }

    @Test
    void deleteRefreshToken() {
        User user = User.builder().username("Marek915").role(Role.USER)
                .password(passwordEncoder.encode("12345678"))
                .email("Marek911@gmail.com").build();
        userRepository.save(user);

        RefreshToken refreshToken = new RefreshToken(null, user, "testToken915", Instant.now().plusMillis(tokenProperties.getRefreshTokenExpirationMs()));
        RefreshToken savedToken = tokenRepo.save(refreshToken);

        tokenService.deleteRefreshToken(savedToken);
        assertFalse(tokenRepo.findByToken(savedToken.getToken()).isPresent(), "Should not find deleted token");
    }
}