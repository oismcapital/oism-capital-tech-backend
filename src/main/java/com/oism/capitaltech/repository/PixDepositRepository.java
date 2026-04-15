package com.oism.capitaltech.repository;

import com.oism.capitaltech.entity.DepositStatus;
import com.oism.capitaltech.entity.PixDeposit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PixDepositRepository extends JpaRepository<PixDeposit, Long> {

    Optional<PixDeposit> findByTransactionId(String transactionId);

    Optional<PixDeposit> findByTransactionIdAndStatus(String transactionId, DepositStatus status);
}
