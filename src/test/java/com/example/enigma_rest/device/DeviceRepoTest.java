package com.example.enigma_rest.device;

import com.example.enigma_rest.user.Role;
import com.example.enigma_rest.user.User;
import com.example.enigma_rest.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DeviceRepoTest {
    @Autowired
    private DeviceRepo deviceRepo;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .username("Marek95")
                .role(Role.USER)
                .password("12345678")
                .email("Marek95@gmail.com").build());
        Device device = new Device(null, "12345", user, null);
        deviceRepo.save(device);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        deviceRepo.deleteAll();
    }

    @Test
    void findByDeviceId() {
        Optional<Device> foundDevice = deviceRepo.findByDeviceId("12345");
        assertTrue(foundDevice.isPresent(), "Should find device by deviceId");
        assertEquals(foundDevice.get().getDeviceId(), "12345", "Should have correct deviceId");
        assertEquals(foundDevice.get().getUser().getUsername(), "Marek95", "Should have correct user");
    }
}