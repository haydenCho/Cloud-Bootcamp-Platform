package com.solcho.bootcamp.community.repository;

import com.solcho.bootcamp.community.entity.CommunityPost;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    List<CommunityPost> findAllByOrderByIdDesc();

    List<CommunityPost> findByUserIdOrderByIdDesc(Long userId);
}
