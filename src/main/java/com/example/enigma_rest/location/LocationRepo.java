package com.example.enigma_rest.location;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepo extends JpaRepository<Location, Long> {
    Page<LocationDto> findByDevice_User_UsernameOrderByDevice_DeviceIdAsc(String username, Pageable pageable);

    Page<LocationDto> findAllBy(Pageable pageable);
}
