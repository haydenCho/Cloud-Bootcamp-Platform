package com.solcho.bootcamp.community.service;

import com.solcho.bootcamp.common.exception.ApiException;
import com.solcho.bootcamp.community.dto.CommentCreateRequest;
import com.solcho.bootcamp.community.dto.CommentResponse;
import com.solcho.bootcamp.community.dto.MyCommentResponse;
import com.solcho.bootcamp.community.entity.CommunityComment;
import com.solcho.bootcamp.community.entity.CommunityPost;
import com.solcho.bootcamp.community.repository.CommunityCommentRepository;
import com.solcho.bootcamp.community.repository.CommunityPostRepository;
import com.solcho.bootcamp.user.entity.Role;
import com.solcho.bootcamp.user.entity.User;
import com.solcho.bootcamp.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 커뮤니티 댓글/답글 서비스. 답글은 1단계 깊이만 허용한다(답글에 답글 불가).
 */
@Service
public class CommentService {

    private final CommunityCommentRepository commentRepository;
    private final CommunityPostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommunityCommentRepository commentRepository,
                          CommunityPostRepository postRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /** 게시글의 댓글을 트리(최상위 + 답글)로 반환. */
    @Transactional(readOnly = true)
    public List<CommentResponse> getTree(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw ApiException.notFound("존재하지 않는 게시글입니다.");
        }
        List<CommunityComment> comments = commentRepository.findByPostIdOrderByIdAsc(postId);
        Map<Long, User> authors = loadAuthors(comments.stream().map(CommunityComment::getUserId).toList());

        // 최상위 댓글 → 그 아래 답글 매핑
        Map<Long, List<CommentResponse>> repliesByParent = comments.stream()
                .filter(CommunityComment::isReply)
                .collect(Collectors.groupingBy(
                        CommunityComment::getParentCommentId,
                        Collectors.mapping(c -> toResponse(c, authors, List.of()), Collectors.toList())));

        List<CommentResponse> roots = new ArrayList<>();
        for (CommunityComment c : comments) {
            if (!c.isReply()) {
                roots.add(toResponse(c, authors, repliesByParent.getOrDefault(c.getId(), List.of())));
            }
        }
        return roots;
    }

    @Transactional
    public CommentResponse create(Long userId, Long postId, CommentCreateRequest req) {
        if (!postRepository.existsById(postId)) {
            throw ApiException.notFound("존재하지 않는 게시글입니다.");
        }
        if (req.parentCommentId() != null) {
            CommunityComment parent = commentRepository.findById(req.parentCommentId())
                    .orElseThrow(() -> ApiException.notFound("존재하지 않는 댓글입니다."));
            if (!parent.getPostId().equals(postId)) {
                throw ApiException.badRequest("댓글과 게시글이 일치하지 않습니다.");
            }
            if (parent.isReply()) {
                throw ApiException.badRequest("답글에는 답글을 달 수 없습니다.");
            }
        }
        CommunityComment saved = commentRepository.save(CommunityComment.builder()
                .postId(postId)
                .userId(userId)
                .parentCommentId(req.parentCommentId())
                .body(req.body())
                .build());
        Map<Long, User> authors = loadAuthors(List.of(userId));
        return toResponse(saved, authors, List.of());
    }

    /** 수정은 작성자 본인 또는 ADMIN. */
    @Transactional
    public CommentResponse update(Long userId, Role role, Long commentId, String body) {
        CommunityComment comment = getOwnedOrAdmin(userId, role, commentId);
        comment.update(body);
        Map<Long, User> authors = loadAuthors(List.of(comment.getUserId()));
        return toResponse(comment, authors, List.of());
    }

    /** 삭제는 작성자 본인 또는 ADMIN. 최상위 댓글이면 답글도 함께 삭제. */
    @Transactional
    public void delete(Long userId, Role role, Long commentId) {
        CommunityComment comment = getOwnedOrAdmin(userId, role, commentId);
        if (!comment.isReply()) {
            commentRepository.deleteByParentCommentId(comment.getId());
        }
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public List<MyCommentResponse> getMyComments(Long userId) {
        List<CommunityComment> comments = commentRepository.findByUserIdOrderByIdDesc(userId);
        Map<Long, String> titles = postRepository
                .findAllById(comments.stream().map(CommunityComment::getPostId).toList()).stream()
                .collect(Collectors.toMap(CommunityPost::getId, CommunityPost::getTitle));
        return comments.stream()
                .map(c -> new MyCommentResponse(
                        c.getId(), c.getBody(), c.getPostId(),
                        titles.getOrDefault(c.getPostId(), "(삭제된 게시글)"),
                        c.getParentCommentId(), c.getCreatedAt()))
                .toList();
    }

    // ---- 내부 헬퍼 ----

    private CommunityComment getOwnedOrAdmin(Long userId, Role role, Long commentId) {
        CommunityComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> ApiException.notFound("존재하지 않는 댓글입니다."));
        if (!comment.getUserId().equals(userId) && role != Role.ADMIN) {
            throw ApiException.forbidden("권한이 없습니다.");
        }
        return comment;
    }

    private Map<Long, User> loadAuthors(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private CommentResponse toResponse(CommunityComment c, Map<Long, User> authors, List<CommentResponse> replies) {
        User author = authors.get(c.getUserId());
        return new CommentResponse(
                c.getId(),
                c.getBody(),
                c.getUserId(),
                author != null ? author.getNickname() : "(알 수 없음)",
                author != null ? author.getProfileImageUrl() : null,
                c.getParentCommentId(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                replies);
    }
}
