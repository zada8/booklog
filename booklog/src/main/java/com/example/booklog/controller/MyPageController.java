package com.example.booklog.controller;

import com.example.booklog.entity.Book;
import com.example.booklog.entity.User;
import com.example.booklog.security.CustomUserDetails;
import com.example.booklog.service.BookService;
import com.example.booklog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collections;
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
    public String myPage(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("user", user);
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

    // 정보 수정 페이지
    @GetMapping("/edit")
    public String editPage(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("user", user);
        return "mypage/edit";
    }

    // 정보 수정 처리
    @PostMapping("/edit")
    public String updateUser(@RequestParam String name,
                           @RequestParam(required = false) String email,
                           @RequestParam(required = false) String currentPassword,
                           @RequestParam(required = false) String newPassword,
                           @RequestParam(required = false) String confirmPassword,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName());

            // 새 비밀번호 확인
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                if (!newPassword.equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("error", "새 비밀번호가 일치하지 않습니다.");
                    return "redirect:/mypage/edit";
                }
            }

            userService.updateUser(user, name, email, currentPassword, newPassword);

            // SecurityContext 업데이트 (이름 변경 시 헤더에 바로 반영)
            User updatedUser = userService.findByUsername(authentication.getName());
            CustomUserDetails updatedUserDetails = new CustomUserDetails(
                updatedUser.getUsername(),
                updatedUser.getPassword(),
                updatedUser.getName(),
                Collections.singletonList(new SimpleGrantedAuthority(updatedUser.getRole()))
            );

            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUserDetails,
                updatedUserDetails.getPassword(),
                updatedUserDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(newAuth);

            redirectAttributes.addFlashAttribute("success", "정보가 성공적으로 수정되었습니다.");
            return "redirect:/mypage";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/mypage/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "정보 수정 중 오류가 발생했습니다.");
            return "redirect:/mypage/edit";
        }
    }

    // 회원 탈퇴 처리
    @GetMapping("/delete")
    public String deleteUser(Authentication authentication,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName());
            userService.deleteUser(user);

            // 로그아웃 처리
            request.getSession().invalidate();

            redirectAttributes.addFlashAttribute("success", "회원 탈퇴가 완료되었습니다.");
            return "redirect:/";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "회원 탈퇴 중 오류가 발생했습니다.");
            return "redirect:/mypage";
        }
    }
}