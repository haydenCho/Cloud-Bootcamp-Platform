package com.solcho.bootcamp.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "아이디를 입력해주세요.")
        @Size(min = 3, max = 50, message = "아이디는 3~50자여야 합니다.")
        String loginId,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 4, max = 100, message = "비밀번호는 4자 이상이어야 합니다.")
        String password,

        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(min = 1, max = 50, message = "닉네임은 50자 이하여야 합니다.")
        String nickname
) {
}
