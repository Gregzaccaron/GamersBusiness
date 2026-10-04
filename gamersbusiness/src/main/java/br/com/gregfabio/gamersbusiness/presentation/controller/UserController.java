package br.com.gregfabio.gamersbusiness.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import br.com.gregfabio.gamersbusiness.application.service.ProfileService;
import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.presentation.dto.PageResponse;
import br.com.gregfabio.gamersbusiness.presentation.dto.response.ProfileResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@org.springframework.validation.annotation.Validated
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final ProfileService profiles;

    public UserController(ProfileService profiles) {
        this.profiles = profiles;
    }

    @GetMapping
    public PageResponse<ProfileResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return PageResponse.from(profiles.listUsers(new PageRequest(page, size)), ProfileResponse::from);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive long id) {
        profiles.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
