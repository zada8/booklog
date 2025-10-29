package com.example.booklog.repository;

import com.example.booklog.entity.Comment;
import com.example.booklog.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // 게시글별 댓글 목록 (최신순)
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);
    
    // 게시글의 댓글 개수
    int countByPost(Post post);
}