package com.exhibition.exhibition_service.model;

import com.exhibition.exhibition_service.enums.EXHIBITION_STATE;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder



public class Exhibition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)


    private Long id;
    @Column(name = "organizer_id")
    private Long organizerId;
    private String exhibitionName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalDateTime bookingOpenDateTime;
    private LocalDateTime bookingCloseDateTime;
    private int stallsPerPerson;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EXHIBITION_STATE exhibitionState = EXHIBITION_STATE.DRAFT;


}
