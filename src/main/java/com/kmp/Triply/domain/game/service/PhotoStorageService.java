package com.kmp.Triply.domain.game.service;

import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 미션 인증 사진을 OCI Object Storage 에 올린다. OCI 는 S3 호환 API 를 제공하므로 AWS SDK 를 그대로 쓴다.
 * S3 호환 API 는 인스턴스 프린시펄을 지원하지 않아 Customer Secret Key 가 필요하다.
 */
@Slf4j
@Service
public class PhotoStorageService {

    private final String bucket;
    private final S3Client client;

    public PhotoStorageService(
            @Value("${mission.photo.storage.endpoint}") String endpoint,
            @Value("${mission.photo.storage.region}") String region,
            @Value("${mission.photo.storage.bucket}") String bucket,
            @Value("${mission.photo.storage.access-key}") String accessKey,
            @Value("${mission.photo.storage.secret-key}") String secretKey) {
        this.bucket = bucket;
        this.client = configured(endpoint, bucket, accessKey, secretKey)
                ? build(endpoint, region, accessKey, secretKey)
                : null;
        if (this.client == null) {
            log.warn("사진 저장소 설정이 비어 있습니다. 사진 미션 제출은 거부됩니다. (OCI_S3_* 환경변수 확인)");
        }
    }

    public boolean isConfigured() {
        return client != null;
    }

    /** 업로드 후 객체 키를 돌려준다. 공개 URL 이 아니라 키만 남긴다 — 사진은 인증된 경로로만 다시 꺼낸다. */
    public String upload(byte[] image, String contentType, Long missionId, Long teamId) {
        if (client == null) {
            throw new CustomException(ErrorCode.PHOTO_STORAGE_UNAVAILABLE);
        }
        String key = "missions/%s/%d/team-%d/%s%s".formatted(
                LocalDate.now(), missionId, teamId, UUID.randomUUID(), extensionFor(contentType));
        try {
            client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(image));
        } catch (Exception e) {
            log.error("사진 업로드 실패. key={}", key, e);
            throw new CustomException(ErrorCode.PHOTO_STORAGE_UNAVAILABLE);
        }
        return key;
    }

    private static boolean configured(String endpoint, String bucket, String accessKey, String secretKey) {
        return StringUtils.hasText(endpoint) && StringUtils.hasText(bucket)
                && StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey);
    }

    private static S3Client build(String endpoint, String region, String accessKey, String secretKey) {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                // OCI 의 S3 호환 엔드포인트는 경로 방식만 안전하게 동작한다
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    private static String extensionFor(String contentType) {
        return "image/png".equals(contentType) ? ".png" : ".jpg";
    }

    @PreDestroy
    void close() {
        if (client != null) {
            client.close();
        }
    }
}
