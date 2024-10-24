package io.blog.devlog.domain.post.repository;

import io.blog.devlog.domain.post.model.Post;
import io.blog.devlog.domain.user.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    public Optional<Post> findByUrl(String url);
    @EntityGraph(attributePaths = {"user", "category"}, type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT p FROM Post p " +
            "WHERE p.url = :url and p.category.readCategoryAuth <= :role and (p.isPrivate = false OR :isAdmin = true OR p.user.id = :userId)")
    public Optional<Post> findPostByUrl(
            @Param("url") String url,
            @Param("userId") Long userId,
            @Param("isAdmin") boolean isAdmin,
            @Param("role") Role role);

    @EntityGraph(attributePaths = {"user", "category"}, type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT p FROM Post p " +
            "WHERE p.category.readCategoryAuth <= :role and (p.isPrivate = false OR :isAdmin = true OR p.user.id = :userId)")
    Page<Post> findAllPageUserPosts(
            Pageable pageable,
            @Param("userId") Long userId,
            @Param("isAdmin") boolean isAdmin,
            @Param("role") Role role);

    @EntityGraph(attributePaths = {"user", "category"}, type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT p FROM Post p " +
            "WHERE p.isPrivate = false and p.category.readCategoryAuth <= :role")
    Page<Post> findAllPagePublicPosts(Pageable pageable, @Param("role") Role role);

    @EntityGraph(attributePaths = {"user", "category"}, type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT p FROM Post p " +
            "WHERE p.category.readCategoryAuth <= :role and p.category.name = :categoryName AND " +
            "(p.isPrivate = false OR :isAdmin = true OR p.user.id = :userId)")
    Page<Post> findAllPageByCategory(
            Pageable pageable,
            @Param("categoryName") String categoryName,
            @Param("userId") Long userId,
            @Param("isAdmin") boolean isAdmin,
            @Param("role") Role role);

    @EntityGraph(attributePaths = {"user", "category"}, type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT p FROM Post p " +
            "WHERE p.category.readCategoryAuth <= :role and p.category.id = :categoryId AND " +
            "(p.isPrivate = false OR :isAdmin = true OR p.user.id = :userId)")
    Page<Post> findAllPageByCategoryId(
            Pageable pageable,
            @Param("categoryId") Long categoryId,
            @Param("userId") Long userId,
            @Param("isAdmin") boolean isAdmin,
            @Param("role") Role role);

    @EntityGraph(attributePaths = {"user", "category"}, type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT p FROM Post p WHERE p.category.id = :categoryId")
    List<Post> findAllByCategoryId(@Param("categoryId") Long categoryId);

    @Modifying
    @Query("UPDATE Post p SET p.views = p.views + 1 WHERE p.id = :postId")
    void increasePostView(@Param("postId") Long postId);
}
