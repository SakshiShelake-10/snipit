package com.snipit.snipit.controller;

import com.snipit.snipit.model.User;
import com.snipit.snipit.service.QrCodeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/urls")
public class QrController {

    private final QrCodeService qrCodeService;

    public QrController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @GetMapping("/{id}/qr")
    public ResponseEntity<byte[]> qr(@AuthenticationPrincipal User user,
                                     @PathVariable Long id,
                                     @RequestParam(defaultValue = "300") int size) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build().toUriString();

        byte[] png = qrCodeService.generatePng(user, id, baseUrl, size);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }
}