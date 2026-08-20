package com.solcho.bootcamp.community.service;

import com.solcho.bootcamp.common.exception.ApiException;
import com.solcho.bootcamp.community.dto.PostCreateRequest;
import com.solcho.bootcamp.community.dto.PostDetailResponse;
import com.solcho.bootcamp.community.dto.PostSummaryResponse;
import com.solcho.bootcamp.community.entity.CommunityPost;
import com.solcho.bootcamp.community.repository.CommunityCommentRepository;
import com.solcho.bootcamp.community.repository.CommunityPostRepository;
import com.solcho.bootcamp.user.entity.Role;
import com.solcho.bootcamp.user.entity.User;
import com.solcho.bootcamp.user.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 커뮤니티 게시글 서비스. 작성자 닉네임/프로필은 저장 시 복사하지 않고 user_id 로 조인해서 채운다.
 */
@Service
public class PostService {

    private final CommunityPostRepository postRepository;
    private final CommunityCommentRepository commentRepository;
    private final UserRepository userRepository;

    public PostService(CommunityPostRepository postRepository,
                       CommunityCommentRepository commentRepository,
                       UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<PostSummaryResponse> list() {
        List<CommunityPost> posts = postRepository.findAllByOrderByIdDesc();
        Map<Long, User> users = loadAuthors(posts.stream().map(CommunityPost::getUserId).toList());
        Map<Long, Long> commentCounts = commentCountMap();
        return posts.stream()
                .map(p -> toSummary(p, users, commentCounts))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PostSummaryResponse> getMyPosts(Long userId) {
        List<CommunityPost> posts = postRepository.findByUserIdOrderByIdDesc(userId);
        Map<Long, User> users = loadAuthors(posts.stream().map(CommunityPost::getUserId).toList());
        Map<Long, Long> commentCounts = commentCountMap();
        return posts.stream().map(p -> toSummary(p, users, commentCounts)).toList();
    }

    /** 상세 조회 시 조회수 1 증가. */
    @Transactional
    public PostDetailResponse getDetail(Long id) {
        CommunityPost post = postRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("존재하지 않는 게시글입니다."));
        post.increaseViewCount();
        return toDetail(post);
    }

    @Transactional
    public PostDetailResponse create(Long userId, PostCreateRequest req) {
        CommunityPost post = postRepository.save(CommunityPost.builder()
                .userId(userId)
                .title(req.title())
                .body(req.body())
                .build());
        return toDetail(post);
    }

    /** 수정은 작성자 본인만. */
    @Transactional
    public PostDetailResponse update(Long userId, Long id, PostCreateRequest req) {
        CommunityPost post = postRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("존재하지 않는 게시글입니다."));
        if (!post.getUserId().equals(userId)) {
            throw ApiException.forbidden("작성자만 수정할 수 있습니다.");
        }
        post.update(req.title(), req.body());
        return toDetail(post);
    }

    /** 삭제는 작성자 본인 또는 ADMIN. 딸린 댓글도 함께 삭제. */
    @Transactional
    public void delete(Long userId, Role role, Long id) {
        CommunityPost post = postRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("존재하지 않는 게시글입니다."));
        if (!post.getUserId().equals(userId) && role != Role.ADMIN) {
            throw ApiException.forbidden("삭제 권한이 없습니다.");
        }
        commentRepository.deleteByPostId(id);
        postRepository.delete(post);
    }

    // ---- 내부 헬퍼 ----

    private Map<Long, User> loadAuthors(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private Map<Long, Long> commentCountMap() {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : commentRepository.countGroupedByPost()) {
            map.put((Long) row[0], (Long) row[1]);
        }
        return map;
    }

    private PostSummaryResponse toSummary(CommunityPost p, Map<Long, User> users, Map<Long, Long> counts) {
        User author = users.get(p.getUserId());
        return new PostSummaryResponse(
                p.getId(),
                p.getTitle(),
                p.getUserId(),
                author != null ? author.getNickname() : "(알 수 없음)",
                p.getViewCount(),
                counts.getOrDefault(p.getId(), 0L),
                p.getCreatedAt());
    }

    private PostDetailResponse toDetail(CommunityPost p) {
        User author = userRepository.findById(p.getUserId()).orElse(null);
        return new PostDetailResponse(
                p.getId(),
                p.getTitle(),
                p.getBody(),
                p.getUserId(),
                author != null ? author.getNickname() : "(알 수 없음)",
                author != null ? author.getProfileImageUrl() : null,
                p.getViewCount(),
                p.getCreatedAt(),
                p.getUpdatedAt());
    }
}
