package com.oism.capitaltech.repository;

import com.oism.capitaltech.entity.WalletTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    @Query("select t from WalletTransaction t where t.user.id = :userId order by t.createdAt desc, t.id desc")
    List<WalletTransaction> findLatestByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("select t from WalletTransaction t where t.user.id = :userId and t.createdAt >= :from and t.createdAt <= :to order by t.createdAt desc, t.id desc")
    List<WalletTransaction> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("from") Instant from,
            @Param("to") Instant to);

    /** Sum of INTEREST_WITHDRAWAL amounts for a specific investment (by investmentId in metadataJson). */
    @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM wallet_transactions WHERE user_id = :userId AND type = 'INTEREST_WITHDRAWAL' AND metadata_json::text LIKE CONCAT('%\"investmentId\":\"', CAST(:investmentId AS text), '\"%')", nativeQuery = true)
    java.math.BigDecimal sumWithdrawnInterestForInvestment(
            @Param("userId") Long userId,
            @Param("investmentId") String investmentId);
}

