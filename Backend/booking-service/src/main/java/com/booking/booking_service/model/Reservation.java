package com.booking.booking_service.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reservation {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long exhibitionId;

    @ElementCollection
    @CollectionTable(name = "reservation_stall_ids", joinColumns = @JoinColumn(name = "reservation_id"))
    @Column(name = "stall_id")
    private List<Long> stallIds = new ArrayList<>();

    private Date createdAt;

    private Long totalAmount;


}
