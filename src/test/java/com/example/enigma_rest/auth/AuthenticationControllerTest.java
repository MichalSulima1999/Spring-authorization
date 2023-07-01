package com.example.enigma_rest.auth;

import com.example.enigma_rest.refreshtoken.RefreshTokenRepo;
import com.example.enigma_rest.user.Role;
import com.example.enigma_rest.user.User;
import com.example.enigma_rest.user.UserRepository;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.enigma_rest.Utils.asJsonString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepo tokenRepo;

    @BeforeEach
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

    @AfterEach
    public void clearDb() {
        userRepository.deleteAll();
        tokenRepo.deleteAll();
    }

    @Test
    void register() throws Exception {
        RegisterRequest request =
                new RegisterRequest("JohnDoe",
                        "JohnDoe@gmail.com",
                        "12345678",
                        Role.USER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", Matchers.notNullValue()))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    void authenticate() throws Exception {
        AuthenticationRequest request =
                new AuthenticationRequest(
                        "Marek95",
                        "12345678");

        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", Matchers.notNullValue()))
                .andExpect(jsonPath("$.role", is("USER")));
    }
}