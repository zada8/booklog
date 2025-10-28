package com.example.booklog.controller;

import com.example.booklog.entity.Book;
import com.example.booklog.entity.BookApiDto;
import com.example.booklog.entity.RecommendedBookDto;
import com.example.booklog.service.BookService;
import com.example.booklog.service.KakaoBookApiService;
import com.example.booklog.service.NationalLibraryApiService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/books")
public class BookController {
	
    @Autowired
    private BookService bookService;
    
	@Autowired
    private NationalLibraryApiService nlApiService;
	
    @Autowired
    private KakaoBookApiService kakaoApiService;
    
	// API 검색 페이지
	@GetMapping("/search-api")
	public String searchApiPage(@RequestParam(required = false) String query, Model model) {
	    if (query != null && !query.trim().isEmpty()) {
	        // TODO: API 호출해서 검색 결과 가져오기
	        List<BookApiDto> books = kakaoApiService.search(query);
	        model.addAttribute("books", books);
	        model.addAttribute("query", query);
	        
	    }
	    return "books/search";
	}

	// API에서 선택한 책으로 등록 폼 이동
	@GetMapping("/new-from-api")
	public String newBookFromApi(@RequestParam String isbn, Model model) {
	    BookApiDto apiBook = kakaoApiService.getBookByIsbn(isbn);
	    
	    Book book = new Book();
	    if (apiBook != null) {
	        // API 데이터를 Book 객체에 자동 입력
	        book.setTitle(apiBook.getTitle());
	        book.setAuthor(apiBook.getAuthor());
	        book.setPublisher(apiBook.getPublisher());
	        
	        // 장르는 기본값으로 설정 (사용자가 직접 선택하도록)
	        book.setGenre("소설");  // 또는 null로 두고 폼에서 선택하게
	    }
	    
	    model.addAttribute("book", book);
	    model.addAttribute("fromApi", true);
	    return "books/form";
	}
	
	// 검색 기능
	@GetMapping("/search")
	public String searchBooks(@RequestParam String keyword, Model model) {
	    List<Book> books = bookService.searchBooks(keyword);
	    model.addAttribute("books", books);
	    model.addAttribute("keyword", keyword);
	    return "books/list";
	}
    
    
    // 책 등록 폼
    @GetMapping("/new")
    public String createBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "books/form";
    }
    
    // 책 등록 처리
    @PostMapping
    public String saveBook(@ModelAttribute Book book) {
        bookService.saveBook(book);
        return "redirect:/books";
    }
    
    // 책 상세보기
    @GetMapping("/{id}")
    public String viewBook(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid book Id:" + id));
        model.addAttribute("book", book);
        return "books/detail";
    }
    
    // 책 수정 폼
    @GetMapping("/{id}/edit")
    public String editBookForm(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid book Id:" + id));
        model.addAttribute("book", book);
        return "books/form";
    }
    
    // 책 삭제
    @GetMapping("/{id}/delete")
    public String deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return "redirect:/books";
    }
    
 // 책 목록 조회 (사서 추천 포함)
    @GetMapping
    public String list(Model model) {
        List<Book> books = bookService.getAllBooks();
        model.addAttribute("books", books);
        
        // 사서 추천 도서 추가 (최신 5권)
        try {
            List<RecommendedBookDto> recommendedBooks = nlApiService.getLatestRecommendedBooks(5);
            model.addAttribute("recommendedBooks", recommendedBooks);
        } catch (Exception e) {
            model.addAttribute("recommendedBooks", new ArrayList<>());
        }
        
        return "books/list";
    }
    
    // 사서 추천 도서에서 등록 폼으로 이동
    @GetMapping("/new-from-recommend")
    public String newBookFromRecommend(@RequestParam String isbn, Model model) {
        Book book = new Book();
        
        // ISBN으로 일반 API에서도 검색해보기
        BookApiDto apiBook = nlApiService.getBookByIsbn(isbn);
        
        if (apiBook != null) {
            book.setTitle(apiBook.getTitle());
            book.setAuthor(apiBook.getAuthor());
            book.setPublisher(apiBook.getPublisher());
            
            // 장르 매핑
            String subject = apiBook.getSubject();
            if (subject != null) {
                if (subject.contains("문학")) {
                    book.setGenre("소설");
                } else if (subject.contains("역사")) {
                    book.setGenre("역사");
                } else if (subject.contains("과학") || subject.contains("기술")) {
                    book.setGenre("과학");
                } else if (subject.contains("철학") || subject.contains("종교")) {
                    book.setGenre("인문");
                } else {
                    book.setGenre("기타");
                }
            }
        }
        
        model.addAttribute("book", book);
        model.addAttribute("fromRecommend", true);  // 추천 도서에서 왔다는 표시
        return "books/form";
    }
}

