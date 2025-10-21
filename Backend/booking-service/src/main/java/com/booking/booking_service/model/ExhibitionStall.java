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
@AllArgsConstructor
@NoArgsConstructor
public class ExhibitionStall {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;


    private Long stallId;


    private Long hallId;


    private Long exhibitionId;

    @Enumerated(EnumType.STRING)
    private STALL_TYPE stallType;

    @ManyToOne
    private BookingStatus bookingStatus;

    @OneToMany
    private List<Genre> genres = new ArrayList<>();

    private Long price;
}
