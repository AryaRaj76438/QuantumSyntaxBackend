package com.quantumsyntax.dto.response;

import com.quantumsyntax.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PostResponse {
    private String id;
    private String title;
    private String slug;
    private String excerpt;
    private String coverImageUrl;
    private UserSummaryResponse author;
    private List<String> tags;
    private String category;
    private int readingTimeMinutes;
    private int likesCount;
    private int commentsCount;
    private int viewsCount;
    private boolean isLiked;
    private boolean isBookmarked;
    private boolean isFeatured;
    private String status;
    private Instant publishedAt;
    private Instant createdAt;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserSummaryResponse {
        private String id;
        private String name;
        private String avatarUrl;
    }

    public static PostResponse from(Post post, boolean liked, boolean bookmarked) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .excerpt(post.getExcerpt())
                .coverImageUrl(post.getCoverImageUrl())
                .author(new UserSummaryResponse(
                        post.getAuthor().getId(),
                        post.getAuthor().getName(),
                        post.getAuthor().getAvatarUrl()
                ))
                .tags(post.getTags().stream().map(t -> t.getName()).toList())
                .category(post.getCategory() != null ? post.getCategory().getName() : "General")
                .readingTimeMinutes(post.getReadingTimeMinutes())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .viewsCount(post.getViewsCount())
                .isLiked(liked)
                .isBookmarked(bookmarked)
                .isFeatured(post.isFeatured())
                .status(post.getStatus().name())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
