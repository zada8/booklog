package com.example.booklog.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booklog.entity.Comment;
import com.example.booklog.entity.Post;
import com.example.booklog.entity.User;
import com.example.booklog.service.PostService;
import com.example.booklog.service.UserService;
import com.example.booklog.service.CommentService;


@Controller
@RequestMapping("/community")
public class PostController {
	
	@Autowired
	private CommentService commentService;
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private UserService userService;
    
    // 게시글 목록
    @GetMapping
    public String list(@RequestParam(required = false) String category,
                      @RequestParam(required = false) String keyword,
                      Model model,
                      Authentication authentication) {
        
        List<Post> posts;
        
        // 검색어가 있으면 검색
        if (keyword != null && !keyword.trim().isEmpty()) {
            if (category != null && !category.isEmpty()) {
                posts = postService.searchPostsByCategory(category, keyword);
            } else {
                posts = postService.searchPosts(keyword);
            }
        }
        // 카테고리별 필터
        else if (category != null && !category.isEmpty()) {
            posts = postService.getPostsByCategory(category);
        }
        // 전체 목록
        else {
            posts = postService.getAllPosts();
        }
        
        model.addAttribute("posts", posts);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);
        
        if (authentication != null) {
            model.addAttribute("currentUsername", authentication.getName());
        }
        
        return "community/list";
    }
    
    // 글쓰기 폼
    @GetMapping("/new")
    public String newPostForm(Model model, Authentication authentication) {
        //로그인 체크
        if (authentication == null) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("post", new Post());
        return "community/form";
    }
    
    // 글 저장
    @PostMapping
    public String savePost(@ModelAttribute Post post,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        
        User user = userService.findByUsername(authentication.getName());
        
        // 새 글 작성
        if (post.getId() == null) {
            post.setUser(user);
            post.setViewCount(0);
        }
        // 기존 글 수정
        else {
            Post existingPost = postService.getPostById(post.getId());
            
            // 권한 체크
            if (!existingPost.getUser().getUsername().equals(authentication.getName())) {
                redirectAttributes.addFlashAttribute("error", "수정 권한이 없습니다.");
                return "redirect:/community";
            }
            
            post.setUser(existingPost.getUser());
            post.setViewCount(existingPost.getViewCount());
        }
        
        postService.savePost(post);
        return "redirect:/community";
    }
    
    // 글 상세보기
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                        Model model,
                        Authentication authentication) {
        Post post = postService.getPostById(id);
        
        List<Comment> comments = commentService.getCommentsByPost(post);
        
        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("commentCount", comments.size());
        
        if (authentication != null) {
            model.addAttribute("currentUsername", authentication.getName());
        }
        
        return "community/detail";
    }
    
    // 글 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                          Model model,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        Post post = postService.getPostById(id);
        
        // 권한 체크
        if (!post.getUser().getUsername().equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute("error", "수정 권한이 없습니다.");
            return "redirect:/community";
        }
        
        model.addAttribute("post", post);
        return "community/form";
    }
    
    // 글 삭제
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                        Authentication authentication,
                        RedirectAttributes redirectAttributes) {
        Post post = postService.getPostById(id);
        
        // 권한 체크
        if (!post.getUser().getUsername().equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute("error", "삭제 권한이 없습니다.");
            return "redirect:/community";
        }
        
        postService.deletePost(id);
        redirectAttributes.addFlashAttribute("message", "게시글이 삭제되었습니다.");
        return "redirect:/community";
    }
}