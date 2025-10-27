package com.example.booklog.controller;

import com.example.booklog.entity.Book;
import com.example.booklog.service.BookService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/books")
public class BookController {
	
	// 검색 기능
	@GetMapping("/search")
	public String searchBooks(@RequestParam String keyword, Model model) {
	    List<Book> books = bookService.searchBooks(keyword);
	    model.addAttribute("books", books);
	    model.addAttribute("keyword", keyword);
	    return "books/list";
	}
    
    @Autowired
    private BookService bookService;
    
    // 책 목록 페이지
    @GetMapping
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.getRecentBooks());
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
    
    
}

