package com.example.booklog.controller;

import java.util.ArrayList;
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

import com.example.booklog.entity.Book;
import com.example.booklog.entity.BookApiDto;
import com.example.booklog.entity.RecommendedBookDto;
import com.example.booklog.entity.User;
import com.example.booklog.service.BookService;
import com.example.booklog.service.KakaoBookApiService;
import com.example.booklog.service.NationalLibraryApiService;
import com.example.booklog.service.UserService;

@Controller
@RequestMapping("/books")
public class BookController {
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private KakaoBookApiService kakaoApiService;
    
    @Autowired
    private NationalLibraryApiService nlApiService;
    
    // 내 책 목록 (기본)
    @GetMapping
    public String list(@RequestParam(required = false, defaultValue = "ALL") String status,
                      Model model,
                      Authentication authentication) {
        List<Book> books;

        // Authentication null 체크
        if (authentication != null) {
            User user = userService.findByUsername(authentication.getName());
            books = bookService.getBooksByUserAndStatus(user, status);
            model.addAttribute("currentUsername", authentication.getName());
        } else {
            books = new ArrayList<>();
            model.addAttribute("currentUsername", null);
        }

        model.addAttribute("books", books);
        model.addAttribute("viewMode", "my"); // 현재 보기 모드
        model.addAttribute("currentStatus", status); // 현재 선택된 상태

        // 사서 추천 도서
        try {
            List<RecommendedBookDto> recommendedBooks = nlApiService.getLatestRecommendedBooks(5);
            model.addAttribute("recommendedBooks", recommendedBooks);
        } catch (Exception e) {
            model.addAttribute("recommendedBooks", new ArrayList<>());
        }

        return "books/list";
    }

    // 다른 사람들의 책 목록
    @GetMapping("/others")
    public String listOthers(@RequestParam(required = false, defaultValue = "ALL") String status,
                            Model model,
                            Authentication authentication) {
        List<Book> books;

        // Authentication null 체크
        if (authentication != null) {
            User user = userService.findByUsername(authentication.getName());
            books = bookService.getBooksByOthersAndStatus(user, status);
            model.addAttribute("currentUsername", authentication.getName());
        } else {
            books = bookService.getAllBooks();
            model.addAttribute("currentUsername", null);
        }

        model.addAttribute("books", books);
        model.addAttribute("viewMode", "others"); // 현재 보기 모드
        model.addAttribute("currentStatus", status); // 현재 선택된 상태

        // 사서 추천 도서
        try {
            List<RecommendedBookDto> recommendedBooks = nlApiService.getLatestRecommendedBooks(5);
            model.addAttribute("recommendedBooks", recommendedBooks);
        } catch (Exception e) {
            model.addAttribute("recommendedBooks", new ArrayList<>());
        }

        return "books/list";
    }
    
    /*
    // 새 책 등록 폼
    @GetMapping("/new")
    public String newBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "books/form";
    }
    */
    
