package com.example.booklog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
            	//누구나 접근 가능 (공개 페이지)
                .requestMatchers(
                		"/",				//랜딩 페이지 
                		"/auth/login", 		//로그인 페이지
                		"/auth/register", 	//회원가입 페이지
                		"/books",			//책 목록 (룰러보기)
                		"/books/{id}",		//책 상세보기
                		"/books/search",	//검색
                		"/css/**", 			//css 파일
                		"/js/**"			//js 파일
                		).permitAll()
                
                //로그인 필요 (보호된 페이지)
                .requestMatchers(
                		"books/new", 			//책 등록
                		"books/{id}/edit",		//책 수정
                		"books/{id}/delete",	//책 삭제
                		"/mypage/**"			//마이페이지
                		).authenticated() 
                //그 외 모든 요청도 로그인 필요
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/books", true)
                .failureUrl("/auth/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout"))
                .logoutSuccessUrl("/auth/login?logout=true")
                .permitAll()
            );
        
        return http.build();
    }
}