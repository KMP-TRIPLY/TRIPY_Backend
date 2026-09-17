package com.kmp.Triply.domain.course.entity;

/** 코스가 실내에서 진행되는 정도. 비·폭염·한파 때 무엇을 추천할지 가르는 기준이다. */
public enum IndoorType {

    /** 모든 스팟이 실내. 비가 와도 일정이 그대로 굴러간다. */
    INDOOR,
    /** 실내 스팟과 야외 스팟이 섞여 있다. 우산이 있으면 가능하다. */
    MIXED,
    /** 전부 야외. 비가 오면 미루는 편이 낫다. */
    OUTDOOR
}
