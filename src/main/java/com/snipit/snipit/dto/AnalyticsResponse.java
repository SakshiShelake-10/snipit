package com.snipit.snipit.dto;

import java.time.LocalDate;
import java.util.List;

public record AnalyticsResponse(
        long totalClicks,
        List<DailyClicks> clicksPerDay,
        List<CountItem> browsers,
        List<CountItem> devices,
        List<CountItem> referrers) {

    public record DailyClicks(LocalDate date, long clicks) {
    }

    public record CountItem(String name, long count) {
    }
}