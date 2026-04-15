package com.oism.capitaltech.service;

/**
 * Monta o payload EMV de um Pix estático conforme especificação do Banco Central.
 * Referência: https://www.bcb.gov.br/content/estabilidadefinanceira/pix/Regulamento_Pix/II_ManualdePadroesparaIniciacaodoPix.pdf
 */
public final class PixEmvBuilder {

    private PixEmvBuilder() {}

    /**
     * @param chavePix     Chave Pix (e-mail, CPF, CNPJ, telefone ou aleatória)
     * @param nomeRecebedor Nome do recebedor (máx 25 caracteres)
     * @param cidade        Cidade do recebedor (máx 15 caracteres)
     * @param valor         Valor da transação (null = valor livre)
     * @param txId          Identificador da transação (máx 25 chars, sem espaços)
     */
    public static String build(String chavePix,
                               String nomeRecebedor,
                               String cidade,
                               String valor,
                               String txId) {
        String nome = truncate(nomeRecebedor, 25);
        String cid  = truncate(cidade, 15);
        String tx   = truncate(sanitize(txId), 25);

        // ID 26 — Merchant Account Information (Pix)
        String gui        = field("00", "BR.GOV.BCB.PIX");
        String chave      = field("01", chavePix);
        String merchantInfo = field("26", gui + chave);

        // ID 54 — valor (opcional)
        String valorField = (valor != null && !valor.isBlank())
                ? field("54", valor)
                : "";

        // ID 62 — Additional Data Field (txid)
        String txIdField      = field("05", tx);
        String additionalData = field("62", txIdField);

        // Monta o payload sem o CRC (últimos 4 chars)
        String payload =
                field("00", "01")           // Payload Format Indicator
                + field("01", "12")          // Point of Initiation Method (12 = estático)
                + merchantInfo
                + field("52", "0000")        // Merchant Category Code
                + field("53", "986")         // Transaction Currency (BRL)
                + valorField
                + field("58", "BR")          // Country Code
                + field("59", nome)          // Merchant Name
                + field("60", cid)           // Merchant City
                + additionalData
                + "6304";                    // CRC placeholder

        return payload + crc16(payload);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static String field(String id, String value) {
        String len = String.format("%02d", value.length());
        return id + len + value;
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) : s;
    }

    private static String sanitize(String s) {
        if (s == null) return "***";
        return s.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    /**
     * CRC-16/CCITT-FALSE conforme especificação do Banco Central.
     */
    static String crc16(String payload) {
        int crc = 0xFFFF;
        byte[] bytes = payload.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        for (byte b : bytes) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
                crc &= 0xFFFF;
            }
        }
        return String.format("%04X", crc);
    }
}
