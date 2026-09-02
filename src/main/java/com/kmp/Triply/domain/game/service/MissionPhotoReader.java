package com.kmp.Triply.domain.game.service;

import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 업로드된 파일이 진짜 이미지인지 확인하고 바이트로 읽는다.
 * Content-Type 헤더는 클라이언트가 마음대로 보내므로 믿지 않고 파일 앞부분(매직 넘버)으로 판단한다.
 */
@Component
public class MissionPhotoReader {

    private final long maxBytes;

    public MissionPhotoReader(@Value("${mission.photo.max-bytes}") long maxBytes) {
        this.maxBytes = maxBytes;
    }

    public record Image(byte[] bytes, String contentType) {}

    public Image read(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > maxBytes) {
            throw new CustomException(ErrorCode.INVALID_PHOTO);
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INVALID_PHOTO);
        }
        String contentType = detect(bytes);
        if (contentType == null) {
            throw new CustomException(ErrorCode.INVALID_PHOTO);
        }
        return new Image(bytes, contentType);
    }

    /** JPEG: FF D8 FF, PNG: 89 50 4E 47 0D 0A 1A 0A. 그 외는 거부. */
    static String detect(byte[] bytes) {
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        if (bytes.length >= png.length) {
            for (int i = 0; i < png.length; i++) {
                if (bytes[i] != png[i]) return null;
            }
            return "image/png";
        }
        return null;
    }
}
