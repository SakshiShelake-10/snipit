package com.snipit.snipit.service;

import com.snipit.snipit.dto.AnalyticsResponse;
import com.snipit.snipit.dto.AnalyticsResponse.CountItem;
import com.snipit.snipit.dto.AnalyticsResponse.DailyClicks;
import com.snipit.snipit.model.Url;
import com.snipit.snipit.model.User;
import com.snipit.snipit.repository.ClickRepository;
import com.snipit.snipit.repository.UrlRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private static final int TOP_REFERRERS = 5;

    private final UrlRepository urlRepository;
    private final ClickRepository clickRepository;

    public AnalyticsService(UrlRepository urlRepository, ClickRepository clickRepository) {
        this.urlRepository = urlRepository;
        this.clickRepository = clickRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(User user, Long urlId, int days) {
        Url url = urlRepository.findByIdAndUser(urlId, user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Link not found"));

        if (days < 1 || days > 90) {
            throw new IllegalArgumentException("Days must be between 1 and 90");
        }

        long totalClicks = clickRepository.countByUrl(url);

        LocalDate firstDay = LocalDate.now().minusDays(days - 1L);
        List<LocalDateTime> clickTimes =
                clickRepository.findClickTimesSince(url, firstDay.atStartOfDay());
        Map<LocalDate, Long> perDay = clickTimes.stream()
                .collect(Collectors.groupingBy(LocalDateTime::toLocalDate, Collectors.counting()));

        List<DailyClicks> clicksPerDay = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate day = firstDay.plusDays(i);
            clicksPerDay.add(new DailyClicks(day, perDay.getOrDefault(day, 0L)));
        }

        return new AnalyticsResponse(
                totalClicks,
                clicksPerDay,
                toItems(clickRepository.browserBreakdown(url), Integer.MAX_VALUE),
                toItems(clickRepository.deviceBreakdown(url), Integer.MAX_VALUE),
                toItems(clickRepository.referrerBreakdown(url), TOP_REFERRERS));
    }

    private List<CountItem> toItems(List<Object[]> rows, int limit) {
        return rows.stream()
                .map(row -> new CountItem(
                        row[0] == null ? "Unknown" : row[0].toString(),
                        ((Number) row[1]).longValue()))
                .sorted((a, b) -> Long.compare(b.count(), a.count()))
                .limit(limit)
                .toList();
    }
}