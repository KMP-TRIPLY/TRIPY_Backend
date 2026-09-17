package com.kmp.Triply.domain.course.dto.request;

import com.kmp.Triply.domain.course.entity.CourseTag;
import com.kmp.Triply.domain.course.entity.CourseType;
import com.kmp.Triply.domain.course.entity.Difficulty;
import com.kmp.Triply.domain.course.entity.IndoorType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
public class CourseCreateRequest {

    @NotBlank(message = "코스 제목은 필수입니다.")
    @Size(max = 200)
    private String title;

    private String description;

    /**
     * 국가행정표준 시도 코드. 비워 두면 city 로 서버가 찾는다.
     * 클라이언트가 코드를 모르는 것이 정상이므로 필수가 아니다.
     */
    @Size(max = 10)
    private String regionCode;

    @NotBlank(message = "도시명은 필수입니다.")
    @Size(max = 50)
    private String city;

    @NotNull
    private Difficulty difficulty = Difficulty.NORMAL;

    @Min(1)
    private int estimatedMinutes = 120;

    @NotNull
    private CourseType courseType = CourseType.GENERAL;

    /** 비워 두면 MIXED. 비 오는 날 추천에서 빠지지 않게 하려면 정확히 넣는 편이 좋다. */
    @NotNull
    private IndoorType indoorType = IndoorType.MIXED;

    /** 상황·취향 태그. 비워 둘 수 있다. */
    private Set<CourseTag> tags = new LinkedHashSet<>();
}
