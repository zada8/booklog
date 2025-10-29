package com.example.booklog.controller;

import com.example.booklog.entity.Comment;
import com.example.booklog.entity.Post;
import com.example.booklog.entity.User;
import com.example.booklog.service.CommentService;
import com.example.booklog.service.PostService;
import com.example.booklog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/community/{postId}/comments")
public class CommentController {
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private UserService userService;
    
    // 댓글 작성
    @PostMapping
    public String createComment(@PathVariable Long postId,
                               @RequestParam String content,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        
        if (authentication == null) {
            return "redirect:/auth/login";
        }
        
        Post post = postService.getPostById(postId);
        User user = userService.findByUsername(authentication.getName());
        
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPost(post);
        comment.setUser(user);
        
        commentService.saveComment(comment);
        //redirectAttributes.addFlashAttribute("message", "댓글이 등록되었습니다.");
        
        return "redirect:/community/" + postId;
    }
    
    // 댓글 삭제
    @GetMapping("/{commentId}/delete")
    public String deleteComment(@PathVariable Long postId,
                                @PathVariable Long commentId,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        
        Comment comment = commentService.getCommentById(commentId);
        
        // 권한 체크
        if (!comment.getUser().getUsername().equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute("error", "삭제 권한이 없습니다.");
            return "redirect:/community/" + postId;
        }
        
        commentService.deleteComment(commentId);
        //redirectAttributes.addFlashAttribute("message", "댓글이 삭제되었습니다.");
        
        return "redirect:/community/" + postId;
    }
}