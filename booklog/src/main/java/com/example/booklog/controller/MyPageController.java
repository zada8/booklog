package com.example.booklog.controller;

import com.example.booklog.entity.Book;
import com.example.booklog.entity.User;
import com.example.booklog.service.BookService;
import com.example.booklog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/mypage")
public class MyPageController {
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private UserService userService;
    
    // 마이페이지 메인
    @GetMapping
    public String myPage() {
        return "mypage/index";
    }
    
    //추가: 내 도서 목록
    @GetMapping("/books")
    public String myBooks(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        
        // 내가 등록한 책만 가져오기
        List<Book> myBooks = bookService.getBooksByUser(user);
        
        // 평균 평점 계산
        double averageRating = 0.0;
        if (!myBooks.isEmpty()) {
            int totalRating = myBooks.stream()
                    .mapToInt(Book::getRating)
                    .sum();
            averageRating = (double) totalRating / myBooks.size();
        }
        
        model.addAttribute("books", myBooks);
        model.addAttribute("user", user);
        model.addAttribute("currentUsername", authentication.getName());
        model.addAttribute("averageRating", averageRating);
        
        return "mypage/books";
    }
}