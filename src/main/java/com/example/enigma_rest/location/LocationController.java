package com.example.enigma_rest.location;

import com.example.enigma_rest.device.Device;
import com.example.enigma_rest.device.DeviceRepo;
import com.example.enigma_rest.user.User;
import com.example.enigma_rest.user.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
@Slf4j
public class LocationController {
    private final LocationService locationService;
    private final UserRepository userRepository;
    private final DeviceRepo deviceRepo;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Location> addLocation(
            @RequestBody LocationRequest request,
            HttpServletResponse response
    ) throws IOException {
        Optional<User> optionalUser = userRepository.findByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName());
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found!");
        }

        Optional<Device> optionalDevice = deviceRepo.findByDeviceId(request.getDeviceId());
        if (optionalDevice.isPresent() && optionalDevice.get().getUser() != optionalUser.get()) {
            response.getWriter().write("Device not associated with this user!");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        return ResponseEntity.ok(locationService.addLocation(request,
                optionalUser.get(),
                optionalDevice.orElse(null)));
    }

    @GetMapping
    public ResponseEntity<Page<LocationDto>> getPageOfLocations(
            @RequestParam(name = "pageNum",
                    defaultValue = "0") int pageNum,
            @RequestParam(name = "pageSize",
                    defaultValue = "10") int pageSize
    ) {
        Pageable page = PageRequest.of(pageNum, pageSize);
        return ResponseEntity.ok().body(locationService.getPageOfLocations(page));
    }

    @GetMapping("/user")
    public ResponseEntity<Page<LocationDto>> getPageOfUserLocations(
            @RequestParam(name = "pageNum",
                    defaultValue = "0") int pageNum,
            @RequestParam(name = "pageSize",
                    defaultValue = "10") int pageSize
    ) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Pageable page = PageRequest.of(pageNum, pageSize);
        return ResponseEntity.ok().body(locationService.getPageOfUserLocations(page, username));
    }
}
