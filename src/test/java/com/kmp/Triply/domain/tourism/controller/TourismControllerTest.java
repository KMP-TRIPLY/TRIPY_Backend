package com.kmp.Triply.domain.tourism.controller;

import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 좌표 파라미터 처리만 본다. 범위를 벗어나거나 없는 좌표는 외부 API 를 부르기 전에 끊겨야 하므로
 * 서비스는 null 이어도 된다 (닿으면 NPE 가 나서 오히려 검증이 안 걸린 걸 잡아준다).
 */
class TourismControllerTest {

    private final TourismController controller = new TourismController(null, null);

    @Test
    void 좌표가_없으면_400_으로_끊는다() {
        assertThatThrownBy(() -> nearby(null, null, null, null))
                .isInstanceOf(MissingServletRequestParameterException.class)
                .hasMessageContaining("mapX");
    }

    @Test
    void 국내_범위를_벗어난_좌표는_거부한다() {
        // QA 리포트의 mapX=1&mapY=1 — 외부 API 가 실패해 502 로 새던 케이스
        assertThatThrownBy(() -> nearby(BigDecimal.ONE, BigDecimal.ONE, null, null))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_COORDINATE);

        assertThatThrownBy(() -> nearby(null, null, new BigDecimal("127.3"), new BigDecimal("999")))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 국내_좌표는_mapX_든_longitude_든_통과한다() {
        // 검증을 통과하면 서비스(null) 를 부르다 NPE — 좌표 해석까지 갔다는 뜻
        assertThatThrownBy(() -> nearby(new BigDecimal("127.1193983"), new BigDecimal("36.4655023"), null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> nearby(null, null, new BigDecimal("127.3845"), new BigDecimal("36.3504")))
                .isInstanceOf(NullPointerException.class);
    }

    private void nearby(BigDecimal mapX, BigDecimal mapY, BigDecimal longitude, BigDecimal latitude)
            throws MissingServletRequestParameterException {
        controller.getNearbySpots(mapX, mapY, longitude, latitude, 1000, null, 0, 20);
    }
}
