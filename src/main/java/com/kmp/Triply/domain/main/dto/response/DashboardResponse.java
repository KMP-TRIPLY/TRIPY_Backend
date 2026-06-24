package com.kmp.Triply.domain.main.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class DashboardResponse {

    private Long userId;
    private String nickname;
    private String profileImg;
    private int level;

    private TripSummary nearestTrip;
    private long totalTrips;

    private List<SpotPreview> recommendedSpots;

    @Getter
    @Builder
    public static class TripSummary {
        private Long tripId;
        private String title;
        private LocalDate startDate;
        private LocalDate endDate;
        private long dDay;
        private String status;
    }

    @Getter
    @Builder
    public static class SpotPreview {
        private String contentId;
        private String title;
        private String signguNm;
        private String categoryMiddle;
        private int rank;
    }
}