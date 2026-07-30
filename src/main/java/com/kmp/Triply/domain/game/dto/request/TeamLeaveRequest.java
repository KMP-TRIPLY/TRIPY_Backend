package com.kmp.Triply.domain.game.dto.request;

import com.kmp.Triply.domain.game.entity.TeamLeaveReasonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "팀 탈퇴 요청")
public class TeamLeaveRequest {

    @NotNull
    @Schema(description = "탈퇴 사유 유형", example = "PERSONAL_REASON")
    private TeamLeaveReasonType reasonType;

    @Size(max = 500)
    @Schema(description = "탈퇴 상세 사유", example = "일정이 생겨서 중도 탈퇴합니다.")
    private String reasonDetail;
}
