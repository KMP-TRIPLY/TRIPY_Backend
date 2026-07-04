package com.kmp.Triply.domain.tourism.service;

import com.kmp.Triply.domain.tourism.dto.response.TourismSpotDetailResponse;
import com.kmp.Triply.domain.tourism.entity.TourismSpot;
import com.kmp.Triply.domain.tourism.repository.TourismSpotRepository;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourismSpotServiceImpl implements TourismSpotService {

    private final TourismSpotRepository tourismSpotRepository;

    @Override
    public TourismSpotDetailResponse getSpotDetail(String contentId) {
        TourismSpot spot = tourismSpotRepository.findByOpenApiContentId(contentId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));
        return TourismSpotDetailResponse.from(spot);
    }
}