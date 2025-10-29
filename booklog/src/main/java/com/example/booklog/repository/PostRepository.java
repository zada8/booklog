package com.example.booklog.repository;

import com.example.booklog.entity.Post;
import com.example.booklog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 전체 목록 (최신순)
    List<Post> findAllByOrderByCreatedAtDesc();
    
    // 카테고리별 목록
    List<Post> findByCategoryOrderByCreatedAtDesc(String category);
    
    // 작성자별 목록
    List<Post> findByUserOrderByCreatedAtDesc(User user);
    
    // 제목 검색
    List<Post> findByTitleContainingOrderByCreatedAtDesc(String keyword);
    
    // 제목 + 카테고리 검색
    List<Post> findByCategoryAndTitleContainingOrderByCreatedAtDesc(String category, String keyword);
}