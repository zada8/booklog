package com.example.booklog.service;

import com.example.booklog.entity.Comment;
import com.example.booklog.entity.Post;
import com.example.booklog.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommentService {
    
    @Autowired
    private CommentRepository commentRepository;
    
    // 게시글별 댓글 목록
    public List<Comment> getCommentsByPost(Post post) {
        return commentRepository.findByPostOrderByCreatedAtDesc(post);
    }
    
    // 댓글 개수
    public int getCommentCount(Post post) {
        return commentRepository.countByPost(post);
    }
    
    // 댓글 저장
    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }
    
    // 댓글 조회
    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));
    }
    
    // 댓글 삭제
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
}