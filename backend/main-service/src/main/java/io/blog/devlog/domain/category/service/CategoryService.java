package io.blog.devlog.domain.category.service;

import io.blog.devlog.domain.category.dto.CategoryDto;
import io.blog.devlog.domain.category.model.Category;
import io.blog.devlog.domain.category.repository.CategoryRepository;
import io.blog.devlog.domain.post.model.Post;
import io.blog.devlog.domain.post.service.PostService;
import io.blog.devlog.domain.user.model.Role;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.blog.devlog.global.utils.SecurityUtils.getPrincipalRole;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final PostService postService;

    @Transactional(readOnly = true)
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    public List<Category> sortCategories(List<Category> categories) {
        return categories.stream().sorted(Comparator.comparingLong(Category::getLayer)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Category> getCategories() {
        Role role = getPrincipalRole();
        if (role == null) {
            role = Role.GUEST;
        }
        Role finalRole = role;
        return categoryRepository.findAll()
                .stream()
                .filter(category -> this.hasReadCategoryAuth(category, finalRole))
                .sorted(Comparator.comparingLong(Category::getLayer))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Category> getCategoriesReadWrite() {
        Role role = getPrincipalRole();
        if (role == null) {
            role = Role.GUEST;
        }
        Role finalRole = role;
        return categoryRepository.findAll()
                .stream()
                .filter(category -> this.hasReadWriteCategoryAuth(category, finalRole))
                .sorted(Comparator.comparingLong(Category::getLayer))
                .toList();
    }

    public List<Category> updateCategories(List<Category> categories) {
        List<Category> prevCategories = categoryRepository.findAll();
        // categories 없는 카테고리는 삭제
        List<Category> deleteCategories = prevCategories.stream()
                .filter(category -> categories.stream().noneMatch(c -> c.getId().equals(category.getId())))
                .toList();
        // TODO: 최적화가 필요한 부분 (카테고리를 삭제하면, 해당 카테고리 안의 모든 게시글을 조회하고, 해당 게시글과 연결된 파일을 조회하게 됨)
        for (Category deleteCategory : deleteCategories) {
            List<Post> posts = postService.getAllPostsByCategoryId(deleteCategory.getId());
            for (Post post : posts) {
                postService.deletePost(post);
            }
        }
        categoryRepository.deleteAll(deleteCategories);

        return categoryRepository.saveAll(categories);
    }

    public void cleanUpCategories() {
        categoryRepository.truncate();
    }

    public boolean hasReadCategoryAuth(Category category) {
        Role role = getPrincipalRole();
        if (role == null) role = Role.GUEST;
        return category.getReadCategoryAuth().getKey() <= role.getKey();
    }

    public boolean hasReadCategoryAuth(Category category, Role role) {
        if (role == null) role = Role.GUEST;
        return category.getReadCategoryAuth().getKey() <= role.getKey();
    }

    public boolean hasReadWriteCategoryAuth(Category category, Role role) {
        if (role == null) role = Role.GUEST;
        return category.getReadCategoryAuth().getKey() <= role.getKey() && category.getWritePostAuth().getKey() <= role.getKey();
    }

    public boolean hasCommentCategoryAuth(Category category, Role role) {
        if (role == null) role = Role.GUEST;
        return category.getWriteCommentAuth().getKey() <= role.getKey();
    }
}
