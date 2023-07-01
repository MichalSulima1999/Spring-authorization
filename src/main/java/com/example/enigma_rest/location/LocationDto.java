package com.example.enigma_rest.location;

import com.example.enigma_rest.device.DeviceDto;

public interface LocationDto {
    Long getId();

    float getLatitude();

    float getLongitude();

    DeviceDto getDevice();
}
