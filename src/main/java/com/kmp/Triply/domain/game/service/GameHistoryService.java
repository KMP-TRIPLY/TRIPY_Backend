package com.kmp.Triply.domain.game.service;

import com.kmp.Triply.domain.game.dto.response.GameHistoryResponse;

import java.util.List;

public interface GameHistoryService {

    List<GameHistoryResponse> getMyGameHistory(Long userId);
}
