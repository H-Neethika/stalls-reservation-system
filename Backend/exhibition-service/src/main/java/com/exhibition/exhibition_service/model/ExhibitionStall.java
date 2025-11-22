package com.exhibition.exhibition_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExhibitionStall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stallName;

    @ManyToOne
    private Stall stall;

    @ManyToOne
    private BookingStatus bookingStatus;

    @ManyToMany
    private List<Genre> genres = new ArrayList<>();

    @ManyToOne
    private Exhibition exhibition;
}
