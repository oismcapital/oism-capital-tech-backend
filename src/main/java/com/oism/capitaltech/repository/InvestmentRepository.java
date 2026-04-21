package com.oism.capitaltech.repository;

import com.oism.capitaltech.entity.Investment;
import com.oism.capitaltech.entity.InvestmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvestmentRepository extends JpaRepository<Investment, Long> {

    List<Investment> findByUserIdOrderByContractedAtDesc(Long userId);

    List<Investment> findByUserIdAndStatusOrderByContractedAtDesc(Long userId, InvestmentStatus status);

    /** Todos os contratos ACTIVE que ainda não tiveram accrual hoje e estão dentro dos 30 dias de rentabilidade. */
    @Query("""
            SELECT i FROM Investment i
            WHERE i.status = 'ACTIVE'
              AND (i.lastAccrualDate IS NULL OR i.lastAccrualDate < :today)
              AND i.contractedAt >= :cutoff
            """)
    List<Investment> findActiveForAccrual(@Param("today") LocalDate today,
                                          @Param("cutoff") java.time.LocalDateTime cutoff);

    /** Contratos que atingiram maturação (D+35) e ainda estão ACTIVE. */
    @Query("SELECT i FROM Investment i WHERE i.status = 'ACTIVE' AND i.maturityDate <= :today")
    List<Investment> findMaturedContracts(@Param("today") LocalDate today);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Investment i WHERE i.id = :id")
    Optional<Investment> findByIdWithLock(@Param("id") Long id);
}
