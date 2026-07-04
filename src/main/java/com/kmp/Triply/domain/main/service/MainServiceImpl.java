package com.kmp.Triply.domain.main.service;

import com.kmp.Triply.domain.main.dto.response.DashboardResponse;
import com.kmp.Triply.domain.main.dto.response.DDayResponse;
import com.kmp.Triply.domain.tourism.dto.response.RecommendationResponse;
import com.kmp.Triply.domain.tourism.service.TourismApiService;
import com.kmp.Triply.domain.trip.entity.Trip;
import com.kmp.Triply.domain.trip.repository.TripRepository;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.domain.user.repository.UserRepository;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainServiceImpl implements MainService {

    private static final int DASHBOARD_SPOT_PREVIEW_COUNT = 3;

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final TourismApiService tourismApiService;

    @Override
    public DashboardResponse getDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        long totalTrips = tripRepository.countByUserId(userId);
        Trip nearestTrip = tripRepository
                .findFirstByUserIdAndEndDateGreaterThanEqualOrderByStartDateAsc(userId, LocalDate.now())
                .orElse(null);

        DashboardResponse.TripSummary tripSummary = nearestTrip == null ? null : buildTripSummary(nearestTrip);

        List<DashboardResponse.SpotPreview> spotPreviews = fetchSpotPreviews();

        return DashboardResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImg(user.getProfileImg())
                .level(user.getLevel())
                .nearestTrip(tripSummary)
                .totalTrips(totalTrips)
                .recommendedSpots(spotPreviews)
                .build();
    }

    private List<DashboardResponse.SpotPreview> fetchSpotPreviews() {
        try {
            List<RecommendationResponse> spots = new ArrayList<>(tourismApiService.getChungcheongRecommendations());
            Collections.shuffle(spots);
            return spots.stream()
                    .limit(DASHBOARD_SPOT_PREVIEW_COUNT)
                    .map(s -> DashboardResponse.SpotPreview.builder()
                            .contentId(s.getContentId())
                            .title(s.getTitle())
                            .signguNm(s.getSignguNm())
                            .categoryMiddle(s.getCategoryMiddle())
                            .rank(s.getRank())
                            .build())
                    .toList();
        } catch (Exception e) {
            log.warn("대시보드 추천 명소 조회 실패, 빈 목록으로 대체", e);
            return Collections.emptyList();
        }
    }

    @Override
    public DDayResponse getDDay(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Trip trip = tripRepository
                .findFirstByUserIdAndEndDateGreaterThanEqualOrderByStartDateAsc(userId, LocalDate.now())
                .orElse(null);

        if (trip == null) {
            return null;
        }

        LocalDate today = LocalDate.now();
        long dDay = ChronoUnit.DAYS.between(today, trip.getStartDate());
        String status = resolveStatus(today, trip.getStartDate(), trip.getEndDate());

        return DDayResponse.builder()
                .tripId(trip.getId())
                .title(trip.getTitle())
                .description(trip.getDescription())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .dDay(dDay)
                .status(status)
                .build();
    }

    private DashboardResponse.TripSummary buildTripSummary(Trip trip) {
        LocalDate today = LocalDate.now();
        long dDay = ChronoUnit.DAYS.between(today, trip.getStartDate());
        String status = resolveStatus(today, trip.getStartDate(), trip.getEndDate());

        return DashboardResponse.TripSummary.builder()
                .tripId(trip.getId())
                .title(trip.getTitle())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .dDay(dDay)
                .status(status)
                .build();
    }

    private String resolveStatus(LocalDate today, LocalDate startDate, LocalDate endDate) {
        if (today.isBefore(startDate)) return "UPCOMING";
        if (!today.isAfter(endDate)) return "ONGOING";
        return "COMPLETED";
    }
}