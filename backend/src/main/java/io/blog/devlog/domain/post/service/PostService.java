package io.blog.devlog.domain.post.service;

import io.blog.devlog.domain.category.model.Category;
import io.blog.devlog.domain.category.service.CategoryService;
import io.blog.devlog.domain.file.model.File;
import io.blog.devlog.domain.file.service.FileService;
import io.blog.devlog.domain.file.service.TempFileService;
import io.blog.devlog.domain.post.dto.RequestPostDto;
import io.blog.devlog.domain.post.model.Post;
import io.blog.devlog.domain.post.repository.PostRepository;
import io.blog.devlog.domain.user.model.Role;
import io.blog.devlog.domain.user.model.User;
import io.blog.devlog.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.blog.devlog.global.utils.SecurityUtils.getUserEmail;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final UserService userService;

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElse(null);
    }

    public Post getPostByUrl(String url) throws BadRequestException {
        String email = getUserEmail();
        Long userId = null;
        boolean isAdmin = false;
        Role role = Role.GUEST;
        if (email == null) {
            userId = 0L;
        } else {
            User user = userService.getUserByEmail(email).orElseThrow(() -> new BadRequestException("User not found : " + email));
            userId = user.getId();
            isAdmin = userService.isAdmin(user);
            role = user.getRole();
        }
        return postRepository.findByUrl(url, userId, isAdmin, role).orElseThrow(() -> new BadRequestException("Post not found : " + url));
    }

    public Page<Post> getPosts(Pageable pageable) throws BadRequestException {
        String email = getUserEmail();
        log.info("getPosts (email: " + email + ")");
        if (email == null) {
            return postRepository.findAllPublicPosts(pageable, Role.GUEST);
        }
        User user = userService.getUserByEmail(email).orElseThrow(() -> new BadRequestException("User not found : " + email));
        return postRepository.findAllUserPosts(pageable, user.getId(), userService.isAdmin(user), user.getRole());
    }

    public Page<Post> getPostsByCategory(String categoryName, Pageable pageable) throws BadRequestException {
        String email = getUserEmail();
        if (email == null) {
            return postRepository.findAllByCategory(pageable, categoryName, 0L, false, Role.GUEST);
        }
        User user = userService.getUserByEmail(email).orElseThrow(() -> new BadRequestException("User not found : " + email));
        return postRepository.findAllByCategory(pageable, categoryName, user.getId(), userService.isAdmin(user), user.getRole());
    }

    public Page<Post> getPostsByCategoryId(Long categoryId, Pageable pageable) throws BadRequestException {
        String email = getUserEmail();
        if (email == null) {
            return postRepository.findAllByCategoryId(pageable, categoryId, 0L, false, Role.GUEST);
        }
        User user = userService.getUserByEmail(email).orElseThrow(() -> new BadRequestException("User not found : " + email));
        return postRepository.findAllByCategoryId(pageable, categoryId, user.getId(), userService.isAdmin(user), user.getRole());
    }
}
