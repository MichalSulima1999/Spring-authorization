package com.example.enigma_rest.auth;

import com.example.enigma_rest.refreshtoken.RefreshTokenRepo;
import com.example.enigma_rest.user.Role;
import com.example.enigma_rest.user.User;
import com.example.enigma_rest.user.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthenticationServiceTest {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepo tokenRepo;

    @BeforeAll
    public void setup() {
        List<User> users = new ArrayList<>(Arrays.asList(
                User.builder().username("Marek95").role(Role.USER)
                        .password(passwordEncoder.encode("12345678"))
                        .email("Marek95@gmail.com").build(),
                User.builder().username("Admin").role(Role.ADMIN)
                        .password(passwordEncoder.encode("12345678"))
                        .email("Admin@gmail.com").build()
        ));

        userRepository.saveAll(users);
    }

    @AfterAll
    public void clearDb() {
        tokenRepo.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterUser() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        RegisterRequest request =
                new RegisterRequest("JohnDoe",
                        "JohnDoe@gmail.com",
                        "12345678",
                        Role.USER);
        AuthenticationResponse authenticationResponse =
                authenticationService.register(request, response);
        Optional<User> foundUser = userRepository.findByUsername(request.getUsername());

        assertNotNull(authenticationResponse.getToken(), "Access token should be generated");
        assertTrue(response.containsHeader("Set-Cookie"), "Refresh token should be generated");
        assertTrue(foundUser.isPresent(), "User should be added");
        assertNotEquals(foundUser.get().getPassword(), request.getPassword(), "Password should be encrypted");
    }

    @Test
    void shouldAuthenticateUser() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationRequest request =
                new AuthenticationRequest(
                        "Marek95",
                        "12345678");
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(request, response);

        assertNotNull(authenticationResponse.getToken(), "Access token should be generated");
        assertTrue(response.containsHeader("Set-Cookie"), "Refresh token should be generated");
        assertEquals(authenticationResponse.getRole(), Role.USER, "Should set correct role");
    }

    @Test
    void shouldAuthenticateAdmin() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationRequest request =
                new AuthenticationRequest(
                        "Admin",
                        "12345678");
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(request, response);

        assertNotNull(authenticationResponse.getToken(), "Access token should be generated");
        assertTrue(response.containsHeader("Set-Cookie"), "Refresh token should be generated");
        assertEquals(authenticationResponse.getRole(), Role.ADMIN, "Should set correct role");
    }
}