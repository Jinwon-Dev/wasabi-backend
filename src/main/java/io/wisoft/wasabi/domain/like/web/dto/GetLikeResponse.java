package io.wisoft.wasabi.domain.like.web.dto;

public record GetLikeResponse (
        boolean isLike,
        int likeCount
) {
}
