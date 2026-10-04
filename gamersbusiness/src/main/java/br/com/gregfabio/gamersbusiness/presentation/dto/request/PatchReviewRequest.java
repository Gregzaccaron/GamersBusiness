package br.com.gregfabio.gamersbusiness.presentation.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonSetter;

public final class PatchReviewRequest {
    @Min(1)
    @Max(5)
    private Integer rating;
    private String comment;
    private boolean ratingProvided;
    private boolean commentProvided;

    @JsonSetter("rating")
    public void setRating(Integer rating) {
        this.ratingProvided = true;
        this.rating = rating;
    }

    @JsonSetter("comment")
    public void setComment(String comment) {
        this.commentProvided = true;
        this.comment = comment;
    }

    @AssertTrue(message = "provide rating or comment; a provided rating cannot be null")
    public boolean isPatchValid() {
        return (ratingProvided || commentProvided) && (!ratingProvided || rating != null);
    }

    public Integer rating() {
        return rating;
    }

    public String comment() {
        return comment;
    }

    public boolean commentProvided() {
        return commentProvided;
    }
}
