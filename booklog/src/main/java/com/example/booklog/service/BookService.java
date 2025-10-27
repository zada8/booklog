package com.example.booklog.service;

import com.example.booklog.entity.Book;
import com.example.booklog.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    // 모든 책 조회
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
    
    // 최근 등록순으로 조회
    public List<Book> getRecentBooks() {
        return bookRepository.findAllByOrderByCreatedAtDesc();
    }
    
    // ID로 책 찾기
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }
    
    // 책 저장 (등록/수정)
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }
    
    // 책 삭제
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }
    
    // 제목으로 검색
    public List<Book> searchByTitle(String title) {
        return bookRepository.findByTitleContaining(title);
    }
    
    // 장르로 검색
    public List<Book> findByGenre(String genre) {
        return bookRepository.findByGenre(genre);
    }
    
    public List<Book> findByPublisher(String publisher) {
        return bookRepository.findByPublisherContaining(publisher);
    }
    
 // 통합 검색
    public List<Book> searchBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBooks();
        }
        
        // 제목, 저자, 출판사에서 검색
        List<Book> results = new ArrayList<>();
        results.addAll(bookRepository.findByTitleContaining(keyword));
        results.addAll(bookRepository.findByAuthorContaining(keyword));
        results.addAll(bookRepository.findByPublisherContaining(keyword));
        
        // 중복 제거
        return results.stream().distinct().collect(Collectors.toList());
    }
}