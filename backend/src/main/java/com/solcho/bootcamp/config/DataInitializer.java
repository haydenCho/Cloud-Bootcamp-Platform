package com.solcho.bootcamp.config;

import com.solcho.bootcamp.user.entity.Role;
import com.solcho.bootcamp.user.entity.User;
import com.solcho.bootcamp.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 애플리케이션 시작 시 admin / guest 계정을 없으면 생성한다.
 * admin 비밀번호는 환경 변수(app.init.admin-password)로 주입되며, 코드에 하드코딩하지 않는다.
 */
@Configuration
@EnableConfigurationProperties(InitProperties.class)
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final InitProperties props;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(InitProperties props,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.props = props;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    CommandLineRunner seedAccounts() {
        return args -> {
            seedAdmin();
            seedGuest();
        };
    }

    void seedAdmin() {
        if (userRepository.existsByLoginId(props.adminLoginId())) {
            return;
        }
        if (props.adminPassword() == null || props.adminPassword().isBlank()) {
            log.warn("ADMIN_PASSWORD 가 비어 있어 admin 계정을 생성하지 않았습니다. .env 에 ADMIN_PASSWORD 를 설정하세요.");
            return;
        }
        userRepository.save(User.builder()
                .loginId(props.adminLoginId())
                .passwordHash(passwordEncoder.encode(props.adminPassword()))
                .nickname(props.adminNickname())
                .role(Role.ADMIN)
                .build());
        log.info("초기 admin 계정 생성 완료: loginId={}", props.adminLoginId());
    }

    void seedGuest() {
        if (userRepository.existsByLoginId(props.guestLoginId())) {
            return;
        }
        userRepository.save(User.builder()
                .loginId(props.guestLoginId())
                .passwordHash(passwordEncoder.encode(props.guestPassword()))
                .nickname(props.guestNickname())
                .role(Role.USER)
                .build());
        log.info("시연용 guest 계정 생성 완료: loginId={}", props.guestLoginId());
    }
}
