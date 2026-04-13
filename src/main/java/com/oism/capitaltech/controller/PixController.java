package com.oism.capitaltech.controller;

import com.oism.capitaltech.dto.PixQrCodeResponse;
import com.oism.capitaltech.dto.PixRequest;
import com.oism.capitaltech.dto.PixWebhookRequest;
import com.oism.capitaltech.service.PixService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/pix")
public class PixController {

    private final PixService pixService;

    public PixController(PixService pixService) {
        this.pixService = pixService;
    }

    @PostMapping("/qrcode")
    public ResponseEntity<PixQrCodeResponse> generateQrCode(@Valid @RequestBody PixRequest request) {
        return ResponseEntity.ok(pixService.generateDynamicQrCode(request));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> webhook(@Valid @RequestBody PixWebhookRequest request,
                                                       @RequestHeader(name = "X-Webhook-Secret", defaultValue = "")
                                                       String webhookSecret) {
        pixService.processWebhook(request, webhookSecret);
        return ResponseEntity.ok(Map.of("message", "Webhook processado com sucesso"));
    }
}
