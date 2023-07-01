package com.example.enigma_rest.user;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        userRepository.deleteAll();
    }

    @Test
    void findByUsername() {
        assertTrue(userRepository.findByUsername("Marek95").isPresent(),
                "User should be found");
        assertEquals(userRepository.findByUsername("Marek95").get().getEmail(), "Marek95@gmail.com",
                "User emails should be equal");
        assertFalse(userRepository.findByUsername("Marek59").isPresent(),
                "User should not be found");
    }
}