package com.snipit.snipit.controller;

import com.snipit.snipit.dto.AnalyticsResponse;
import com.snipit.snipit.model.User;
import com.snipit.snipit.service.AnalyticsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/urls")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{id}/analytics")
    public AnalyticsResponse analytics(@AuthenticationPrincipal User user,
                                       @PathVariable Long id,
                                       @RequestParam(defaultValue = "7") int days) {
        return analyticsService.getAnalytics(user, id, days);
    }
}