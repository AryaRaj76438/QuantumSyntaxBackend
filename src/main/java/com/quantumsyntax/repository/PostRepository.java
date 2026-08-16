package com.quantumsyntax.repository;

import com.quantumsyntax.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, String> {
    Optional<Post> findBySlug(String slug);

    Page<Post> findByStatus(Post.Status status, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' AND (:tag IS NULL OR EXISTS (SELECT t FROM p.tags t WHERE t.slug = :tag)) AND (:category IS NULL OR p.category.slug = :category) AND (:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Post> findPublishedWithFilters(@Param("tag") String tag, @Param("category") String category, @Param("search") String search, Pageable pageable);

    List<Post> findByStatusAndIsFeaturedTrueOrderByPublishedAtDesc(Post.Status status);

    List<Post> findTop10ByStatusOrderByViewsCountDesc(Post.Status status);

    long countByStatus(Post.Status status);
}
