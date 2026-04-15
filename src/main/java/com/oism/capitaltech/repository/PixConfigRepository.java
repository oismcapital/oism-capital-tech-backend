package com.oism.capitaltech.repository;

import com.oism.capitaltech.entity.PixConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PixConfigRepository extends JpaRepository<PixConfig, Long> {

    Optional<PixConfig> findFirstByAtivaTrue();
}
