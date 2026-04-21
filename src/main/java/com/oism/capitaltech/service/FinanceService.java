package com.oism.capitaltech.service;

import com.oism.capitaltech.dto.FinanceSummaryResponse;
import com.oism.capitaltech.entity.Investment;
import com.oism.capitaltech.entity.InvestmentStatus;
import com.oism.capitaltech.entity.User;
import com.oism.capitaltech.repository.InvestmentRepository;
import com.oism.capitaltech.security.SecurityCurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class FinanceService {

    private final UserService userService;
    private final SecurityCurrentUser securityCurrentUser;
    private final InvestmentRepository investmentRepository;

    public FinanceService(UserService userService,
                          SecurityCurrentUser securityCurrentUser,
                          InvestmentRepository investmentRepository) {
        this.userService = userService;
        this.securityCurrentUser = securityCurrentUser;
        this.investmentRepository = investmentRepository;
    }

    @Transactional(readOnly = true)
    public FinanceSummaryResponse getSummary() {
        User user = userService.getByEmail(securityCurrentUser.email());

        List<Investment> active = investmentRepository
                .findByUserIdAndStatusOrderByContractedAtDesc(user.getId(), InvestmentStatus.ACTIVE);

        BigDecimal totalInvested = active.stream()
                .map(Investment::getPrincipal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal totalAccrued = active.stream()
                .map(Investment::getAccruedInterest)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        List<Double> points = parsePerformancePoints(user.getHistoricoRendimentoJSONB());

        return new FinanceSummaryResponse(
                user.getSaldo(),
                totalInvested,
                totalAccrued,
                user.getLucroHoje(),
                points,
                user.isValorEscondido()
        );
    }

    private List<Double> parsePerformancePoints(String historicoJson) {
        if (historicoJson == null || historicoJson.isBlank() || "[]".equals(historicoJson.trim())) {
            return List.of();
        }
        try {
            List<Double> points = new ArrayList<>();
            String content = historicoJson.trim();
            if (!content.startsWith("[")) return List.of();
            content = content.substring(1, content.length() - 1).trim();
            if (content.isEmpty()) return List.of();

            int depth = 0;
            int start = 0;
            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == '{') { if (depth == 0) start = i; depth++; }
                else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        String obj = content.substring(start, i + 1);
                        Double val = extractValor(obj);
                        if (val != null) points.add(val);
                    }
                }
            }
            return points;
        } catch (Exception e) {
            return List.of();
        }
    }

    private Double extractValor(String obj) {
        int idx = obj.indexOf("\"valor\"");
        if (idx < 0) return null;
        int colon = obj.indexOf(':', idx);
        if (colon < 0) return null;
        int end = obj.indexOf('}', colon);
        String raw = obj.substring(colon + 1, end).trim().replace(",", "").replace("\"", "");
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
