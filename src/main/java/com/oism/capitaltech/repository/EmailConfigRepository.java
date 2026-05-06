package com.oism.capitaltech.repository;

import com.oism.capitaltech.entity.EmailConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailConfigRepository extends JpaRepository<EmailConfig, Long> {

    Optional<EmailConfig> findFirstByAtivaTrue();
}
