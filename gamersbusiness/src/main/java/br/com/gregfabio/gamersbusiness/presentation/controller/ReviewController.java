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

import br.com.gregfabio.gamersbusiness.application.service.ReviewService;
import br.com.gregfabio.gamersbusiness.domain.model.PageRequest;
import br.com.gregfabio.gamersbusiness.presentation.dto.request.CreateReviewRequest;
import br.com.gregfabio.gamersbusiness.presentation.dto.request.PatchReviewRequest;
import br.com.gregfabio.gamersbusiness.presentation.dto.response.ReviewResponse;
import br.com.gregfabio.gamersbusiness.presentation.dto.response.ReviewsPageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@org.springframework.validation.annotation.Validated
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1")
public class ReviewController {
    private final ReviewService reviews;

    public ReviewController(ReviewService reviews) {
        this.reviews = reviews;
    }

    @GetMapping("/games/{gameId}/reviews")
    public ReviewsPageResponse list(
            @PathVariable @Positive long gameId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ReviewsPageResponse.from(reviews.list(gameId, new PageRequest(page, size)));
    }

    @PostMapping("/games/{gameId}/reviews")
    public ResponseEntity<ReviewResponse> create(
            Authentication authentication,
            @PathVariable @Positive long gameId,
            @Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ReviewResponse.from(
                reviews.create(userId(authentication), gameId, request.rating(), request.comment())));
    }

    @PatchMapping("/reviews/{id}")
    public ReviewResponse update(
            Authentication authentication,
            @PathVariable @Positive long id,
            @Valid @RequestBody PatchReviewRequest request) {
        return ReviewResponse.from(reviews.update(
                userId(authentication), id, request.rating(), request.comment(), request.commentProvided()));
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable @Positive long id) {
        reviews.delete(userId(authentication), id);
        return ResponseEntity.noContent().build();
    }

    private static long userId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
