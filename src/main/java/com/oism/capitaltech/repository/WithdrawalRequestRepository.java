package com.oism.capitaltech.repository;

import com.oism.capitaltech.entity.WithdrawalRequest;
import com.oism.capitaltech.entity.WithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {

    List<WithdrawalRequest> findByUserIdOrderByRequestedAtDesc(Long userId);

    List<WithdrawalRequest> findByStatus(WithdrawalStatus status);
}
