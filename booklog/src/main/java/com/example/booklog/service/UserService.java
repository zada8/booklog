package com.example.booklog.service;

import com.example.booklog.entity.User;
import com.example.booklog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // 회원가입
    public User register(User user) {
        // 비밀번호 암호화
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 기본 역할 설정
        user.setRole("ROLE_USER");
        return userRepository.save(user);
    }
    
    // username으로 사용자 찾기
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    
    // username 중복 체크
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    // email 중복 체크
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}