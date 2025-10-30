package com.example.booklog.entity;

public class BookApiDto {
    private String title;           // 제목
    private String author;          // 저자
    private String publisher;       // 출판사
    private String isbn;            // ISBN
    private String coverUrl;        // 표지 URL
    private String publishDate;     // 출판일
    private String page;            // 페이지
    private String subject;         // 주제/장르
    private String description;
    
    // 기본 생성자
    public BookApiDto() {
    }
    
    // 모든 필드 생성자
    public BookApiDto(String title, String author, String publisher, String isbn, 
                      String coverUrl, String publishDate, String page, String subject) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.isbn = isbn;
        this.coverUrl = coverUrl;
        this.publishDate = publishDate;
        this.page = page;
        this.subject = subject;
    }
    
    // Getter & Setter
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
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getCoverUrl() {
        return coverUrl;
    }
    
    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
    
    public String getPublishDate() {
        return publishDate;
    }
    
    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }
    
    public String getPage() {
        return page;
    }
    
    public void setPage(String page) {
        this.page = page;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}