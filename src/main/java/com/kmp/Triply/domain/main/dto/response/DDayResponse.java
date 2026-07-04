package com.kmp.Triply.domain.main.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DDayResponse {

    private Long tripId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private long dDay;
    private String status;
}