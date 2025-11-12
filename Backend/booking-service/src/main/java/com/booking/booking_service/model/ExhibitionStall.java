package com.booking.booking_service.model;

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
    private Long id;

    @ManyToOne
    private ExhibitionHall exhibitionHallId;


    private String stallName;


    @ManyToOne
    private StallType stallType;


    private Long price;

    @ManyToOne
    private BookingStatus bookingStatus;

    @OneToMany
    private List<Genre> genres = new ArrayList<>();

    private Long rowPosition;

    private Long columnPosition;


}
