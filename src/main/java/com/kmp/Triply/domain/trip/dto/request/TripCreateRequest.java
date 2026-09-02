package com.kmp.Triply.domain.trip.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class TripCreateRequest {

    // 컬럼이 varchar(255) 라 길이를 막지 않으면 DB 에서 터지고 500 으로 나간다.
    @NotBlank(message = "여행 제목은 필수입니다.")
    @Size(max = 100, message = "여행 제목은 100자를 넘을 수 없습니다.")
    @Pattern(regexp = "[^<>]*", message = "여행 제목에 < > 문자는 쓸 수 없습니다.")
    private String title;

    @Size(max = 255, message = "여행 설명은 255자를 넘을 수 없습니다.")
    private String description;

    @NotNull(message = "시작 날짜는 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료 날짜는 필수입니다.")
    private LocalDate endDate;

    /** 둘 다 있을 때만 순서를 본다. 하나라도 없으면 @NotNull 이 먼저 잡는다. */
    @JsonIgnore
    @AssertTrue(message = "종료 날짜는 시작 날짜보다 빠를 수 없습니다.")
    public boolean isValidPeriod() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }
}
