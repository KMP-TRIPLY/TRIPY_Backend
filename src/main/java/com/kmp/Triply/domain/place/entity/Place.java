package com.kmp.Triply.domain.place.entity;

import com.kmp.Triply.domain.trip.entity.Trip;
import com.kmp.Triply.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false)
    private String name;

    private String address;

    private Double latitude;

    private Double longitude;

    private LocalDate visitDate;

    private String memo;

    @Builder
    private Place(Trip trip, String name, String address, Double latitude, Double longitude,
                  LocalDate visitDate, String memo) {
        this.trip = trip;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.visitDate = visitDate;
        this.memo = memo;
    }

    public void update(String name, String address, LocalDate visitDate, String memo) {
        this.name = name;
        this.address = address;
        this.visitDate = visitDate;
        this.memo = memo;
    }
}