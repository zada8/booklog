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

    // 사용자 정보 업데이트
    public User updateUser(User user, String name, String email, String currentPassword, String newPassword) {
        // 이름 업데이트
        if (name != null && !name.trim().isEmpty()) {
            user.setName(name);
        }

        // 이메일 업데이트
        if (email != null && !email.trim().isEmpty()) {
            user.setEmail(email);
        }

        // 비밀번호 변경 (새 비밀번호가 입력된 경우에만)
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            // 현재 비밀번호 확인
            if (currentPassword != null && passwordEncoder.matches(currentPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
            } else {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }
        }

        return userRepository.save(user);
    }

    // 사용자 삭제
    public void deleteUser(User user) {
        userRepository.delete(user);
    }
}