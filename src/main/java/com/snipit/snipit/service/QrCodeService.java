package com.snipit.snipit.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.snipit.snipit.model.Url;
import com.snipit.snipit.model.User;
import com.snipit.snipit.repository.UrlRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class QrCodeService {

    private final UrlRepository urlRepository;

    public QrCodeService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public byte[] generatePng(User user, Long urlId, String baseUrl, int size) {
        Url url = urlRepository.findByIdAndUser(urlId, user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Link not found"));

        if (size < 100 || size > 1000) {
            throw new IllegalArgumentException("Size must be between 100 and 1000");
        }

        String content = baseUrl + "/" + url.getShortCode();

        try {
            BitMatrix matrix = new QRCodeWriter().encode(
                    content, BarcodeFormat.QR_CODE, size, size,
                    Map.of(EncodeHintType.MARGIN, 1));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("Could not generate QR code", e);
        }
    }
}