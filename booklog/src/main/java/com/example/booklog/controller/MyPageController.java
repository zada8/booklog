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
import org.springframework.web.bind.annotation.RequestParam;

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
    
    // 내 도서 목록
    @GetMapping("/books")
    public String myBooks(@RequestParam(required = false) String status,
                         Model model, 
                         Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        
        List<Book> myBooks;
        String statusName;
        
        //  상태별 필터링
        if (status != null && !status.isEmpty()) {
            myBooks = bookService.getBooksByUserAndStatus(user, status);
            switch (status) {
                case "READ":
                    statusName = "읽은 책";
                    break;
                case "READING":
                    statusName = "읽고 있는 책";
                    break;
                case "WANT_TO_READ":
                    statusName = "읽고 싶은 책";
                    break;
                default:
                    statusName = "전체";
            }
        } else {
            myBooks = bookService.getBooksByUser(user);
            statusName = "전체";
        }
        
        // 평균 평점 계산 (rating이 null이 아닌 책들만)
        Double averageRating = null;
        if (!myBooks.isEmpty()) {
            List<Book> booksWithRating = myBooks.stream()
                    .filter(book -> book.getRating() != null)
                    .toList();

            if (!booksWithRating.isEmpty()) {
                int totalRating = booksWithRating.stream()
                        .mapToInt(Book::getRating)
                        .sum();
                averageRating = (double) totalRating / booksWithRating.size();
            }
        }
        
        model.addAttribute("books", myBooks);
        model.addAttribute("user", user);
        model.addAttribute("currentUsername", authentication.getName());
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("statusName", statusName);
        model.addAttribute("selectedStatus", status);
        
        return "mypage/books";
    }
}