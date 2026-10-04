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
import br.com.gregfabio.gamersbusiness.presentation.dto.request.GameRequest;
import br.com.gregfabio.gamersbusiness.presentation.dto.response.GameResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@org.springframework.validation.annotation.Validated
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/games")
public class GameController {
    private final CatalogService catalog;

    public GameController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public PageResponse<GameResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) @Positive Long categoryId,
            @RequestParam(required = false) @Positive Long developerId) {
        return PageResponse.from(
                catalog.games(new PageRequest(page, size), title, categoryId, developerId), GameResponse::from);
    }

    @GetMapping("/{id}")
    public GameResponse get(@PathVariable @Positive long id) {
        return GameResponse.from(catalog.game(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GameResponse> create(@Valid @RequestBody GameRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(GameResponse.from(catalog.saveGame(
                null, request.title(), request.description(), request.price(), request.releaseDate(),
                request.developerId(), request.categoryIds())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public GameResponse update(@PathVariable @Positive long id, @Valid @RequestBody GameRequest request) {
        return GameResponse.from(catalog.saveGame(
                id, request.title(), request.description(), request.price(), request.releaseDate(),
                request.developerId(), request.categoryIds()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable @Positive long id) {
        catalog.deleteGame(id);
        return ResponseEntity.noContent().build();
    }
}
