package com.solcho.bootcamp.like.service;

import com.solcho.bootcamp.like.dto.ServiceLikeResponse;
import com.solcho.bootcamp.like.entity.ServiceLike;
import com.solcho.bootcamp.like.repository.ServiceLikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceLikeService {

    private final ServiceLikeRepository repository;

    public ServiceLikeService(ServiceLikeRepository repository) {
        this.repository = repository;
    }

    /** 총 좋아요 수 + 내가 눌렀는지. userId 가 null(비로그인)이면 likedByMe=false. */
    @Transactional(readOnly = true)
    public ServiceLikeResponse getStatus(Long userId) {
        long total = repository.count();
        boolean likedByMe = userId != null && repository.existsById(userId);
        return new ServiceLikeResponse(total, likedByMe);
    }

    /** 토글: 이미 눌렀으면 취소(삭제), 아니면 좋아요(생성). 갱신된 상태를 반환. */
    @Transactional
    public ServiceLikeResponse toggle(Long userId) {
        if (repository.existsById(userId)) {
            repository.deleteById(userId);
        } else {
            repository.save(new ServiceLike(userId));
        }
        return new ServiceLikeResponse(repository.count(), repository.existsById(userId));
    }
}
