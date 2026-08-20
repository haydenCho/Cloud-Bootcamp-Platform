package com.solcho.bootcamp.community.repository;

import com.solcho.bootcamp.community.entity.CommunityComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

    List<CommunityComment> findByPostIdOrderByIdAsc(Long postId);

    List<CommunityComment> findByUserIdOrderByIdDesc(Long userId);

    long countByPostId(Long postId);

    /** 게시글별 댓글 수 집계 (목록 N+1 방지). Object[] = [postId, count] */
    @Query("select c.postId, count(c) from CommunityComment c group by c.postId")
    List<Object[]> countGroupedByPost();

    /** 게시글 삭제 시 딸린 댓글/답글 일괄 삭제. */
    void deleteByPostId(Long postId);

    /** 최상위 댓글 삭제 시 그 답글들 일괄 삭제. */
    void deleteByParentCommentId(Long parentCommentId);
}
