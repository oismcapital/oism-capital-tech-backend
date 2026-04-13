package com.oism.capitaltech.service;

import com.oism.capitaltech.dto.PixQrCodeResponse;
import com.oism.capitaltech.dto.PixRequest;
import com.oism.capitaltech.dto.PixWebhookRequest;
import com.oism.capitaltech.entity.WalletTransactionType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Service
public class PixService {

    private final RestClient restClient;
    private final UserService userService;

    @Value("${app.pix.bank-api-url}")
    private String bankApiUrl;

    @Value("${app.pix.webhook-secret}")
    private String webhookSecret;

    public PixService(RestClient restClient, UserService userService) {
        this.restClient = restClient;
        this.userService = userService;
    }

    public PixQrCodeResponse generateDynamicQrCode(PixRequest request) {
        String transactionId = UUID.randomUUID().toString();
        Map<String, Object> payload = Map.of(
                "txid", transactionId,
                "valor", request.valor(),
                "descricao", request.descricao(),
                "userId", request.userId()
        );

        try {
            restClient.post()
                    .uri(bankApiUrl + "/qrcodes/dinamicos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ignored) {
            // Mantem o backend utilizavel mesmo sem credenciais reais do banco.
        }

        return new PixQrCodeResponse(
                transactionId,
                "PIX-CODE-" + transactionId,
                "BASE64-QR-" + transactionId,
                request.valor(),
                "PENDING"
        );
    }

    @Transactional
    public void processWebhook(PixWebhookRequest request, String receivedSecret) {
        if (!webhookSecret.equals(receivedSecret)) {
            throw new IllegalArgumentException("Webhook Pix nao autorizado");
        }

        if ("CONFIRMED".equalsIgnoreCase(request.status())) {
            userService.creditBalance(
                    request.userId(),
                    request.valor(),
                    WalletTransactionType.PIX_CREDIT,
                    Map.of("transactionId", request.transactionId())
            );
        }
    }
}
