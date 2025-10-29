package com.example.booklog.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    private String author;
    
    private String publisher;
    
    private String genre;
    
    @Column(nullable = false)
    private Integer rating; // 1-5
    
    @Column(columnDefinition = "TEXT")
    private String review;
    
    private LocalDate readDate;
    
    //독서 상태
    @Column(nullable = false)
    private String status = "WANT_TO_READ"; // READ, READING, WANT_TO_READ

    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    // 생성 시 자동으로 현재 시간 저장
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    // 수정 시 자동으로 현재 시간 저장
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 기본 생성자 (JPA 필수)
    public Book() {}
    
    // Getter & Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getGenre() {
        return genre;
    }
    
    public void setGenre(String genre) {
        this.genre = genre;
    }
    
    public Integer getRating() {
        return rating;
    }
    
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    
    public String getReview() {
        return review;
    }
    
    public void setReview(String review) {
        this.review = review;
    }
    
    public LocalDate getReadDate() {
        return readDate;
    }
    
    public void setReadDate(LocalDate readDate) {
        this.readDate = readDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    //상태 한글명 반환
    public String getStatusName() {
        switch (status) {
            case "READ": return "읽은 책";
            case "READING": return "읽고 있는 책";
            case "WANT_TO_READ": return "읽고 싶은 책";
            default: return status;
        }
    }
}
