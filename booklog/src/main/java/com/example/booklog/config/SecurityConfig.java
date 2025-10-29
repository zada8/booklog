package com.example.booklog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
                // H2 Console 접근 허용
                .requestMatchers("/h2-console/**").permitAll()
                
                // 누구나 접근 가능
                .requestMatchers(
                    "/",
                    "/auth/login",
                    "/auth/register",
                    "/books",
                    "/books/{id}",
                    "/books/search",
                    "/community",
                    "/community/{id}",
                    "/css/**",
                    "/js/**"
                ).permitAll()
                
                // 로그인 필요
                .requestMatchers(
                    "/books/new",
                    "/books/{id}/edit",
                    "/books/{id}/delete",
                    "/books/search-api",
                    "/books/new-from-api",
                    "/community/new",   
                    "/community/{id}/edit", 
                    "/community/{id}/delete",
                    "/mypage/**"
                ).authenticated()
                
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
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/books?logout=true")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()));
        
        return http.build();
    }
}