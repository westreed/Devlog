package io.blog.devlog.domain.comment.service;

import io.blog.devlog.config.TestConfig;
import io.blog.devlog.domain.category.model.Category;
import io.blog.devlog.domain.category.repository.CategoryRepository;
import io.blog.devlog.domain.category.service.CategoryService;
import io.blog.devlog.domain.comment.dto.ResponseCommentDto;
import io.blog.devlog.domain.comment.model.Comment;
import io.blog.devlog.domain.comment.repository.CommentRepository;
import io.blog.devlog.domain.file.handler.FileHandler;
import io.blog.devlog.domain.file.repository.FileRepository;
import io.blog.devlog.domain.file.repository.TempFileRepository;
import io.blog.devlog.domain.file.service.FileService;
import io.blog.devlog.domain.file.service.TempFileService;
import io.blog.devlog.domain.post.model.Post;
import io.blog.devlog.domain.post.model.PostDetail;
import io.blog.devlog.domain.post.repository.PostRepository;
import io.blog.devlog.domain.post.service.PostService;
import io.blog.devlog.domain.user.model.Role;
import io.blog.devlog.domain.user.model.User;
import io.blog.devlog.domain.user.repository.UserRepository;
import io.blog.devlog.domain.user.service.UserService;
import io.blog.devlog.global.jwt.service.JwtService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

@DataJpaTest // Component Scan을 하지 않아 컨테이너에 @Component 빈들이 등록되지 않는다.
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class CommentServiceTest {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TempFileRepository tempFileRepository;
    @Autowired
    private FileRepository fileRepository;
    private CommentService commentService;
    private UserService userService;
    private PostService postService;
    private CategoryService categoryService;
    private JwtService jwtService;
    private TempFileService tempFileService;
    private FileService fileService;
    private FileHandler fileHandler;
    private static final TestConfig testConfig = new TestConfig();

    private User guestUser;
    private Post post;

    @BeforeEach
    public void beforeSetUp() {
        jwtService = testConfig.createJwtService();
        userService = new UserService(userRepository, jwtService);
        tempFileService = new TempFileService(tempFileRepository);
        fileHandler = new FileHandler(tempFileService);
        fileService = new FileService(fileRepository, tempFileService, fileHandler);
        postService = new PostService(postRepository, userService, fileService);
        categoryService = new CategoryService(categoryRepository, postService);
        commentService = new CommentService(commentRepository, userService, fileService);
    }

    public List<Category> createCategory() {
        List<Category> categories = new ArrayList<>();
        for (var i=0; i<3; i++) {
            Category category = Category.builder()
                    .name(String.format("카테고리%d", i))
                    .layer(i)
                    .writePostAuth(Role.USER)
                    .readCategoryAuth(Role.GUEST)
                    .writeCommentAuth(Role.USER)
                    .build();
            categories.add(category);
        }
        return categories;
    }

    public void setupUserAndCategoryAndPost() throws BadRequestException {
        userService.saveUser(testConfig.adminUser);
        User guestUser = userService.saveUser(testConfig.guestUser);
        List<Category> categories = categoryService.updateCategories(createCategory());
        Post post = Post.builder()
                        .url("url")
                        .title("제목")
                        .content("내용")
                        .category(categories.get(0))
                        .user(guestUser)
                        .isPrivate(false)
                        .build();
        this.post = postRepository.save(post);
        this.guestUser = guestUser;
    }

    @Test
    @DisplayName("댓글 작성 테스트")
    public void saveCommentTest() throws BadRequestException {
        // given
        this.setupUserAndCategoryAndPost();
        // 사용자 정보를 SecurityContextHolder에 등록함.
        testConfig.updateAuthentication(testConfig.adminUser);
        Comment comment = Comment.builder()
                        .content("댓글 내용1")
                        .post(this.post)
                        .user(this.guestUser)
                        .isPrivate(false)
                        .build();
        commentRepository.save(comment);
        System.out.println("-------------------------------------------------------------------------------------------------");

        // when

        // then
        PostDetail postDetail = postService.getPostByUrl("url");

        List<ResponseCommentDto> comments = commentService.getCommentsFromPost(testConfig.adminUser, postDetail);
        for (ResponseCommentDto c : comments) {
            System.out.println(c.getContent());
        }
    }

    @Test
    @DisplayName("게시글 댓글 복합 조회 테스트")
    public void getPostCommentTest() throws BadRequestException {
        // given
        this.setupUserAndCategoryAndPost();
        // 사용자 정보를 SecurityContextHolder에 등록함.
        testConfig.updateAuthentication(testConfig.guestUser);
        Comment comment = Comment.builder()
                .content("댓글 내용1")
                .post(this.post)
                .user(this.guestUser)
                .isPrivate(false)
                .build();
        commentRepository.save(comment);
        System.out.println("-------------------------------------------------------------------------------------------------");

        // when

        // then
        // 이전 query 결과를 재사용하진 않았음.
        PostDetail postDetail = postService.getPostByUrl("url");
        Post post = postDetail.getPost();
        List<ResponseCommentDto> comments = commentService.getCommentsFromPost(testConfig.guestUser, postDetail);
        System.out.println("게시글 제목 : " + post.getTitle());
        for (ResponseCommentDto c : comments) {
            System.out.println(c.getContent());
        }
    }
}
