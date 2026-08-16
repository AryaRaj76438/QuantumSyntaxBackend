package com.quantumsyntax.controller;

import com.quantumsyntax.dto.request.CreatePostRequest;
import com.quantumsyntax.dto.response.PostResponse;
import com.quantumsyntax.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = getUserEmail(userDetails);

        Page<PostResponse> result =
                postService.listPosts(
                        page,
                        limit,
                        tag,
                        category,
                        search,
                        email
                );

        return ResponseEntity.ok(
                Map.of(
                        "posts", result.getContent(),
                        "total", result.getTotalElements(),
                        "page", page,
                        "totalPages", result.getTotalPages()
                )
        );
    }

    @GetMapping("/featured")
    public ResponseEntity<List<PostResponse>> getFeatured(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                postService.getFeaturedPosts(
                        getUserEmail(userDetails)
                )
        );
    }

    @GetMapping("/trending")
    public ResponseEntity<List<PostResponse>> getTrending(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                postService.getTrendingPosts(
                        getUserEmail(userDetails)
                )
        );
    }

    @GetMapping("/{slug}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable String slug,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                postService.getPostBySlug(
                        slug,
                        getUserEmail(userDetails)
                )
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WRITER')")
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PostResponse response =
                postService.createPost(
                        request,
                        userDetails.getUsername()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{slug}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WRITER')")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable String slug,
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                postService.updatePost(
                        slug,
                        request,
                        userDetails.getUsername()
                )
        );
    }

    @DeleteMapping("/{slug}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WRITER')")
    public ResponseEntity<Void> deletePost(
            @PathVariable String slug,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        postService.deletePost(
                slug,
                userDetails.getUsername()
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{slug}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable String slug,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        var result =
                postService.toggleLike(
                        slug,
                        userDetails.getUsername()
                );

        return ResponseEntity.ok(
                Map.of(
                        "liked", result.liked(),
                        "likesCount", result.likesCount()
                )
        );
    }

    @PostMapping("/{slug}/bookmark")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> toggleBookmark(
            @PathVariable String slug,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        boolean bookmarked =
                postService.toggleBookmark(
                        slug,
                        userDetails.getUsername()
                );

        return ResponseEntity.ok(
                Map.of(
                        "bookmarked", bookmarked
                )
        );
    }

    private String getUserEmail(UserDetails userDetails) {
        return userDetails != null
                ? userDetails.getUsername()
                : null;
    }
}