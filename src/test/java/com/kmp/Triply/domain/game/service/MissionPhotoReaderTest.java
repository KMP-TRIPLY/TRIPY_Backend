package com.kmp.Triply.domain.game.service;

import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Content-Type 헤더는 클라이언트가 조작할 수 있으므로 매직 넘버로만 판단하는지 본다.
 */
class MissionPhotoReaderTest {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};
    private static final byte[] GIF = {'G', 'I', 'F', '8', '9', 'a', 0, 0};

    private final MissionPhotoReader reader = new MissionPhotoReader(1000);

    @Test
    void JPEG_와_PNG_만_통과한다() {
        assertThat(reader.read(upload("a.jpg", "image/jpeg", JPEG)).contentType()).isEqualTo("image/jpeg");
        assertThat(reader.read(upload("a.png", "image/png", PNG)).contentType()).isEqualTo("image/png");
    }

    @Test
    void 확장자와_헤더를_속여도_내용으로_거부한다() {
        // .jpg 에 image/jpeg 를 달았지만 실제 내용은 GIF / 텍스트
        assertReject(upload("fake.jpg", "image/jpeg", GIF));
        assertReject(upload("fake.jpg", "image/jpeg", "not an image at all".getBytes()));
    }

    @Test
    void 빈_파일과_크기_초과는_거부한다() {
        assertReject(upload("empty.jpg", "image/jpeg", new byte[0]));

        byte[] tooBig = new byte[1001];
        System.arraycopy(JPEG, 0, tooBig, 0, JPEG.length);
        assertReject(upload("big.jpg", "image/jpeg", tooBig));
    }

    private void assertReject(MockMultipartFile file) {
        assertThatThrownBy(() -> reader.read(file))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PHOTO);
    }

    private static MockMultipartFile upload(String name, String contentType, byte[] content) {
        return new MockMultipartFile("photo", name, contentType, content);
    }
}
