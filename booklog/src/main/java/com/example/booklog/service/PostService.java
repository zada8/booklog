package com.example.booklog.service;

import com.example.booklog.entity.Post;
import com.example.booklog.entity.User;
import com.example.booklog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PostService {
    
    @Autowired
    private PostRepository postRepository;
    
    // 전체 목록
    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }
    
    // 카테고리별 목록
    public List<Post> getPostsByCategory(String category) {
        return postRepository.findByCategoryOrderByCreatedAtDesc(category);
    }
    
    // 작성자별 목록
    public List<Post> getPostsByUser(User user) {
        return postRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    // 게시글 상세 (조회수 증가)
    public Post getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        
        // 조회수 증가
        post.setViewCount(post.getViewCount() + 1);
        return postRepository.save(post);
    }
    
    // 게시글 저장
    public Post savePost(Post post) {
        return postRepository.save(post);
    }
    
    // 게시글 삭제
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
    
    // 검색
    public List<Post> searchPosts(String keyword) {
        return postRepository.findByTitleContainingOrderByCreatedAtDesc(keyword);
    }
    
    // 카테고리 + 검색
    public List<Post> searchPostsByCategory(String category, String keyword) {
        return postRepository.findByCategoryAndTitleContainingOrderByCreatedAtDesc(category, keyword);
    }
}