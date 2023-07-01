package com.example.enigma_rest.location;

import com.example.enigma_rest.auth.AuthenticationRequest;
import com.example.enigma_rest.auth.AuthenticationResponse;
import com.example.enigma_rest.auth.AuthenticationService;
import com.example.enigma_rest.device.Device;
import com.example.enigma_rest.device.DeviceRepo;
import com.example.enigma_rest.user.Role;
import com.example.enigma_rest.user.User;
import com.example.enigma_rest.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.enigma_rest.Utils.asJsonString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LocationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LocationRepo locationRepo;
    @Autowired
    private LocationService locationService;
    @Autowired
    private DeviceRepo deviceRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .username("Marek95")
                .role(Role.ADMIN)
                .password(passwordEncoder.encode("12345678"))
                .email("Marek95@gmail.com").build());
        Device device = deviceRepo.save(new Device(null, "12345", user, null));
        locationRepo.save(Location.builder().device(device).latitude(12).longitude(23).build());

        User user2 = userRepository.save(User.builder()
                .username("Marek11")
                .role(Role.USER)
                .password(passwordEncoder.encode("12345678"))
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
    void addLocation() throws Exception {
        LocationRequest request = new LocationRequest("2323", 123.123f, 43.123f);

        mockMvc.perform(post("/api/location")
                        .header("Authorization", "Bearer " + getAccessToken("Marek95", "12345678"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.latitude", equalTo(123.123)))
                .andExpect(jsonPath("$.longitude", equalTo(43.123)));
    }

    @Test
    void addLocationWrongDevice() throws Exception {
        LocationRequest request = new LocationRequest("123", 123, 43);


        mockMvc.perform(post("/api/location")
                        .header("Authorization", "Bearer " + getAccessToken("Marek95", "12345678"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Device not associated with this user!"));
    }

    @Test
    void getPageOfLocationsAsAdmin() throws Exception {
        Pageable pageable = PageRequest.of(0, 5);


        mockMvc.perform(get("/api/location")
                        .header("Authorization", "Bearer " + getAccessToken("Marek95", "12345678")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    void getPageOfLocationsAsUser() throws Exception {
        Pageable pageable = PageRequest.of(0, 5);


        mockMvc.perform(get("/api/location")
                        .header("Authorization", "Bearer " + getAccessToken("Marek11", "12345678")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPageOfUserLocations() throws Exception {
        Pageable pageable = PageRequest.of(0, 5);


        mockMvc.perform(get("/api/location/user")
                        .header("Authorization", "Bearer " + getAccessToken("Marek95", "12345678")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].latitude", equalTo(12.0)));
    }

    private String getAccessToken(String username, String password) {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationRequest request =
                new AuthenticationRequest(
                        username,
                        password);
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(request, response);
        return authenticationResponse.getToken();
    }
}