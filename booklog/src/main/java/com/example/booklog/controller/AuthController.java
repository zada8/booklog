package com.example.booklog.controller;

import com.example.booklog.entity.User;
import com.example.booklog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    // 로그인 페이지
    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error,
                           @RequestParam(required = false) String logout,
                           Model model) {
        if (error != null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 잘못되었습니다.");
        }
        if (logout != null) {
            model.addAttribute("message", "로그아웃 되었습니다.");
        }
        return "auth/login";
    }
    
    // 회원가입 페이지
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }
    
    // 회원가입 처리
    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        // 중복 체크
        if (userService.isUsernameExists(user.getUsername())) {
            model.addAttribute("error", "이미 사용 중인 아이디입니다.");
            return "auth/register";
        }
        
        if (userService.isEmailExists(user.getEmail())) {
            model.addAttribute("error", "이미 사용 중인 이메일입니다.");
            return "auth/register";
        }
        
        // 회원가입
        userService.register(user);
        return "redirect:/auth/login?registered=true";
    }
}