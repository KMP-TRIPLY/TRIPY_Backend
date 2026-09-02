package com.kmp.Triply.domain.trip.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * QA 리포트에서 500 으로 터지거나 그냥 통과하던 입력들이 400 으로 끊기는지 본다.
 * 컨트롤러 없이 Bean Validation 만 돌린다.
 */
class TripCreateRequestTest {

    private static final Validator VALIDATOR;

    static {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            VALIDATOR = factory.getValidator();
        }
    }

    @Test
    void 정상_입력은_통과한다() {
        assertThat(validate("공주 여행", "2026-09-01", "2026-09-03")).isEmpty();
        // 당일치기 (시작일 == 종료일) 도 정상이다
        assertThat(validate("당일치기", "2026-09-01", "2026-09-01")).isEmpty();
    }

    @Test
    void 제목이_길면_거부한다() {
        // varchar(255) 를 넘겨 DB 에서 터지던 케이스
        assertThat(messages(validate("ㄱ".repeat(1000), "2026-09-01", "2026-09-03")))
                .contains("여행 제목은 100자를 넘을 수 없습니다.");
    }

    @Test
    void 제목에_태그_문자가_있으면_거부한다() {
        assertThat(messages(validate("<script>alert(1)</script>", "2026-09-01", "2026-09-03")))
                .contains("여행 제목에 < > 문자는 쓸 수 없습니다.");
    }

    @Test
    void 종료일이_시작일보다_빠르면_거부한다() {
        assertThat(messages(validate("역전된 날짜", "2026-09-05", "2026-09-01")))
                .contains("종료 날짜는 시작 날짜보다 빠를 수 없습니다.");
    }

    @Test
    void 날짜가_없으면_순서검사는_걸리지_않는다() {
        // @NotNull 만 걸려야 한다 — 날짜 순서 메시지가 같이 나오면 안 된다
        assertThat(messages(validate("날짜 없음", null, null)))
                .doesNotContain("종료 날짜는 시작 날짜보다 빠를 수 없습니다.");
    }

    private static Set<ConstraintViolation<TripCreateRequest>> validate(String title, String start, String end) {
        TripCreateRequest request = new TripCreateRequest();
        ReflectionTestUtils.setField(request, "title", title);
        ReflectionTestUtils.setField(request, "startDate", start == null ? null : LocalDate.parse(start));
        ReflectionTestUtils.setField(request, "endDate", end == null ? null : LocalDate.parse(end));
        return VALIDATOR.validate(request);
    }

    private static Set<String> messages(Set<ConstraintViolation<TripCreateRequest>> violations) {
        return violations.stream().map(ConstraintViolation::getMessage).collect(java.util.stream.Collectors.toSet());
    }
}
