package com.example.booklog.entity;

public class AladinBookDto {
    private String title;           // 제목
    private String author;          // 저자
    private String publisher;       // 출판사
    private String isbn;            // ISBN13
    private String coverUrl;        // 표지 URL
    private String pubDate;         // 출판일
    private String description;     // 설명
    private String categoryName;    // 카테고리명
    private Integer priceStandard;  // 정가
    private Integer priceSales;     // 판매가
    private String link;            // 상품 링크

    // 기본 생성자
    public AladinBookDto() {
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

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getPriceStandard() {
        return priceStandard;
    }

    public void setPriceStandard(Integer priceStandard) {
        this.priceStandard = priceStandard;
    }

    public Integer getPriceSales() {
        return priceSales;
    }

    public void setPriceSales(Integer priceSales) {
        this.priceSales = priceSales;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
