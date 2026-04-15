package com.oism.capitaltech.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.oism.capitaltech.entity.PixConfig;
import com.oism.capitaltech.repository.PixConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.Map;

/**
 * Gateway Pix estático.
 * Gera o payload EMV e o QR Code PNG a partir da configuração ativa em pix_config.
 * Para integrar com Efí/Mercado Pago no futuro, basta criar outra implementação
 * e trocar o bean no PaymentService.
 */
@Service
public class MockPixGateway {

    private static final Logger log = LoggerFactory.getLogger(MockPixGateway.class);
    private static final int QR_SIZE = 300;

    private final PixConfigRepository pixConfigRepository;

    public MockPixGateway(PixConfigRepository pixConfigRepository) {
        this.pixConfigRepository = pixConfigRepository;
    }

    public PixPayload generateDynamicQrCode(String transactionId,
                                            BigDecimal amount,
                                            String description,
                                            Long userId) {
        PixConfig config = pixConfigRepository.findFirstByAtivaTrue()
                .orElseThrow(() -> new IllegalStateException(
                        "Nenhuma configuração Pix ativa encontrada. Cadastre em /api/v1/admin/pix-config"));

        String valor = amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();

        String emvPayload = PixEmvBuilder.build(
                config.getChavePix(),
                config.getNomeRecebedor(),
                config.getCidade(),
                valor,
                transactionId
        );

        String qrCodeBase64 = generateQrCodeBase64(emvPayload);

        log.debug("[PIX] txid={} chave={} valor={}", transactionId, config.getChavePix(), valor);

        return new PixPayload(emvPayload, qrCodeBase64);
    }

    private String generateQrCodeBase64(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 1
            );
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            log.error("Erro ao gerar QR Code PNG", e);
            throw new IllegalStateException("Falha ao gerar imagem do QR Code", e);
        }
    }

    public record PixPayload(String copyAndPaste, String qrCodeBase64) {}
}
