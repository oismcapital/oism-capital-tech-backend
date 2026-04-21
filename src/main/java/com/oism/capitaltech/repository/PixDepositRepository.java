package com.oism.capitaltech.repository;

import com.oism.capitaltech.entity.DepositStatus;
import com.oism.capitaltech.entity.PixDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PixDepositRepository extends JpaRepository<PixDeposit, Long> {

    Optional<PixDeposit> findByTransactionId(String transactionId);

    Optional<PixDeposit> findByTransactionIdAndStatus(String transactionId, DepositStatus status);

    /** Deposits COMPLETED that have not yet had the wallet balance credited. */
    @Query("SELECT p FROM PixDeposit p WHERE p.status = 'COMPLETED' AND p.balanceCredited = false")
    List<PixDeposit> findCompletedWithoutProcessing();
}
