package br.com.gregfabio.gamersbusiness.presentation.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gregfabio.gamersbusiness.application.service.ProfileService;
import br.com.gregfabio.gamersbusiness.presentation.dto.request.PatchProfileRequest;
import br.com.gregfabio.gamersbusiness.presentation.dto.response.ProfileResponse;
import jakarta.validation.Valid;

@org.springframework.validation.annotation.Validated
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/me")
public class ProfileController {
    private final ProfileService profiles;

    public ProfileController(ProfileService profiles) {
        this.profiles = profiles;
    }

    @GetMapping
    public ProfileResponse getProfile(Authentication authentication) {
        return ProfileResponse.from(profiles.getProfile(userId(authentication)));
    }

    @PatchMapping
    public ProfileResponse updateProfile(
            Authentication authentication, @Valid @RequestBody PatchProfileRequest request) {
        return ProfileResponse.from(profiles.updateProfile(
                userId(authentication),
                request.username(),
                request.email(),
                request.currentPassword(),
                request.newPassword()));
    }

    private static long userId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
