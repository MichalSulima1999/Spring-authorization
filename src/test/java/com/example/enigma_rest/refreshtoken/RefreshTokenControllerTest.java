package com.example.enigma_rest.refreshtoken;

import com.example.enigma_rest.config.TokenProperties;
import com.example.enigma_rest.user.Role;
import com.example.enigma_rest.user.User;
import com.example.enigma_rest.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RefreshTokenControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepo tokenRepo;

    @Autowired
    private TokenProperties tokenProperties;

    @BeforeEach
    public void setup() {
        User user = User.builder().username("Marek95").role(Role.USER)
                .password(passwordEncoder.encode("12345678"))
                .email("Marek95@gmail.com").build();
        userRepository.save(user);

        RefreshToken refreshToken = new RefreshToken(null, user, "testToken", Instant.now().plusMillis(tokenProperties.getRefreshTokenExpirationMs()));
        tokenRepo.save(refreshToken);
    }

    @AfterEach
    public void clearDb() {
        tokenRepo.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void refreshTokenWithCookie() throws Exception {
        Cookie cookie = new Cookie(tokenProperties.getRefreshTokenCookieName(), "testToken");

        mockMvc.perform(get("/api/refresh-token/refresh")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", Matchers.notNullValue()))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    void refreshTokenWithoutCookie() throws Exception {
        mockMvc.perform(get("/api/refresh-token/refresh"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void refreshTokenWithExpiredRefreshToken() throws Exception {
        User user = User.builder().username("Marek2").role(Role.USER)
                .password(passwordEncoder.encode("12345678"))
                .email("Marek2@gmail.com").build();
        userRepository.save(user);

        RefreshToken refreshToken = new RefreshToken(null, user, "expiredToken", Instant.now().minusMillis(tokenProperties.getRefreshTokenExpirationMs()));
        tokenRepo.save(refreshToken);
        Cookie cookie = new Cookie(tokenProperties.getRefreshTokenCookieName(), "expiredToken");

        mockMvc.perform(get("/api/refresh-token/refresh")
                        .cookie(cookie))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Refresh token expired!"));
    }

    @Test
    void logoutUserWithCookie() throws Exception {
        Cookie cookie = new Cookie(tokenProperties.getRefreshTokenCookieName(), "testToken");
        MvcResult mvcResult = mockMvc.perform(get("/api/refresh-token/logout")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(content().string("Logged out successfully")).andReturn();
        String headerValue = mvcResult.getResponse().getHeader("Set-Cookie");
        assertEquals(headerValue, "jwt=; Max-Age=0; Expires=Thu, 1 Jan 1970 00:00:00 GMT; Secure; HttpOnly; SameSite=None", "Should send null token");
    }
}