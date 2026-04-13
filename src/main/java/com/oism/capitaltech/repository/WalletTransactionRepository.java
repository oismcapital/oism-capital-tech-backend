package com.oism.capitaltech.repository;

import com.oism.capitaltech.entity.WalletTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    @Query("select t from WalletTransaction t where t.user.id = :userId order by t.createdAt desc, t.id desc")
    List<WalletTransaction> findLatestByUserId(@Param("userId") Long userId, Pageable pageable);
}

