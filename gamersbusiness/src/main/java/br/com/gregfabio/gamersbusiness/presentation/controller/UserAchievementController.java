package br.com.gregfabio.gamersbusiness.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.gregfabio.gamersbusiness.application.service.UserAchievementService;
import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.presentation.dto.PageResponse;
import br.com.gregfabio.gamersbusiness.presentation.dto.request.UnlockAchievementRequest;
import br.com.gregfabio.gamersbusiness.presentation.dto.response.UserAchievementResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@org.springframework.validation.annotation.Validated
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/me/achievements")
public class UserAchievementController {
    private final UserAchievementService achievements;

    public UserAchievementController(UserAchievementService achievements) {
        this.achievements = achievements;
    }

    @GetMapping
    public PageResponse<UserAchievementResponse> list(
            Authentication authentication,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) @Positive Long gameId) {
        return PageResponse.from(achievements.list(
                userId(authentication), gameId, new PageRequest(page, size)), UserAchievementResponse::from);
    }

    @PostMapping
    public ResponseEntity<UserAchievementResponse> unlock(
            Authentication authentication, @Valid @RequestBody UnlockAchievementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(UserAchievementResponse.from(
                achievements.unlock(userId(authentication), request.achievementId())));
    }

    private static long userId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
