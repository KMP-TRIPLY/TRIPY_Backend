package com.kmp.Triply.domain.reward.service;

import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.game.entity.GameStatus;
import com.kmp.Triply.domain.game.entity.Team;
import com.kmp.Triply.domain.game.entity.TeamMember;
import com.kmp.Triply.domain.game.repository.GameRoomRepository;
import com.kmp.Triply.domain.game.repository.MissionAttemptRepository;
import com.kmp.Triply.domain.game.repository.TeamMemberRepository;
import com.kmp.Triply.domain.game.repository.TeamRepository;
import com.kmp.Triply.domain.reward.dto.request.RewardSettleRequest;
import com.kmp.Triply.domain.reward.dto.response.RewardSettlementMemberResponse;
import com.kmp.Triply.domain.reward.dto.response.RewardSettlementResponse;
import com.kmp.Triply.domain.reward.dto.response.RewardSettlementTeamResponse;
import com.kmp.Triply.domain.reward.dto.response.UserCouponResponse;
import com.kmp.Triply.domain.reward.dto.response.UserRewardResponse;
import com.kmp.Triply.domain.reward.entity.Coupon;
import com.kmp.Triply.domain.reward.entity.UserCoupon;
import com.kmp.Triply.domain.reward.repository.CouponRepository;
import com.kmp.Triply.domain.reward.repository.UserCouponRepository;
import com.kmp.Triply.domain.reward.repository.UserRewardRepository;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardServiceImpl implements RewardService {

    private static final String COUPON_CODE_PREFIX = "TRIPLY";
    private static final String COUPON_CODE_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int COUPON_CODE_LENGTH = 8;

    private final UserRewardRepository userRewardRepository;
    private final UserCouponRepository userCouponRepository;
    private final CouponRepository couponRepository;
    private final GameRoomRepository gameRoomRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MissionAttemptRepository missionAttemptRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public List<UserRewardResponse> getMyRewards(Long userId) {
        return userRewardRepository.findByUserIdOrderByEarnedAtDesc(userId).stream()
                .map(UserRewardResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public RewardSettlementResponse settleRewards(RewardSettleRequest request) {
        GameRoom gameRoom = gameRoomRepository.findById(request.getGameRoomId())
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_ROOM_NOT_FOUND));
        if (gameRoom.getStatus() != GameStatus.FINISHED) {
            throw new CustomException(ErrorCode.REWARD_SETTLEMENT_UNAVAILABLE);
        }

        List<Coupon> coupons = couponRepository.findAllByIsActiveTrueOrderByValidUntilAsc();
        List<Team> teams = teamRepository.findAllByGameRoomIdOrderByTotalScoreDescCreatedAtAsc(gameRoom.getId());
        List<RewardSettlementTeamResponse> teamReports = new ArrayList<>();
        int issuedCouponCount = 0;

        for (int index = 0; index < teams.size(); index++) {
            Team team = teams.get(index);
            int rank = index + 1;
            List<TeamMember> members = teamMemberRepository.findAllByTeamId(team.getId());
            List<RewardSettlementMemberResponse> memberSettlements = getMemberSettlements(team, members);
            int activeMemberScore = getActiveMemberScore(team, members);
            int redistributableScore = getRedistributableScore(team, activeMemberScore);
            int redistributedScorePerMember = getRedistributedScorePerMember(redistributableScore, members.size());
            int redistributionRemainder = getRedistributionRemainder(redistributableScore, members.size());
            List<UserCouponResponse> issuedCoupons = settleTeamCoupons(gameRoom, team, rank, coupons, members);
            issuedCouponCount += issuedCoupons.size();
            teamReports.add(RewardSettlementTeamResponse.of(
                    team,
                    rank,
                    activeMemberScore,
                    redistributableScore,
                    redistributedScorePerMember,
                    redistributionRemainder,
                    memberSettlements,
                    issuedCoupons
            ));
        }

        return RewardSettlementResponse.of(gameRoom, issuedCouponCount, teamReports);
    }

    private List<UserCouponResponse> settleTeamCoupons(GameRoom gameRoom, Team team, int rank,
                                                       List<Coupon> coupons, List<TeamMember> members) {
        List<UserCouponResponse> issuedCoupons = new ArrayList<>();

        for (Coupon coupon : coupons) {
            if (!isCouponEligible(coupon, rank)) {
                continue;
            }
            for (TeamMember member : members) {
                issueCouponIfAbsent(coupon, member, gameRoom)
                        .map(UserCouponResponse::from)
                        .ifPresent(issuedCoupons::add);
            }
        }

        return issuedCoupons;
    }

    private List<RewardSettlementMemberResponse> getMemberSettlements(Team team, List<TeamMember> members) {
        int activeMemberScore = getActiveMemberScore(team, members);
        int redistributableScore = getRedistributableScore(team, activeMemberScore);
        int redistributedScorePerMember = getRedistributedScorePerMember(redistributableScore, members.size());
        int redistributionRemainder = getRedistributionRemainder(redistributableScore, members.size());
        List<RewardSettlementMemberResponse> memberSettlements = new ArrayList<>();

        for (int index = 0; index < members.size(); index++) {
            TeamMember member = members.get(index);
            int originalScore = missionAttemptRepository.sumScoreByTeamIdAndUserId(team.getId(), member.getUser().getId());
            int redistributedScore = redistributedScorePerMember;
            if (index == 0) {
                redistributedScore += redistributionRemainder;
            }
            memberSettlements.add(RewardSettlementMemberResponse.of(member, originalScore, redistributedScore));
        }

        return memberSettlements;
    }

    private int getActiveMemberScore(Team team, List<TeamMember> members) {
        return members.stream()
                .mapToInt(member -> missionAttemptRepository.sumScoreByTeamIdAndUserId(
                        team.getId(), member.getUser().getId()))
                .sum();
    }

    private int getRedistributableScore(Team team, int activeMemberScore) {
        int attemptedTeamScore = missionAttemptRepository.sumScoreByTeamId(team.getId());
        int baseTeamScore = Math.max(team.getTotalScore(), attemptedTeamScore);
        return Math.max(0, baseTeamScore - activeMemberScore);
    }

    private int getRedistributedScorePerMember(int redistributableScore, int memberCount) {
        if (memberCount == 0) {
            return 0;
        }
        return redistributableScore / memberCount;
    }

    private int getRedistributionRemainder(int redistributableScore, int memberCount) {
        if (memberCount == 0) {
            return 0;
        }
        return redistributableScore % memberCount;
    }

    private boolean isCouponEligible(Coupon coupon, int rank) {
        LocalDateTime now = LocalDateTime.now();
        return coupon.isActive()
                && !now.isBefore(coupon.getValidFrom())
                && !now.isAfter(coupon.getValidUntil())
                && (coupon.getMinRank() == null || rank <= coupon.getMinRank())
                && (coupon.getMaxIssueCount() == null
                    || userCouponRepository.countByCouponId(coupon.getId()) < coupon.getMaxIssueCount());
    }

    private Optional<UserCoupon> issueCouponIfAbsent(Coupon coupon, TeamMember member, GameRoom gameRoom) {
        Optional<UserCoupon> issuedCoupon = userCouponRepository.findByCouponIdAndUserIdAndGameRoomId(
                coupon.getId(), member.getUser().getId(), gameRoom.getId());
        if (issuedCoupon.isPresent()) {
            return Optional.empty();
        }
        if (coupon.getMaxIssueCount() != null
                && userCouponRepository.countByCouponId(coupon.getId()) >= coupon.getMaxIssueCount()) {
            return Optional.empty();
        }

        return Optional.of(userCouponRepository.save(UserCoupon.builder()
                .user(member.getUser())
                .coupon(coupon)
                .gameRoom(gameRoom)
                .couponCode(generateCouponCode())
                .expiresAt(coupon.getValidUntil())
                .build()));
    }

    private String generateCouponCode() {
        String couponCode;
        do {
            StringBuilder builder = new StringBuilder(COUPON_CODE_PREFIX.length() + 1 + COUPON_CODE_LENGTH);
            builder.append(COUPON_CODE_PREFIX).append("-");
            for (int index = 0; index < COUPON_CODE_LENGTH; index++) {
                builder.append(COUPON_CODE_CHARACTERS.charAt(secureRandom.nextInt(COUPON_CODE_CHARACTERS.length())));
            }
            couponCode = builder.toString();
        } while (userCouponRepository.existsByCouponCode(couponCode));
        return couponCode;
    }
}
