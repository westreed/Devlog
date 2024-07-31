package io.blog.devlog.domain.post.service;

import io.blog.devlog.domain.category.model.Category;
import io.blog.devlog.domain.category.service.CategoryService;
import io.blog.devlog.domain.file.model.File;
import io.blog.devlog.domain.file.service.FileService;
import io.blog.devlog.domain.file.service.TempFileService;
import io.blog.devlog.domain.post.dto.RequestPostDto;
import io.blog.devlog.domain.post.model.Post;
import io.blog.devlog.domain.post.repository.PostRepository;
import io.blog.devlog.domain.user.model.User;
import io.blog.devlog.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.blog.devlog.global.utils.SecurityUtils.getUserEmail;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final FileService fileService;
    private final TempFileService tempFileService;

    public Post savePost(RequestPostDto requestPostDto) throws BadRequestException {
        String email = requestPostDto.getEmail();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found : " + email));
        Category category = categoryService.getCategoryById(requestPostDto.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Category not found : " + requestPostDto.getCategoryId()));

        Post post = postRepository.save(requestPostDto.toEntity(user, category));

        List<File> files = new ArrayList<>();
        for (int i = 0; i < requestPostDto.getFiles().size(); i++) {
            try {
                tempFileService.deleteTempFile(requestPostDto.getFiles().get(i).getTempId());
                File file = fileService.addFile(requestPostDto.getFiles().get(i).toEntity(post));
                files.add(file);
            }
            catch (Exception e) {
                log.error("Temp file not found : " + requestPostDto.getFiles().get(i).getTempId());
            }
        }

        return post;
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElse(null);
    }

    public Post getPostByUrl(String url) throws BadRequestException {
        Post post = postRepository.findByUrl(url).orElseThrow(() -> new BadRequestException("Post not found : " + url));
        if (post.isPrivate()) {
            String email = getUserEmail();
            if (email == null) {
                throw new BadRequestException("Post not found : " + url);
            }
            User user = userService.getUserByEmail(email).orElseThrow(() -> new BadRequestException("User not found : " + email));
            if (!Objects.equals(post.getUser().getId(), user.getId()) && !Objects.equals(user.getRole().getNameKey(), "ROLE_ADMIN")) {
                throw new BadRequestException("Post not found : " + url);
            }
        }
        return post;
    }

    public Page<Post> getPosts(Pageable pageable) throws BadRequestException {
        String email = getUserEmail();
        log.info("getPosts (email: " + email + ")");
        if (email == null) {
            return postRepository.findAllPublicPosts(pageable);
        }
        User user = userService.getUserByEmail(email).orElseThrow(() -> new BadRequestException("User not found : " + email));
        return postRepository.findAllUserPosts(user.getId(), Objects.equals(user.getRole().getNameKey(), "ROLE_ADMIN"), pageable);
    }

    public Page<Post> getPostsByCategory(String categoryName, Pageable pageable) throws BadRequestException {
        String email = getUserEmail();
        if (email == null) {
            return postRepository.findAllByCategory(categoryName, 0L, false, pageable);
        }
        User user = userService.getUserByEmail(email).orElseThrow(() -> new BadRequestException("User not found : " + email));
        return postRepository.findAllByCategory(categoryName, user.getId(), Objects.equals(user.getRole().getNameKey(), "ROLE_ADMIN"), pageable);
    }

    public Page<Post> getPostsByCategoryId(Long categoryId, Pageable pageable) throws BadRequestException {
        String email = getUserEmail();
        if (email == null) {
            return postRepository.findAllByCategoryId(categoryId, 0L, false, pageable);
        }
        User user = userService.getUserByEmail(email).orElseThrow(() -> new BadRequestException("User not found : " + email));
        return postRepository.findAllByCategoryId(categoryId, user.getId(), Objects.equals(user.getRole().getNameKey(), "ROLE_ADMIN"), pageable);
    }
}
