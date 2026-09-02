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
import java.util.Comparator;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            return pickPreviews(tourismApiService.getChungcheongRecommendations());
        } catch (Exception e) {
            log.warn("대시보드 추천 명소 조회 실패, 빈 목록으로 대체", e);
            return Collections.emptyList();
        }
    }

    /**
     * 인기 순위대로 고른다. 매번 무작위로 섞으면 새로고침마다 결과가 바뀌고,
     * 응답에 rank 를 내려주면서 순위를 무시하는 셈이 된다.
     *
     * <p>hubRank 는 시군구 안에서의 순위라 1 위가 시군구마다 하나씩 있다.
     * 그래서 시군구가 겹치지 않게 골라야 한 도시 명소만 세 개 나오지 않는다.
     */
    static List<DashboardResponse.SpotPreview> pickPreviews(List<RecommendationResponse> spots) {
        List<RecommendationResponse> byPopularity = new ArrayList<>(spots);
        byPopularity.sort(Comparator.comparingInt(MainServiceImpl::popularity));

        List<DashboardResponse.SpotPreview> previews = new ArrayList<>();
        Set<String> pickedSigungus = new HashSet<>();
        for (RecommendationResponse spot : byPopularity) {
            if (!pickedSigungus.add(spot.getSignguCd())) {
                continue;
            }
            previews.add(DashboardResponse.SpotPreview.builder()
                    .contentId(spot.getContentId())
                    .title(spot.getTitle())
                    .signguNm(spot.getSignguNm())
                    .categoryMiddle(spot.getCategoryMiddle())
                    .rank(spot.getRank())
                    .build());
            if (previews.size() == DASHBOARD_SPOT_PREVIEW_COUNT) {
                break;
            }
        }
        return previews;
    }

    /** hubRank 는 1 이 가장 인기다. 0 은 값이 없는 것이므로 뒤로 보낸다. */
    private static int popularity(RecommendationResponse spot) {
        return spot.getRank() <= 0 ? Integer.MAX_VALUE : spot.getRank();
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