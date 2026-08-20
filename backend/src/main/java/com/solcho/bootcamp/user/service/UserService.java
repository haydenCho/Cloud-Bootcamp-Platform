package com.solcho.bootcamp.user.service;

import com.solcho.bootcamp.common.exception.ApiException;
import com.solcho.bootcamp.user.dto.ChangePasswordRequest;
import com.solcho.bootcamp.user.dto.UpdateProfileRequest;
import com.solcho.bootcamp.user.entity.User;
import com.solcho.bootcamp.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로그인한 사용자 본인의 정보 조회/수정/삭제 (사용자 CRUD).
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public User updateProfile(Long userId, UpdateProfileRequest req) {
        User user = getById(userId);
        user.updateProfile(req.nickname(), req.profileImageUrl());
        return user;
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest req) {
        User user = getById(userId);
        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("현재 비밀번호가 올바르지 않습니다.");
        }
        user.changePassword(passwordEncoder.encode(req.newPassword()));
    }

    @Transactional
    public void deleteAccount(Long userId) {
        User user = getById(userId);
        userRepository.delete(user);
    }
}
