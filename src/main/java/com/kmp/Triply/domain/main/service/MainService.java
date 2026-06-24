package com.kmp.Triply.domain.main.service;

import com.kmp.Triply.domain.main.dto.response.DashboardResponse;
import com.kmp.Triply.domain.main.dto.response.DDayResponse;

public interface MainService {

    DashboardResponse getDashboard(Long userId);

    DDayResponse getDDay(Long userId);
}