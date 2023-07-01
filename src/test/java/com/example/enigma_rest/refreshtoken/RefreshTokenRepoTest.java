package com.example.enigma_rest.refreshtoken;

import com.example.enigma_rest.config.TokenProperties;
import com.example.enigma_rest.user.Role;
import com.example.enigma_rest.user.User;
import com.example.enigma_rest.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RefreshTokenRepoTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepo tokenRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProperties tokenProperties;

    @BeforeAll
    public void setup() {
        User user = User.builder().username("Marek95").role(Role.USER)
                .password(passwordEncoder.encode("12345678"))
                .email("Marek95@gmail.com").build();
        userRepository.save(user);

        RefreshToken refreshToken = new RefreshToken(null, user, "testToken", Instant.now().plusMillis(tokenProperties.getRefreshTokenExpirationMs()));
        tokenRepo.save(refreshToken);
    }

    @AfterAll
    public void clearDb() {
        userRepository.deleteAll();
    }

    @Test
    void findByToken() {
        Optional<RefreshToken> token = tokenRepo.findByToken("testToken");
        assertTrue(token.isPresent(),
                "Token should be found");
        assertEquals(token.get().getUser().getEmail(), "Marek95@gmail.com",
                "User emails should be equal");
        assertFalse(tokenRepo.findByToken("testToken123").isPresent(),
                "Token should not be found");
    }

    @Test
    @Transactional
    void deleteByUser() {
        User user = userRepository.findByUsername("Marek95").orElseThrow();
        tokenRepo.deleteByUser(user);
        assertFalse(tokenRepo.findByToken("testToken").isPresent(), "Token should be deleted");
    }
}