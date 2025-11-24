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

import com.example.booklog.entity.AiRecommendedBookDto;
import com.example.booklog.entity.Book;
import com.example.booklog.entity.BookApiDto;
import com.example.booklog.entity.RecommendedBookDto;
import com.example.booklog.entity.User;
import com.example.booklog.service.AiRecommendationService;
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

    @Autowired
    private AiRecommendationService aiRecommendationService;
    
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

        // AI 추천 도서
        if (authentication != null) {
            try {
                User user = userService.findByUsername(authentication.getName());
                List<AiRecommendedBookDto> aiRecommendedBooks = aiRecommendationService.getRecommendations(user, 5);
                model.addAttribute("aiRecommendedBooks", aiRecommendedBooks);
            } catch (Exception e) {
                model.addAttribute("aiRecommendedBooks", new ArrayList<>());
            }
        } else {
            model.addAttribute("aiRecommendedBooks", new ArrayList<>());
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

        // AI 추천 도서
        if (authentication != null) {
            try {
                User user = userService.findByUsername(authentication.getName());
                List<AiRecommendedBookDto> aiRecommendedBooks = aiRecommendationService.getRecommendations(user, 5);
                model.addAttribute("aiRecommendedBooks", aiRecommendedBooks);
            } catch (Exception e) {
                model.addAttribute("aiRecommendedBooks", new ArrayList<>());
            }
        } else {
            model.addAttribute("aiRecommendedBooks", new ArrayList<>());
        }

        return "books/list";
    }
    
    // 새 책 등록 폼
    @GetMapping("/new")
    public String newBookForm(@RequestParam(required = false, defaultValue = "WANT_TO_READ") String status,
                             Model model) {
        Book book = new Book();
        book.setStatus(status);
        model.addAttribute("book", book);

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
    
 // 책 등록/수정 처리
    @PostMapping
    public String saveBook(@ModelAttribute Book book,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {

        // 로그인 체크
        if (authentication == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        // 새 책 등록
        if (book.getId() == null) {
            User user = userService.findByUsername(authentication.getName());

            // 사용자 조회 실패 체크
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "사용자 정보를 찾을 수 없습니다.");
                return "redirect:/books";
            }

            book.setUser(user);

            // status 기본값 설정
            if (book.getStatus() == null || book.getStatus().isEmpty()) {
                book.setStatus("WANT_TO_READ");
            }

            // rating은 READ 상태일 때만 필수, 나머지는 null 허용
            // 폼에서 전달되지 않으면 null로 유지

            bookService.saveBook(book);
            redirectAttributes.addFlashAttribute("message", "책이 등록되었습니다.");
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
            redirectAttributes.addFlashAttribute("message", "책이 수정되었습니다.");
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

    // 사서 추천 도서 팝업 상세 정보
    @GetMapping("/recommend-popup")
    public String recommendPopup(@RequestParam String isbn,
                                 @RequestParam String title,
                                 @RequestParam String author,
                                 @RequestParam(required = false) String publisher,
                                 @RequestParam(required = false) String contents,
                                 @RequestParam(required = false) String category,
                                 @RequestParam(required = false) String coverUrl,
                                 @RequestParam(required = false) Integer publishYear,
                                 Model model) {
        // RecommendedBookDto 객체 생성
        RecommendedBookDto book = new RecommendedBookDto();
        book.setIsbn(isbn);
        book.setTitle(title);
        book.setAuthor(author);
        book.setPublisher(publisher);

        // HTML 태그 제거 처리
        if (contents != null && !contents.isEmpty()) {
            book.setContents(removeHtmlTags(contents));
        } else {
            book.setContents(contents);
        }

        book.setCategory(category);
        book.setCoverUrl(coverUrl);
        book.setPublishYear(publishYear);

        model.addAttribute("book", book);
        return "books/recommend-popup";
    }

    // HTML 태그 제거 유틸리티 메서드
    private String removeHtmlTags(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        // HTML 태그 제거
        String text = html.replaceAll("<[^>]*>", "");

        // HTML 엔티티 디코딩
        text = text.replace("&nbsp;", " ")
                   .replace("&quot;", "\"")
                   .replace("&apos;", "'")
                   .replace("&lt;", "<")
                   .replace("&gt;", ">")
                   .replace("&amp;", "&");

        // 연속된 공백을 하나로 축소
        text = text.replaceAll("\\s+", " ");

        // 앞뒤 공백 제거
        text = text.trim();

        return text;
    }

    // AI 추천 도서 팝업 상세 정보
    @GetMapping("/ai-recommend-popup")
    public String aiRecommendPopup(@RequestParam String title,
                                   @RequestParam String author,
                                   @RequestParam(required = false) String publisher,
                                   @RequestParam(required = false) String description,
                                   @RequestParam(required = false) String reason,
                                   @RequestParam(required = false) String category,
                                   Model model) {
        // AiRecommendedBookDto 객체 생성
        AiRecommendedBookDto book = new AiRecommendedBookDto();
        book.setTitle(title);
        book.setAuthor(author);
        book.setPublisher(publisher);
        book.setDescription(description);
        book.setReason(reason);
        book.setCategory(category);

        model.addAttribute("book", book);
        return "books/ai-recommend-popup";
    }

    // AI 추천 도서에서 등록 폼으로 이동
    @GetMapping("/new-from-ai-recommend")
    public String newBookFromAiRecommend(@RequestParam String title,
                                         @RequestParam String author,
                                         @RequestParam(required = false) String publisher,
                                         @RequestParam(required = false, defaultValue = "WANT_TO_READ") String status,
                                         Model model) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setPublisher(publisher);
        book.setStatus(status);

        model.addAttribute("book", book);
        model.addAttribute("fromAiRecommend", true);

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