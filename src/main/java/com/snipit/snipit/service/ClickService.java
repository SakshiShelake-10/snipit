package com.snipit.snipit.service;

import com.snipit.snipit.model.Click;
import com.snipit.snipit.model.Url;
import com.snipit.snipit.repository.ClickRepository;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class ClickService {

    private static final String[] BOT_KEYWORDS =
            {"bot", "crawl", "spider", "facebookexternalhit", "whatsapp"};

    private final ClickRepository clickRepository;

    public ClickService(ClickRepository clickRepository) {
        this.clickRepository = clickRepository;
    }

    public void recordClick(Url url, String userAgent, String referer) {
        Click click = new Click();
        click.setUrl(url);
        click.setBrowser(detectBrowser(userAgent));
        click.setDeviceType(detectDevice(userAgent));
        click.setReferrer(extractReferrerHost(referer));
        clickRepository.save(click);
    }

    private boolean isBot(String userAgent) {
        String ua = userAgent.toLowerCase();
        for (String keyword : BOT_KEYWORDS) {
            if (ua.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String detectBrowser(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) return "Unknown";
        if (isBot(userAgent)) return "Bot";
        if (userAgent.contains("Edg")) return "Edge";
        if (userAgent.contains("OPR/") || userAgent.contains("Opera")) return "Opera";
        if (userAgent.contains("SamsungBrowser")) return "Samsung Internet";
        if (userAgent.contains("Firefox/") || userAgent.contains("FxiOS")) return "Firefox";
        if (userAgent.contains("Chrome/") || userAgent.contains("CriOS")) return "Chrome";
        if (userAgent.contains("Safari/")) return "Safari";
        return "Other";
    }

    private String detectDevice(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) return "Unknown";
        if (isBot(userAgent)) return "Bot";
        String ua = userAgent.toLowerCase();
        if (ua.contains("ipad") || ua.contains("tablet")
                || (ua.contains("android") && !ua.contains("mobile"))) {
            return "Tablet";
        }
        if (ua.contains("mobi") || ua.contains("iphone") || ua.contains("android")) {
            return "Mobile";
        }
        return "Desktop";
    }

    private String extractReferrerHost(String referer) {
        if (referer == null || referer.isBlank()) return "Direct";
        try {
            String host = URI.create(referer.trim()).getHost();
            if (host == null || host.isBlank()) return "Direct";
            host = host.toLowerCase();
            if (host.startsWith("www.")) host = host.substring(4);
            return host.length() > 255 ? host.substring(0, 255) : host;
        } catch (IllegalArgumentException e) {
            return "Direct";
        }
    }
}