package com.booking.booking_service.model;

import com.booking.booking_service.domain.STALL_TYPE;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stall {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String stallName;

    @ManyToOne
    private Hall hall;

    private STALL_TYPE stallType;

    @OneToOne
    private BookingStatus bookingStatus;

    private Boolean isActive;

    @OneToMany
    private List<Genre> genres = new ArrayList<>();

    private Long price;

}
