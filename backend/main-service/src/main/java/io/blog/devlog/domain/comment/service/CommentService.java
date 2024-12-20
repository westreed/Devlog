package io.blog.devlog.domain.comment.service;

import io.blog.devlog.domain.comment.dto.RequestCommentDto;
import io.blog.devlog.domain.comment.dto.RequestEditCommentDto;
import io.blog.devlog.domain.comment.dto.ResponseCommentDto;
import io.blog.devlog.domain.comment.model.Comment;
import io.blog.devlog.domain.comment.repository.CommentRepository;
import io.blog.devlog.domain.file.service.FileService;
import io.blog.devlog.domain.post.model.Post;
import io.blog.devlog.domain.post.model.PostDetail;
import io.blog.devlog.domain.user.model.User;
import io.blog.devlog.global.exception.NoPermissionException;
import io.blog.devlog.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

import static io.blog.devlog.global.utils.SecurityUtils.getUserEmail;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final FileService fileService;

    public ResponseCommentDto saveComment(User user, RequestCommentDto requestCommentDto, Post post) {
        Comment comment = commentRepository.save(requestCommentDto.toEntity(user, post));
        fileService.uploadFileAndDeleteTempFile(comment, requestCommentDto.getFiles());
        fileService.deleteTempFiles(); // 임시파일 제거
        return ResponseCommentDto.of(user.getEmail(), comment);
    }

    public Comment updateComment(RequestEditCommentDto requestEditCommentDto, Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comment not found : " + commentId));
        String email = getUserEmail();
        if (!comment.getUser().getEmail().equals(email)) {
            throw new NoPermissionException("댓글을 수정할 권한이 없습니다.");
        }
        comment = commentRepository.save(comment.toEdit(requestEditCommentDto));
        fileService.uploadFileAndDeleteTempFile(comment, requestEditCommentDto.getFiles());
        fileService.deleteTempFiles(); // 임시파일 제거
        fileService.deleteUnusedFilesByComment(comment, requestEditCommentDto.getFiles());
        return comment;
    }

    public List<ResponseCommentDto> getCommentsFromPost(User user, PostDetail postDetail) {
        return this.getCommentsFromPost(user, postDetail.getPost().getId());
    }

    public List<ResponseCommentDto> getCommentsFromPost(User user, Long postId) {
        Long userId = user.getId() == null ? 0L : user.getId();
        boolean isAdmin = user.isAdmin();
        List<Comment> comments = commentRepository.findAllByPostId(postId, userId, isAdmin);
        return comments.stream()
                .map(comment -> ResponseCommentDto.of(user.getEmail(), comment))
                .toList();
    }

    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comment not found : " + commentId));
        String email = getUserEmail();
        if (!comment.getUser().getEmail().equals(email)) {
            throw new NoPermissionException("댓글을 삭제할 권한이 없습니다.");
        }
        comment.setDeleted(true);
        fileService.deleteFileFromComment(comment);
        commentRepository.save(comment);
    }

    public void deleteCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findAllByPostId(postId, 0L, true);
        comments.forEach(comment -> {
            fileService.deleteFileFromComment(comment);
            commentRepository.delete(comment);
        });
    }
}