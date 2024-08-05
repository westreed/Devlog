package io.blog.devlog.domain.post.service;

import io.blog.devlog.config.TestConfig;
import io.blog.devlog.domain.category.model.Category;
import io.blog.devlog.domain.category.repository.CategoryRepository;
import io.blog.devlog.domain.category.service.CategoryService;
import io.blog.devlog.domain.file.repository.FileRepository;
import io.blog.devlog.domain.file.repository.TempFileRepository;
import io.blog.devlog.domain.file.service.FileService;
import io.blog.devlog.domain.file.service.TempFileService;
import io.blog.devlog.domain.post.dto.RequestPostDto;
import io.blog.devlog.domain.post.model.Post;
import io.blog.devlog.domain.post.repository.PostRepository;
import io.blog.devlog.domain.user.model.Role;
import io.blog.devlog.domain.user.repository.UserRepository;
import io.blog.devlog.domain.user.service.UserService;
import io.blog.devlog.global.jwt.service.JwtService;
import org.apache.coyote.BadRequestException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class PostServiceTest {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private TempFileRepository tempFileRepository;
    private PostService postService;
    private UserService userService;
    private JwtService jwtService;
    private CategoryService categoryService;
    private FileService fileService;
    private TempFileService tempFileService;
    private static final TestConfig testConfig = new TestConfig();

    @BeforeEach
    public void beforeSetUp() {
        jwtService = testConfig.createJwtService();
        userService = new UserService(userRepository, jwtService);
        categoryService = new CategoryService(categoryRepository);
        fileService = new FileService(fileRepository);
        tempFileService = new TempFileService(tempFileRepository);
        postService = new PostService(postRepository, userService, categoryService, fileService, tempFileService);
    }

    public List<Category> createCategory() {
        List<Category> categories = new ArrayList<>();
        for (var i=0; i<3; i++) {
            Category category = Category.builder()
                    .name(String.format("카테고리%d", i))
                    .layer(i)
                    .writePostAuth(Role.USER)
                    .readCategoryAuth(Role.USER)
                    .writeCommentAuth(Role.USER)
                    .build();
            categories.add(category);
        }
        return categories;
    }

    @Test
    @DisplayName("게시글 업로드 테스트")
    public void savePostTest() throws BadRequestException {
        // given
        userService.saveUser(testConfig.adminUser);
        List<Category> categories = categoryService.updateCategories(createCategory());
        RequestPostDto requestPostDto = RequestPostDto.builder()
                .url("url")
                .email(testConfig.email)
                .categoryId(categories.get(0).getId())
                .title("제목")
                .content("내용")
                .files(Collections.emptyList())
                .isPrivate(false)
                .build();
        postService.savePost(requestPostDto);

        // when
        List<Post> posts = postRepository.findAll();

        // then
        Assertions.assertThat(posts.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("게시글 조회 테스트")
    public void findPostIdTest() throws BadRequestException {
        // given
        userService.saveUser(testConfig.adminUser);
        List<Category> categories = categoryService.updateCategories(createCategory());
        RequestPostDto requestPostDto = RequestPostDto.builder()
                .url("url")
                .email(testConfig.email)
                .categoryId(categories.get(0).getId())
                .title("제목")
                .content("내용")
                .files(Collections.emptyList())
                .isPrivate(false)
                .build();
        Post uploadedPost = postService.savePost(requestPostDto);

        // when
        Post post = postService.getPostById(uploadedPost.getId());

        // then
        Assertions.assertThat(post).isNotNull();
        Assertions.assertThat(post.getUrl()).isEqualTo("url");
    }

    @Test
    @DisplayName("게시글 Url 조회 테스트")
    public void findPostUrlTest() throws BadRequestException {
        // given
        userService.saveUser(testConfig.adminUser);
        List<Category> categories = categoryService.updateCategories(createCategory());
        RequestPostDto requestPostDto = RequestPostDto.builder()
                .url("url")
                .email(testConfig.email)
                .categoryId(categories.get(0).getId())
                .title("제목")
                .content("내용")
                .files(Collections.emptyList())
                .isPrivate(false)
                .build();
        postService.savePost(requestPostDto);

        // when
        Post post = postService.getPostByUrl("url");

        // then
        Assertions.assertThat(post).isNotNull();
        Assertions.assertThat(post.getUrl()).isEqualTo("url");
    }

    @Test
    @DisplayName("게시글 조회 테스트 (Pageable)")
    public void findPostAllPageableTest() throws BadRequestException {
        // given
        userService.saveUser(testConfig.adminUser);
        List<Category> categories = categoryService.updateCategories(createCategory());
        RequestPostDto requestPostDto = RequestPostDto.builder()
                .email(testConfig.email)
                .categoryId(categories.get(0).getId())
                .title("제목")
                .content("내용")
                .files(Collections.emptyList())
                .isPrivate(false)
                .build();
        postService.savePost(requestPostDto.setUrl("url1"));
        postService.savePost(requestPostDto.setUrl("url2"));
        postService.savePost(requestPostDto.setUrl("url3"));

        // when
        int page = 0;
        int size = 2;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Post> posts = postService.getPosts(pageable);

        // then
        Assertions.assertThat(posts).isNotNull();
        Assertions.assertThat(posts.getTotalElements()).isEqualTo(3);
        Assertions.assertThat(posts.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("게시글 비공개글 제외 조회 테스트 (Pageable)")
    public void findPrivateExceptionPostAllPageableTest() throws BadRequestException {
        // given
        userService.saveUser(testConfig.adminUser);
        List<Category> categories = categoryService.updateCategories(createCategory());
        RequestPostDto requestPostDto = RequestPostDto.builder()
                .email(testConfig.email)
                .categoryId(categories.get(0).getId())
                .title("제목")
                .content("내용")
                .files(Collections.emptyList())
                .isPrivate(true)
                .build();
        postService.savePost(requestPostDto.setUrl("url1"));
        postService.savePost(requestPostDto.setUrl("url2"));
        postService.savePost(requestPostDto.setUrl("url3"));

        // when
        int page = 0;
        int size = 2;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Post> posts = postService.getPosts(pageable);

        // then
        Assertions.assertThat(posts).isNotNull();
        Assertions.assertThat(posts.getTotalElements()).isEqualTo(0);
        Assertions.assertThat(posts.getTotalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("게시글 비공개글 조회 테스트 (Pageable)")
    public void findPrivatePostAllPageableTest() throws BadRequestException {
        // given
        userService.saveUser(testConfig.adminUser);
        List<Category> categories = categoryService.updateCategories(createCategory());
        RequestPostDto requestPostDto = RequestPostDto.builder()
                .email(testConfig.email)
                .categoryId(categories.get(0).getId())
                .title("제목")
                .content("내용")
                .files(Collections.emptyList())
                .isPrivate(true)
                .build();
        postService.savePost(requestPostDto.setUrl("url1"));
        postService.savePost(requestPostDto.setUrl("url2"));
        postService.savePost(requestPostDto.setUrl("url3"));

        // when
        int page = 0;
        int size = 2;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        UserDetails userDetails = User.builder()
                .username(testConfig.email)
                .password("")
                .authorities(new ArrayList<>(List.of(new SimpleGrantedAuthority("ROLE_" + Role.USER))))
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Page<Post> posts = postService.getPosts(pageable);

        // then
        Assertions.assertThat(posts).isNotNull();
        Assertions.assertThat(posts.getTotalElements()).isEqualTo(3);
        Assertions.assertThat(posts.getTotalPages()).isEqualTo(2);
    }
}