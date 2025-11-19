package com.example.booklog.service;

import com.example.booklog.entity.User;
import com.example.booklog.repository.UserRepository;
import com.example.booklog.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        return new CustomUserDetails(
                user.getUsername(),
                user.getPassword(),
                user.getName(),  // 실제 이름 추가
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}