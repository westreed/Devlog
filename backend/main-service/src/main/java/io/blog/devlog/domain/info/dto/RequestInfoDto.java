package io.blog.devlog.domain.info.dto;

import io.blog.devlog.domain.info.model.Info;
import io.blog.devlog.domain.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestInfoDto {
    private String about; // 블로그 소개글
    private String profileUrl; // 블로그 프로필 사진

    public Info toEntity(User user){
        return Info.builder()
                .user(user)
                .about(this.about)
                .profileUrl(this.profileUrl)
                .build();
    }
}
