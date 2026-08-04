package com.kmp.Triply.domain.game.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class SpotArriveRequest {

    @NotNull
    private Long teamId;

    // 현재 위치 (skipGps=true 이면 생략 가능)
    private BigDecimal lat;
    private BigDecimal lng;

    // 로컬/테스트용 GPS 우회 플래그
    private boolean skipGps;
}