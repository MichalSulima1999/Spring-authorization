package com.example.enigma_rest.location;

import com.example.enigma_rest.device.Device;
import com.example.enigma_rest.device.DeviceRepo;
import com.example.enigma_rest.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepo locationRepo;
    private final DeviceRepo deviceRepo;

    public Location addLocation(LocationRequest locationRequest, User user, Device device) {
        Location location = new Location(null, locationRequest.getLatitude(), locationRequest.getLongitude(), null);

        if (device == null) {
            Device newDevice = deviceRepo.save(new Device(null, locationRequest.getDeviceId(), user, null));
            location.setDevice(newDevice);
        } else {
            location.setDevice(device);
        }

        return locationRepo.save(location);
    }

    public Page<LocationDto> getPageOfLocations(Pageable pageable) {
        return locationRepo.findAllBy(pageable);
    }

    public Page<LocationDto> getPageOfUserLocations(Pageable pageable, String username) {
        return locationRepo.findByDevice_User_UsernameOrderByDevice_DeviceIdAsc(username, pageable);
    }
}
