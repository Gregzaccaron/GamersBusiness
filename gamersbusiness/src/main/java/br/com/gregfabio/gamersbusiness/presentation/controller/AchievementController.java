package br.com.gregfabio.gamersbusiness.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.gregfabio.gamersbusiness.application.service.CatalogService;
import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.presentation.dto.PageResponse;
import br.com.gregfabio.gamersbusiness.presentation.dto.request.AchievementRequest;
import br.com.gregfabio.gamersbusiness.presentation.dto.response.AchievementResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@org.springframework.validation.annotation.Validated
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/achievements")
public class AchievementController {
    private final CatalogService catalog;

    public AchievementController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public PageResponse<AchievementResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return PageResponse.from(catalog.achievements(new PageRequest(page, size)), AchievementResponse::from);
    }

    @GetMapping("/{id}")
    public AchievementResponse get(@PathVariable @Positive long id) {
        return AchievementResponse.from(catalog.achievement(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AchievementResponse> create(@Valid @RequestBody AchievementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(AchievementResponse.from(catalog.saveAchievement(
                null, request.gameId(), request.name(), request.description())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AchievementResponse update(
            @PathVariable @Positive long id, @Valid @RequestBody AchievementRequest request) {
        return AchievementResponse.from(catalog.saveAchievement(
                id, request.gameId(), request.name(), request.description()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable @Positive long id) {
        catalog.deleteAchievement(id);
        return ResponseEntity.noContent().build();
    }
}
