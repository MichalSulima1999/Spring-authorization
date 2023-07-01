package com.example.enigma_rest.location;

import com.example.enigma_rest.device.Device;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Location {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    @NotNull
    @Min(-180)
    @Max(180)
    private float latitude;

    @Column(nullable = false)
    @NotNull
    @Min(-90)
    @Max(90)
    private float longitude;

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    @JsonIgnore
    private Device device;
}
