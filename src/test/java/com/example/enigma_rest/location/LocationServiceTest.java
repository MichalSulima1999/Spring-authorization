package com.example.enigma_rest.location;

import com.example.enigma_rest.device.Device;
import com.example.enigma_rest.device.DeviceRepo;
import com.example.enigma_rest.user.Role;
import com.example.enigma_rest.user.User;
import com.example.enigma_rest.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class LocationServiceTest {
    @Autowired
    private DeviceRepo deviceRepo;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LocationRepo locationRepo;
    @Autowired
    private LocationService locationService;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .username("Marek95")
                .role(Role.USER)
                .password("12345678")
                .email("Marek95@gmail.com").build());
        Device device = deviceRepo.save(new Device(null, "12345", user, null));
        locationRepo.save(Location.builder().device(device).latitude(12).longitude(23).build());

        User user2 = userRepository.save(User.builder()
                .username("Marek11")
                .role(Role.USER)
                .password("12345678")
                .email("Marek11@gmail.com").build());
        Device device2 = deviceRepo.save(new Device(null, "123", user2, null));
        locationRepo.save(Location.builder().device(device2).latitude(12).longitude(23).build());
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        deviceRepo.deleteAll();
        locationRepo.deleteAll();
    }

    @Test
    void addLocation() {
        LocationRequest request = new LocationRequest("2323", 54, 34);
        User user = userRepository.save(User.builder()
                .username("Adam23")
                .role(Role.USER)
                .password("12345678")
                .email("Adam23@gmail.com").build());
        Device device = deviceRepo.save(new Device(null, "2323", user, null));
        Location location = locationService.addLocation(request, user, device);

        Optional<Location> optionalLocation = locationRepo.findById(location.getId());
        assertTrue(optionalLocation.isPresent(), "Should find added location");
        assertEquals(optionalLocation.get().getDevice().getDeviceId(), "2323", "Device id should be correct");
    }

    @Test
    void addLocationWithNewDevice() {
        LocationRequest request = new LocationRequest("4545", 54, 34);
        User user = userRepository.save(User.builder()
                .username("Adam23")
                .role(Role.USER)
                .password("12345678")
                .email("Adam23@gmail.com").build());
        Location location = locationService.addLocation(request, user, null);

        Optional<Location> optionalLocation = locationRepo.findById(location.getId());
        assertTrue(optionalLocation.isPresent(), "Should find added location");
        assertEquals(optionalLocation.get().getDevice().getDeviceId(), "4545", "Device id should be correct");
        assertTrue(deviceRepo.findById(optionalLocation.get().getDevice().getId()).isPresent(), "Should find added device");
    }

    @Test
    void getPageOfLocations() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<LocationDto> page = locationService.getPageOfLocations(pageable);
        assertEquals(page.getTotalElements(), 2, "Should find two elements");
    }

    @Test
    void getPageOfUserLocations() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<LocationDto> page = locationService.getPageOfUserLocations(pageable, "Marek95");

        assertEquals(page.getTotalElements(), 1, "Should find one element");
        assertEquals(page.getContent().get(0).getDevice().getDeviceId(), "12345", "Should get correct device id");
    }
}