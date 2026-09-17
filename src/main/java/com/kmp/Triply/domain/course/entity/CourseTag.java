package com.kmp.Triply.domain.course.entity;

/**
 * 코스 성격 태그. "비 올 때 갈 만한 코스"처럼 상황으로 코스를 고를 때 쓴다.
 * 실내/야외 구분은 {@link IndoorType} 이 따로 맡는다 — 태그는 취향, 실내 여부는 사실이다.
 */
public enum CourseTag {

    /** 비·눈이 와도 진행에 무리가 없다. */
    RAINY_DAY,
    /** 한여름 땡볕을 피할 수 있다. */
    HOT_DAY,
    /** 한겨울 추위를 피할 수 있다. */
    COLD_DAY,
    /** 아이와 함께 다니기 좋다. */
    KID_FRIENDLY,
    /** 이동과 걷는 양이 적다. */
    LIGHT_WALK,
    /** 대중교통·도보로 다닐 만하다. */
    CAR_FREE,
    HISTORY,
    NATURE,
    MUSEUM,
    FOOD,
    /** 노을·일몰이 하이라이트. */
    SUNSET,
    /** 사진 찍기 좋은 곳이 많다. */
    PHOTO_SPOT
}
