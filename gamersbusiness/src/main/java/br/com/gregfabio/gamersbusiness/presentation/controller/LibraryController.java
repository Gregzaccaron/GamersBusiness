package br.com.gregfabio.gamersbusiness.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.gregfabio.gamersbusiness.application.service.LibraryService;
import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.presentation.dto.PageResponse;
import br.com.gregfabio.gamersbusiness.presentation.dto.request.AcquireGameRequest;
import br.com.gregfabio.gamersbusiness.presentation.dto.request.LibraryHoursRequest;
import br.com.gregfabio.gamersbusiness.presentation.dto.response.LibraryResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@org.springframework.validation.annotation.Validated
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/me/library")
public class LibraryController {
    private final LibraryService library;

    public LibraryController(LibraryService library) {
        this.library = library;
    }

    @GetMapping
    public PageResponse<LibraryResponse> list(
            Authentication authentication,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return PageResponse.from(
                library.list(userId(authentication), new PageRequest(page, size)), LibraryResponse::from);
    }

    @PostMapping
    public ResponseEntity<LibraryResponse> acquire(
            Authentication authentication, @Valid @RequestBody AcquireGameRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(LibraryResponse.from(
                library.acquire(userId(authentication), request.gameId())));
    }

    @PatchMapping("/{id}")
    public LibraryResponse updateHours(
            Authentication authentication,
            @PathVariable @Positive long id,
            @Valid @RequestBody LibraryHoursRequest request) {
        return LibraryResponse.from(
                library.updateHours(userId(authentication), id, request.hoursPlayed()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(Authentication authentication, @PathVariable @Positive long id) {
        library.remove(userId(authentication), id);
        return ResponseEntity.noContent().build();
    }

    private static long userId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
