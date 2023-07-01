package com.example.enigma_rest.device;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceRepo extends JpaRepository<Device, Long> {
    Optional<Device> findByDeviceId(String deviceId);

}
