package com.kmp.Triply.domain.course.dto.response;

import com.kmp.Triply.domain.course.entity.CourseSpot;
import com.kmp.Triply.domain.tourism.dto.response.TourismSpotDetailResponse;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CourseSpotResponse {

    private Long id;
    private short sequenceOrder;
    private String storyText;
    private BigDecimal lat;
    private BigDecimal lng;
    private int radiusMeters;
    private TourismSpotDetailResponse tourismSpot;
    private List<MissionResponse> missions;

    public static CourseSpotResponse from(CourseSpot courseSpot, List<MissionResponse> missions) {
        return CourseSpotResponse.builder()
                .id(courseSpot.getId())
                .sequenceOrder(courseSpot.getSequenceOrder())
                .storyText(courseSpot.getStoryText())
                .lat(courseSpot.getLat())
                .lng(courseSpot.getLng())
                .radiusMeters(courseSpot.getRadiusMeters())
                .tourismSpot(TourismSpotDetailResponse.from(courseSpot.getTourismSpot()))
                .missions(missions)
                .build();
    }
}
