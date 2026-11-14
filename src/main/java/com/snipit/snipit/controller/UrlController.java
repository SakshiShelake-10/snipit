package com.snipit.snipit.controller;

import com.snipit.snipit.dto.CreateUrlRequest;
import com.snipit.snipit.dto.UpdateUrlRequest;
import com.snipit.snipit.dto.UrlResponse;
import com.snipit.snipit.model.Url;
import com.snipit.snipit.model.User;
import com.snipit.snipit.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/urls")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping
    public ResponseEntity<UrlResponse> create(@AuthenticationPrincipal User user,
                                              @RequestBody CreateUrlRequest request) {
        Url url = urlService.createShortUrl(user, request.originalUrl(), request.customAlias(),
                request.password(), request.expiresAt(), request.maxClicks());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(url));
    }

    @GetMapping
    public List<UrlResponse> list(@AuthenticationPrincipal User user) {
        return urlService.getUserUrls(user).stream().map(this::toResponse).toList();
    }

    @PutMapping("/{id}")
    public UrlResponse update(@AuthenticationPrincipal User user, @PathVariable Long id,
                              @RequestBody UpdateUrlRequest request) {
        Url url = urlService.updateUserUrl(user, id, request.originalUrl(),
                request.password(), request.expiresAt(), request.maxClicks());
        return toResponse(url);
    }

    @PatchMapping("/{id}/toggle")
    public UrlResponse toggle(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return toResponse(urlService.toggleActive(user, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user,
                                       @PathVariable Long id) {
        urlService.deleteUserUrl(user, id);
        return ResponseEntity.noContent().build();
    }

    private UrlResponse toResponse(Url url) {
        String shortUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/{code}")
                .buildAndExpand(url.getShortCode())
                .toUriString();

        return new UrlResponse(
                url.getId(),
                url.getOriginalUrl(),
                url.getShortCode(),
                shortUrl,
                url.getCreatedAt(),
                url.getExpiresAt(),
                url.getClickCount(),
                url.getMaxClicks(),
                url.isActive(),
                url.getPasswordHash() != null);
    }
}