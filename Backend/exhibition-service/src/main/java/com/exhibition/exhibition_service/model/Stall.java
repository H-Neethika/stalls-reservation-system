package com.exhibition.exhibition_service.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Stall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "stall_points", joinColumns = @JoinColumn(name = "stall_id"))
    private List<Point> points = new ArrayList<>();

    private String path;

    @ManyToOne
    private StallType stallType;

    @ManyToOne
    private Hall hall;
}
