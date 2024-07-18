package io.blog.devlog.domain.post.dto;

import io.blog.devlog.domain.category.model.Category;
import io.blog.devlog.domain.file.dto.FileDto;
import io.blog.devlog.domain.file.model.File;
import io.blog.devlog.domain.post.model.Post;
import io.blog.devlog.domain.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestPostDto {
    private String url;
    private String title;
    private String content;
    private String previewUrl;
    private String email;
    private Long categoryId;
    private List<FileDto> files;
    private boolean isPrivate;

    public Post toEntity(User user, Category category) {
        return Post.builder()
                .url(this.url)
                .title(this.title)
                .content(this.content)
                .previewUrl(this.previewUrl)
                .category(category)
                .user(user)
                .isPrivate(this.isPrivate)
                .build();
    }
}
