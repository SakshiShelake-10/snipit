package com.snipit.snipit.controller;

import com.snipit.snipit.exception.PasswordRequiredException;
import com.snipit.snipit.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController {

    private final UrlService urlService;

    public RedirectController(UrlService urlService) {
        this.urlService = urlService;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<String> redirect(
            @PathVariable String shortCode,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "Referer", required = false) String referer) {
        try {
            return redirectTo(urlService.resolveAndCountClick(shortCode, userAgent, referer));
        } catch (PasswordRequiredException e) {
            return passwordPage(shortCode, false);
        }
    }

    @PostMapping("/{shortCode}/unlock")
    public ResponseEntity<String> unlock(
            @PathVariable String shortCode,
            @RequestParam("password") String password,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        try {
            return redirectTo(urlService.unlockAndCountClick(shortCode, password, userAgent));
        } catch (PasswordRequiredException e) {
            return passwordPage(shortCode, true);
        }
    }

    private ResponseEntity<String> redirectTo(String originalUrl) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    private ResponseEntity<String> passwordPage(String shortCode, boolean wrongPassword) {
        String error = wrongPassword
                ? "<p style=\"color:#b00020\">Incorrect password. Try again.</p>" : "";

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Protected link</title>
                  <style>
                    body { font-family: Arial, sans-serif; display: flex; justify-content: center; padding-top: 80px; }
                    form { width: 300px; }
                    input, button { width: 100%; padding: 10px; margin-top: 10px; box-sizing: border-box; }
                  </style>
                </head>
                <body>
                  <form method="post" action="/{{code}}/unlock">
                    <h2>This link is password protected</h2>
                    {{error}}
                    <input type="password" name="password" placeholder="Enter password" required autofocus>
                    <button type="submit">Continue</button>
                  </form>
                </body>
                </html>
                """.replace("{{code}}", shortCode).replace("{{error}}", error);

        return ResponseEntity
                .status(wrongPassword ? HttpStatus.UNAUTHORIZED : HttpStatus.OK)
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}