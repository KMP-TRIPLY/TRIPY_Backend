package com.kmp.Triply.domain.course.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * app.seed.enabled=true 일 때만 애플리케이션 기동 시 코스 시드 데이터를 등록한다.
 * 이미 등록되어 있으면 {@link CourseSeedService}가 자동으로 건너뛰므로 반복 실행해도 안전하다.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class CourseSeedRunner implements ApplicationRunner {

    private final CourseSeedService courseSeedService;

    @Override
    public void run(ApplicationArguments args) {
        courseSeedService.seedIfNeeded();
    }
}
