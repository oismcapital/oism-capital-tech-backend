package com.oism.capitaltech.service;

import com.oism.capitaltech.dto.DepositStatusResponse;
import com.oism.capitaltech.dto.GeneratePixRequest;
import com.oism.capitaltech.dto.GeneratePixResponse;
import com.oism.capitaltech.entity.DepositStatus;
import com.oism.capitaltech.entity.Plan;
import com.oism.capitaltech.entity.PixDeposit;
import com.oism.capitaltech.entity.User;
import com.oism.capitaltech.entity.WalletTransactionType;
import com.oism.capitaltech.repository.PixDepositRepository;
import com.oism.capitaltech.security.SecurityCurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PixDepositRepository pixDepositRepository;
    private final UserService userService;
    private final SecurityCurrentUser securityCurrentUser;
    private final MockPixGateway mockPixGateway;

    public PaymentService(PixDepositRepository pixDepositRepository,
                          UserService userService,
                          SecurityCurrentUser securityCurrentUser,
                          MockPixGateway mockPixGateway) {
        this.pixDepositRepository = pixDepositRepository;
        this.userService = userService;
        this.securityCurrentUser = securityCurrentUser;
        this.mockPixGateway = mockPixGateway;
    }

    @Transactional
    public GeneratePixResponse generatePix(GeneratePixRequest request) {
        Plan plan = resolvePlan(request.planId());
        User user = userService.getByEmail(securityCurrentUser.email());

        String transactionId = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.MINUTES);

        // Chama o gateway (mock por enquanto)
        MockPixGateway.PixPayload payload = mockPixGateway.generateDynamicQrCode(
                transactionId, plan.getAmount(), plan.getDisplayName(), user.getId()
        );

        // Persiste a transação de forma atômica
        PixDeposit deposit = new PixDeposit();
        deposit.setTransactionId(transactionId);
        deposit.setUser(user);
        deposit.setPlan(plan);
        deposit.setAmount(plan.getAmount());
        deposit.setStatus(DepositStatus.PENDING);
        deposit.setCopyAndPaste(payload.copyAndPaste());
        deposit.setQrCodeBase64(payload.qrCodeBase64());
        deposit.setExpiresAt(expiresAt);

        pixDepositRepository.save(deposit);

        log.info("Pix gerado: transactionId={} userId={} plan={} amount={}",
                transactionId, user.getId(), plan.name(), plan.getAmount());

        return new GeneratePixResponse(
                transactionId,
                plan.name(),
                plan.getDisplayName(),
                plan.getAmount(),
                payload.copyAndPaste(),
                payload.qrCodeBase64(),
                DepositStatus.PENDING.name(),
                expiresAt.toString()
        );
    }

    @Transactional(readOnly = true)
    public DepositStatusResponse getStatus(String transactionId) {
        PixDeposit deposit = pixDepositRepository
                .findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transação não encontrada: " + transactionId));

        // Marca como expirado se passou do prazo e ainda está pendente
        if (deposit.getStatus() == DepositStatus.PENDING
                && deposit.getExpiresAt() != null
                && Instant.now().isAfter(deposit.getExpiresAt())) {
            deposit.setStatus(DepositStatus.EXPIRED);
            pixDepositRepository.save(deposit);
        }

        return DepositStatusResponse.fromEntity(deposit);
    }

    @Transactional
    public void confirmPayment(String transactionId) {
        PixDeposit deposit = pixDepositRepository
                .findByTransactionIdAndStatus(transactionId, DepositStatus.PENDING)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transação não encontrada ou já processada: " + transactionId));

        deposit.setStatus(DepositStatus.COMPLETED);
        deposit.setCompletedAt(Instant.now());
        pixDepositRepository.save(deposit);

        userService.creditBalance(
                deposit.getUser().getId(),
                deposit.getAmount(),
                WalletTransactionType.PIX_CREDIT,
                Map.of(
                        "transactionId", transactionId,
                        "plan", deposit.getPlan().name()
                )
        );

        log.info("Pagamento confirmado: transactionId={} userId={} amount={}",
                transactionId, deposit.getUser().getId(), deposit.getAmount());
    }

    private Plan resolvePlan(String planId) {
        try {
            return Plan.valueOf(planId.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new PlanNotFoundException("Plano não encontrado: " + planId);
        }
    }

    public static class PlanNotFoundException extends RuntimeException {
        public PlanNotFoundException(String message) { super(message); }
    }
}
