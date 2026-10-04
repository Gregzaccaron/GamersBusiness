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
import br.com.gregfabio.gamersbusiness.presentation.dto.request.DeveloperRequest;
import br.com.gregfabio.gamersbusiness.presentation.dto.response.DeveloperResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@org.springframework.validation.annotation.Validated
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/developers")
public class DeveloperController {
    private final CatalogService catalog;

    public DeveloperController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public PageResponse<DeveloperResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return PageResponse.from(catalog.developers(new PageRequest(page, size)), DeveloperResponse::from);
    }

    @GetMapping("/{id}")
    public DeveloperResponse get(@PathVariable @Positive long id) {
        return DeveloperResponse.from(catalog.developer(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeveloperResponse> create(@Valid @RequestBody DeveloperRequest request) {
        var created = catalog.saveDeveloper(null, request.name(), request.country(), request.foundationDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(DeveloperResponse.from(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DeveloperResponse update(
            @PathVariable @Positive long id, @Valid @RequestBody DeveloperRequest request) {
        return DeveloperResponse.from(
                catalog.saveDeveloper(id, request.name(), request.country(), request.foundationDate()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable @Positive long id) {
        catalog.deleteDeveloper(id);
        return ResponseEntity.noContent().build();
    }
}
