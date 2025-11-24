package com.example.booklog.entity;

/**
 * AI 추천 책 정보를 담는 DTO
 */
public class AiRecommendedBookDto {
    private String title;           // 제목
    private String author;          // 저자
    private String publisher;       // 출판사
    private String isbn;            // ISBN
    private String coverUrl;        // 표지 URL
    private String description;     // 책 설명
    private String reason;          // AI 추천 이유
    private String category;        // 장르/카테고리

    // 기본 생성자
    public AiRecommendedBookDto() {
    }

    // 전체 필드 생성자
    public AiRecommendedBookDto(String title, String author, String publisher, String isbn,
                                String coverUrl, String description, String reason, String category) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.isbn = isbn;
        this.coverUrl = coverUrl;
        this.description = description;
        this.reason = reason;
        this.category = category;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