 // 책 등록/수정 처리
    @PostMapping
    public String saveBook(@ModelAttribute Book book,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {

        // 새 책 등록
        if (book.getId() == null) {
            User user = userService.findByUsername(authentication.getName());
            book.setUser(user);

            // status 기본값 설정
            if (book.getStatus() == null || book.getStatus().isEmpty()) {
                book.setStatus("WANT_TO_READ");
            }

            // rating은 READ 상태일 때만 필수, 나머지는 null 허용
            // 폼에서 전달되지 않으면 null로 유지

            bookService.saveBook(book);
        }
        // 기존 책 수정
        else {
            Book existingBook = bookService.getBookById(book.getId());

            // 권한 체크
            if (!existingBook.getUser().getUsername().equals(authentication.getName())) {
                redirectAttributes.addFlashAttribute("error", "수정 권한이 없습니다.");
                return "redirect:/books";
            }

            // status 유지
            if (book.getStatus() == null || book.getStatus().isEmpty()) {
                book.setStatus(existingBook.getStatus());
            }

            book.setUser(existingBook.getUser());
            bookService.saveBook(book);
        }

        return "redirect:/books";
    }
    
    // 책 상세보기
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id);
        model.addAttribute("book", book);
        return "books/detail";
    }
    
    // 책 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                          Model model,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        Book book = bookService.getBookById(id);

        // 권한 체크
        if (!book.getUser().getUsername().equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute("error", "수정 권한이 없습니다.");
            return "redirect:/books";
        }

        model.addAttribute("book", book);

        // 상태에 따라 다른 폼으로 이동
        switch (book.getStatus()) {
            case "READ":
                return "books/read-form";
            case "READING":
                return "books/reading-form";
            case "WANT_TO_READ":
            default:
                return "books/want-to-read-form";
        }
    }
    
    // 책 삭제
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                        Authentication authentication,
                        RedirectAttributes redirectAttributes) {
        Book book = bookService.getBookById(id);
        
        // 권한 체크
        if (!book.getUser().getUsername().equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute("error", "삭제 권한이 없습니다.");
            return "redirect:/books";
        }
        
        bookService.deleteBook(id);
        redirectAttributes.addFlashAttribute("message", "책이 삭제되었습니다.");
        return "redirect:/books";
    }
    
    // 검색 기능
    @GetMapping("/search")
    public String searchBooks(@RequestParam String keyword, 
                             Model model,
                             Authentication authentication) {
        List<Book> books = bookService.searchBooks(keyword);
        model.addAttribute("books", books);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentUsername", authentication.getName());
        return "books/list";
    }
    
    // API 검색 페이지
    @GetMapping("/search-api")
    public String searchApiPage(@RequestParam(required = false) String query, Model model) {
        if (query != null && !query.trim().isEmpty()) {
            List<BookApiDto> books = kakaoApiService.search(query);
            model.addAttribute("books", books);
            model.addAttribute("query", query);
        }
        return "books/search";
    }
    
    // API에서 선택한 책으로 등록 폼 이동
    @GetMapping("/new-from-api")
    public String newBookFromApi(@RequestParam String isbn,
    		 					@RequestParam(required = false, defaultValue = "WANT_TO_READ") String status,
    		 					Model model) {
        BookApiDto apiBook = kakaoApiService.getBookByIsbn(isbn);

        Book book = new Book();
        if (apiBook != null) {
            book.setTitle(apiBook.getTitle());
            book.setAuthor(apiBook.getAuthor());
            book.setPublisher(apiBook.getPublisher());
        }
        book.setStatus(status);

        model.addAttribute("book", book);
        model.addAttribute("fromApi", true);

        // 상태에 따라 다른 폼으로 이동
        switch (status) {
            case "READ":
                return "books/read-form";
            case "READING":
                return "books/reading-form";
            case "WANT_TO_READ":
            default:
                return "books/want-to-read-form";
        }
    }
    
    // 사서 추천 도서에서 등록 폼으로 이동
    @GetMapping("/new-from-recommend")
    public String newBookFromRecommend(@RequestParam String isbn,
    								   @RequestParam(required = false, defaultValue = "WANT_TO_READ") String status,
    								   Model model) {
        Book book = new Book();

        BookApiDto apiBook = kakaoApiService.getBookByIsbn(isbn);

        if (apiBook != null) {
            book.setTitle(apiBook.getTitle());
            book.setAuthor(apiBook.getAuthor());
            book.setPublisher(apiBook.getPublisher());
        }
        book.setStatus(status);

        model.addAttribute("book", book);
        model.addAttribute("fromRecommend", true);

        // 상태에 따라 다른 폼으로 이동
        switch (status) {
            case "READ":
                return "books/read-form";
            case "READING":
                return "books/reading-form";
            case "WANT_TO_READ":
            default:
                return "books/want-to-read-form";
        }
    }
}