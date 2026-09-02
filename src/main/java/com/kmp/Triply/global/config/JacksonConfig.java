package com.kmp.Triply.global.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.type.LogicalType;

@Configuration
public class JacksonConfig {

    /**
     * 잭슨은 기본적으로 숫자를 문자열·날짜로 알아서 바꿔준다. 그래서 {"title": 12345} 는 제목이
     * "12345" 인 여행이 되고, {"startDate": 12345} 는 1970 + 12345일 = 2003-10-20 으로 조용히
     * 저장된다. 둘 다 클라이언트 버그인데 201 로 성공해서 데이터만 조용히 망가진다.
     * 숫자·불리언 → 문자열/날짜 변환만 막는다. 문자열 → 숫자("36.3" → 36.3)는 그대로 둔다.
     */
    @Bean
    public JsonMapperBuilderCustomizer strictScalarCoercion() {
        return builder -> builder
                .withCoercionConfig(LogicalType.Textual, config -> config
                        .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail))
                .withCoercionConfig(LogicalType.DateTime, config -> config
                        .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail));
    }
}
