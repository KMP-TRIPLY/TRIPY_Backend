package com.kmp.Triply.domain.course.service;

import com.kmp.Triply.domain.course.entity.Course;
import com.kmp.Triply.domain.course.repository.CourseRepository;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 코스 삭제는 비활성화다. 만든 사람만 할 수 있고, 없는 코스는 404.
 * 삭제 경로에서 쓰이는 의존성은 CourseRepository 뿐이라 나머지는 null.
 */
class CourseDeleteTest {

    private static final long OWNER_ID = 3L;
    private static final long OTHER_ID = 9L;

    private final CourseRepository courseRepository = mock(CourseRepository.class);
    private final CourseServiceImpl service =
            new CourseServiceImpl(courseRepository, null, null, null, null, null);

    @Test
    void 만든_사람이_지우면_비활성화된다() {
        Course course = course(OWNER_ID);
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));

        service.deleteCourse(OWNER_ID, 4L);

        assertThat(course.isActive()).isFalse();
    }

    @Test
    void 남의_코스는_지울_수_없다() {
        Course course = course(OWNER_ID);
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> service.deleteCourse(OTHER_ID, 4L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.COURSE_ACCESS_DENIED);
        assertThat(course.isActive()).isTrue();
    }

    @Test
    void 만든_사람이_없는_코스는_아무도_못_지운다() {
        Course course = course(null);
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> service.deleteCourse(OWNER_ID, 1L))
                .isInstanceOf(CustomException.class);
        assertThat(course.isActive()).isTrue();
    }

    @Test
    void 없는_코스는_404() {
        when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteCourse(OWNER_ID, 99999999L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.COURSE_NOT_FOUND);
    }

    private static Course course(Long creatorId) {
        User creator = null;
        if (creatorId != null) {
            creator = User.builder().build();
            ReflectionTestUtils.setField(creator, "id", creatorId);
        }
        return Course.builder()
                .title("테스트 코스")
                .regionCode("44")
                .city("공주")
                .createdBy(creator)
                .build();
    }
}
