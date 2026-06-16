package com.kmp.Triply.domain.game.service;

import com.kmp.Triply.domain.game.dto.response.GameHistoryResponse;
import com.kmp.Triply.domain.game.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameHistoryServiceImpl implements GameHistoryService {

    private final TeamMemberRepository teamMemberRepository;

    @Override
    public List<GameHistoryResponse> getMyGameHistory(Long userId) {
        return teamMemberRepository.findByUserIdOrderByJoinedAtDesc(userId).stream()
                .map(GameHistoryResponse::from)
                .toList();
    }
}