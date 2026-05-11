package org.example.paintonlumia.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 비밀번호를 안전하게 암호화해주는 객체
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(basic -> basic.disable()) // REST API이므로 기본 설정 사용 안함
                .csrf(csrf -> csrf.disable()) // JWT를 사용하므로 CSRF 보호 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안함
                .authorizeHttpRequests(auth -> auth
                        // 로그인, 회원가입, 정적 리소스(HTML, CSS, JS), 웹소켓, 갤러리는 모두에게 허용
                        .requestMatchers("/api/auth/**", "/api/archive/public/**", "/ws-paint/**").permitAll()
                        .requestMatchers("/", "/*.html", "/favicon.ico", "/css/**", "/js/**", "/images/**").permitAll()

                        // 관리자 API는 ADMIN 권한만 허용 (추후 역할 기반 제어시 사용)
                        // 나머지 모든 API 요청은 인증(JWT) 필요
                        .anyRequest().authenticated()
                )
                // 내가 만든 JwtAuthenticationFilter를 스프링의 기본 인증 필터 앞에 추가
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}