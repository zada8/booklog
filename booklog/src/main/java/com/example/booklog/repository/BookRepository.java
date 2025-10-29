package com.example.booklog.repository;

import com.example.booklog.entity.Book;
import com.example.booklog.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    // 제목으로 검색
    List<Book> findByTitleContaining(String title);
    
    // 저자로 검색
    List<Book> findByAuthor(String author);
    
    // 장르로 검색
    List<Book> findByGenre(String genre);
    
    // 출판사로 검색
    List<Book> findByPublisher(String publisher);
    List<Book> findByPublisherContaining(String publisher);
   
    
    // 평점으로 정렬해서 가져오기
    List<Book> findAllByOrderByRatingDesc();
    
    // 최근 등록순으로 가져오기
    List<Book> findAllByOrderByCreatedAtDesc();
    
    // 저자로 검색 (포함)
    List<Book> findByAuthorContaining(String author);

    // 출판사로 검색 (포함)
    //List<Book> findByPublisherContaining(String publisher);
    
    List<Book> findByUserOrderByCreatedAtDesc(User user);
}