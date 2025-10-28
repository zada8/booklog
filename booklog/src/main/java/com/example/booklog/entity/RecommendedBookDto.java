package com.example.booklog.entity;

public class RecommendedBookDto {
    private String title;           // 제목
    private String author;          // 저자
    private String publisher;       // 출판사
    private String isbn;            // ISBN
    private String coverUrl;        // 표지 이미지 경로
    private String contents;        // 내용
    private String category;        // 분류명
    private String categoryCode;    // 분류 코드
    private Integer publishYear;    // 발행년도
    
    // 기본 생성자
    public RecommendedBookDto() {
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
    
    public String getContents() {
        return contents;
    }
    
    public void setContents(String contents) {
        this.contents = contents;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getCategoryCode() {
        return categoryCode;
    }
    
    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }
    
    public Integer getPublishYear() {
        return publishYear;
    }
    
    public void setPublishYear(Integer publishYear) {
        this.publishYear = publishYear;
    }
}