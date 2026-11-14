package com.snipit.snipit.service;

import com.snipit.snipit.exception.PasswordRequiredException;
import com.snipit.snipit.model.Url;
import com.snipit.snipit.model.User;
import com.snipit.snipit.repository.UrlRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class UrlService {

    private static final String BASE62 =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 6;
    private static final String ALIAS_PATTERN = "^[A-Za-z0-9_-]{3,20}$";
    private static final Set<String> RESERVED_ALIASES =
            Set.of("api", "login", "register", "dashboard", "admin", "error");

    private final SecureRandom random = new SecureRandom();
    private final UrlRepository urlRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClickService clickService;

    public UrlService(UrlRepository urlRepository, PasswordEncoder passwordEncoder,
                      ClickService clickService) {
        this.urlRepository = urlRepository;
        this.passwordEncoder = passwordEncoder;
        this.clickService = clickService;
    }

    public Url createShortUrl(User owner, String originalUrl, String customAlias,
                              String password, LocalDateTime expiresAt, Long maxClicks) {
        String cleaned = validateOriginalUrl(originalUrl);
        validateExpiry(expiresAt);
        validateMaxClicks(maxClicks);

        Url url = new Url();
        url.setUser(owner);
        url.setOriginalUrl(cleaned);
        url.setShortCode(chooseShortCode(customAlias));
        url.setExpiresAt(expiresAt);
        url.setMaxClicks(maxClicks);

        if (password != null && !password.isEmpty()) {
            url.setPasswordHash(hashLinkPassword(password));
        }

        return urlRepository.save(url);
    }

    public Url updateUserUrl(User user, Long id, String originalUrl, String password,
                             LocalDateTime expiresAt, Long maxClicks) {
        Url url = urlRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Link not found"));

        String cleaned = validateOriginalUrl(originalUrl);
        validateExpiry(expiresAt);
        validateMaxClicks(maxClicks);

        if (password != null) {
            url.setPasswordHash(password.isEmpty() ? null : hashLinkPassword(password));
        }

        url.setOriginalUrl(cleaned);
        url.setExpiresAt(expiresAt);
        url.setMaxClicks(maxClicks);

        return urlRepository.save(url);
    }

    public List<Url> getUserUrls(User user) {
        return urlRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Url toggleActive(User user, Long id) {
        Url url = urlRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Link not found"));
        url.setActive(!url.isActive());
        return urlRepository.save(url);
    }

    public void deleteUserUrl(User user, Long id) {
        Url url = urlRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Link not found"));
        urlRepository.delete(url);
    }

    @Transactional
    public String resolveAndCountClick(String shortCode, String userAgent, String referer) {
        Url url = findAvailableUrl(shortCode);

        if (url.getPasswordHash() != null) {
            throw new PasswordRequiredException();
        }

        return countClickAndGetTarget(url, userAgent, referer);
    }

    @Transactional
    public String unlockAndCountClick(String shortCode, String password, String userAgent) {
        Url url = findAvailableUrl(shortCode);

        if (url.getPasswordHash() != null
                && (password == null || !passwordEncoder.matches(password, url.getPasswordHash()))) {
            throw new PasswordRequiredException();
        }

        return countClickAndGetTarget(url, userAgent, null);
    }

    private Url findAvailableUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Short link not found"));

        if (!url.isActive()) {
            throw new ResponseStatusException(HttpStatus.GONE, "This link is disabled");
        }

        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "This link has expired");
        }

        return url;
    }

    private String countClickAndGetTarget(Url url, String userAgent, String referer) {
        int updated = urlRepository.incrementClickCount(url.getId());
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.GONE, "This link has reached its click limit");
        }
        clickService.recordClick(url, userAgent, referer);
        return url.getOriginalUrl();
    }

    private String hashLinkPassword(String password) {
        if (password.length() < 4 || password.length() > 72) {
            throw new IllegalArgumentException("Link password must be between 4 and 72 characters");
        }
        return passwordEncoder.encode(password);
    }

    private String validateOriginalUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.isBlank()) {
            throw new IllegalArgumentException("URL must not be empty");
        }
        String cleaned = originalUrl.trim();
        String lower = cleaned.toLowerCase();
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            throw new IllegalArgumentException("URL must start with http:// or https://");
        }
        return cleaned;
    }

    private void validateExpiry(LocalDateTime expiresAt) {
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Expiry date must be in the future");
        }
    }

    private void validateMaxClicks(Long maxClicks) {
        if (maxClicks != null && maxClicks < 1) {
            throw new IllegalArgumentException("Max clicks must be at least 1");
        }
    }

    private String chooseShortCode(String customAlias) {
        if (customAlias == null || customAlias.isBlank()) {
            return generateUniqueCode();
        }

        String alias = customAlias.trim();
        if (!alias.matches(ALIAS_PATTERN)) {
            throw new IllegalArgumentException(
                    "Alias must be 3-20 characters: letters, numbers, - or _");
        }
        if (RESERVED_ALIASES.contains(alias.toLowerCase())) {
            throw new IllegalArgumentException("This alias is reserved");
        }
        if (urlRepository.existsByShortCode(alias)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This alias is already taken");
        }
        return alias;
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = randomCode();
        } while (urlRepository.existsByShortCode(code));
        return code;
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(BASE62.charAt(random.nextInt(BASE62.length())));
        }
        return sb.toString();
    }
}