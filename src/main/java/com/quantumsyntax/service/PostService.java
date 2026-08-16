package com.quantumsyntax.service;

import com.quantumsyntax.dto.request.CreatePostRequest;
import com.quantumsyntax.dto.response.PostResponse;
import com.quantumsyntax.entity.Category;
import com.quantumsyntax.entity.Post;
import com.quantumsyntax.entity.Tag;
import com.quantumsyntax.entity.User;
import com.quantumsyntax.exception.ResourceNotFoundException;
import com.quantumsyntax.repository.CategoryRepository;
import com.quantumsyntax.repository.CommentRepository;
import com.quantumsyntax.repository.PostRepository;
import com.quantumsyntax.repository.TagRepository;
import com.quantumsyntax.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final int WORDS_PER_MINUTE = 200;

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;

    public Page<PostResponse> listPosts(
            int page,
            int limit,
            String tag,
            String category,
            String search,
            String userEmail
    ) {
        PageRequest pageable = PageRequest.of(
                Math.max(page - 1, 0),
                limit,
                Sort.by(
                        Sort.Direction.DESC,
                        "publishedAt"
                )
        );

        Page<Post> posts = postRepository.findPublishedWithFilters(
                normalize(tag),
                normalize(category),
                normalize(search),
                pageable
        );

        return posts.map(post -> toResponse(post, userEmail));
    }

    public List<PostResponse> getFeaturedPosts(String userEmail) {
        return postRepository
                .findByStatusAndIsFeaturedTrueOrderByPublishedAtDesc(
                        Post.Status.PUBLISHED
                )
                .stream()
                .map(post -> toResponse(post, userEmail))
                .toList();
    }

    public List<PostResponse> getTrendingPosts(String userEmail) {
        return postRepository
                .findTop10ByStatusOrderByViewsCountDesc(
                        Post.Status.PUBLISHED
                )
                .stream()
                .map(post -> toResponse(post, userEmail))
                .toList();
    }

    @Transactional
    public PostResponse getPostBySlug(
            String slug,
            String userEmail
    ) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Post not found: " + slug
                        )
                );

        post.setViewsCount(post.getViewsCount() + 1);

        return toResponse(post, userEmail);
    }

    @Transactional
    public PostResponse createPost(
            CreatePostRequest request,
            String userEmail
    ) {
        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        Category category = getOrCreateCategory(
                request.getCategory()
        );

        Set<Tag> tags = getOrCreateTags(
                request.getTags()
        );

        Post.Status status = Post.Status.valueOf(
                request.getStatus()
        );

        Post post = Post.builder()
                .title(request.getTitle())
                .slug(generateUniqueSlug(request.getTitle()))
                .content(request.getContent())
                .excerpt(request.getExcerpt())
                .coverImageUrl(request.getCoverImageUrl())
                .author(author)
                .category(category)
                .tags(tags)
                .status(status)
                .readingTimeMinutes(
                        calculateReadingTime(request.getContent())
                )
                .publishedAt(
                        status == Post.Status.PUBLISHED
                                ? Instant.now()
                                : null
                )
                .build();

        post = postRepository.save(post);

        return toResponse(post, userEmail);
    }

    @Transactional
    public PostResponse updatePost(
            String slug,
            CreatePostRequest request,
            String userEmail
    ) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Post not found"
                        )
                );

        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }

        if (request.getContent() != null) {
            post.setContent(request.getContent());
            post.setReadingTimeMinutes(
                    calculateReadingTime(request.getContent())
            );
        }

        if (request.getExcerpt() != null) {
            post.setExcerpt(request.getExcerpt());
        }

        if (request.getCoverImageUrl() != null) {
            post.setCoverImageUrl(request.getCoverImageUrl());
        }

        if (request.getCategory() != null) {
            post.setCategory(
                    getOrCreateCategory(request.getCategory())
            );
        }

        if (request.getTags() != null) {
            post.setTags(
                    getOrCreateTags(request.getTags())
            );
        }

        if (request.getStatus() != null) {
            Post.Status newStatus =
                    Post.Status.valueOf(request.getStatus());

            if (
                    newStatus == Post.Status.PUBLISHED
                            && post.getPublishedAt() == null
            ) {
                post.setPublishedAt(Instant.now());
            }

            post.setStatus(newStatus);
        }

        return toResponse(post, userEmail);
    }

    @Transactional
    public void deletePost(
            String slug,
            String userEmail
    ) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Post not found"
                        )
                );

        postRepository.delete(post);
    }

    public record LikeResult(
            boolean liked,
            int likesCount
    ) {
    }

    @Transactional
    public LikeResult toggleLike(
            String slug,
            String userEmail
    ) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Post not found"
                        )
                );

        /*
         * TODO:
         * Implement a PostLike/UserLike entity.
         *
         * The current Post entity only stores likesCount,
         * so it cannot determine whether this particular
         * user has already liked the post.
         */

        post.setLikesCount(post.getLikesCount() + 1);

        return new LikeResult(
                true,
                post.getLikesCount()
        );
    }

    @Transactional
    public boolean toggleBookmark(
            String slug,
            String userEmail
    ) {
        postRepository.findBySlug(slug)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Post not found"
                        )
                );

        /*
         * TODO:
         * Implement UserBookmark/PostBookmark entity
         * and repository.
         */

        return true;
    }

    private PostResponse toResponse(
            Post post,
            String userEmail
    ) {
        return PostResponse.from(
                post,
                false,
                false
        );
    }

    private Category getOrCreateCategory(
            String categoryName
    ) {
        return categoryRepository
                .findByName(categoryName)
                .orElseGet(() -> {
                    Category category = Category.builder()
                            .name(categoryName)
                            .slug(slugify(categoryName))
                            .build();

                    return categoryRepository.save(category);
                });
    }

    private Set<Tag> getOrCreateTags(
            List<String> tagNames
    ) {
        Set<Tag> tags = new HashSet<>();

        if (tagNames == null) {
            return tags;
        }

        for (String tagName : tagNames) {
            if (tagName == null || tagName.isBlank()) {
                continue;
            }

            Tag tag = tagRepository
                    .findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = Tag.builder()
                                .name(tagName)
                                .slug(slugify(tagName))
                                .build();

                        return tagRepository.save(newTag);
                    });

            tags.add(tag);
        }

        return tags;
    }

    private int calculateReadingTime(
            String content
    ) {
        if (content == null || content.isBlank()) {
            return 1;
        }

        int wordCount = content
                .trim()
                .split("\\s+")
                .length;

        return Math.max(
                1,
                (int) Math.ceil(
                        (double) wordCount / WORDS_PER_MINUTE
                )
        );
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String slugify(String text) {
        return text
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    private String generateUniqueSlug(String title) {
        String baseSlug = slugify(title);
        String slug = baseSlug;
        int suffix = 1;

        while (postRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + suffix++;
        }

        return slug;
    }
}