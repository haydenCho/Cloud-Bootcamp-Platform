package com.solcho.bootcamp.like.repository;

import com.solcho.bootcamp.like.entity.ServiceLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceLikeRepository extends JpaRepository<ServiceLike, Long> {
    // existsById(userId), count(), deleteById(userId), save(...) 는 JpaRepository 제공
}
