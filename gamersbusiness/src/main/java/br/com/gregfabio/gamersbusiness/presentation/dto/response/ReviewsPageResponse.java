package br.com.gregfabio.gamersbusiness.presentation.dto.response;

import java.util.List;

import br.com.gregfabio.gamersbusiness.application.service.ReviewService.ReviewPage;

public record ReviewsPageResponse(
        List<ReviewResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        Double averageRating,
        long reviewCount) {
    public ReviewsPageResponse {
        items = List.copyOf(items);
    }

    public static ReviewsPageResponse from(ReviewPage result) {
        return new ReviewsPageResponse(
                result.page().items().stream().map(ReviewResponse::from).toList(),
                result.page().page(),
                result.page().size(),
                result.page().totalElements(),
                result.page().totalPages(),
                result.aggregate().averageRating(),
                result.aggregate().reviewCount());
    }
}
