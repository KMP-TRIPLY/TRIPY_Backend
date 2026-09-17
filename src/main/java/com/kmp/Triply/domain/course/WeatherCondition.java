package com.kmp.Triply.domain.course;

/**
 * 코스를 추천할 때 기준이 되는 날씨. 클라이언트가 지금 날씨를 넣으면 서버가 그에 맞는 코스를 골라 준다.
 * 날씨 자체를 서버가 조회하지는 않는다 — 어느 지역 어느 시각 기준인지는 화면 쪽이 더 잘 안다.
 */
public enum WeatherCondition {
    RAIN,
    SNOW,
    HOT,
    COLD,
    CLEAR
}
