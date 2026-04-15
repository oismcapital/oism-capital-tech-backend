package com.oism.capitaltech.controller;

import com.oism.capitaltech.dto.PixConfigRequest;
import com.oism.capitaltech.dto.PixConfigResponse;
import com.oism.capitaltech.entity.PixConfig;
import com.oism.capitaltech.repository.PixConfigRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/pix-config")
public class PixConfigController {

    private final PixConfigRepository pixConfigRepository;

    public PixConfigController(PixConfigRepository pixConfigRepository) {
        this.pixConfigRepository = pixConfigRepository;
    }

    @GetMapping
    public ResponseEntity<PixConfigResponse> get() {
        return pixConfigRepository.findFirstByAtivaTrue()
                .map(PixConfigResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Desativa a config anterior e salva a nova como ativa.
     */
    @PostMapping
    @Transactional
    public ResponseEntity<PixConfigResponse> upsert(@Valid @RequestBody PixConfigRequest request) {
        pixConfigRepository.findAll().forEach(c -> {
            c.setAtiva(false);
            pixConfigRepository.save(c);
        });

        PixConfig config = new PixConfig();
        config.setChavePix(request.chavePix());
        config.setNomeRecebedor(request.nomeRecebedor());
        config.setCidade(request.cidade());
        config.setAtiva(true);

        return ResponseEntity.ok(PixConfigResponse.fromEntity(pixConfigRepository.save(config)));
    }
}
