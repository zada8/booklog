package com.example.booklog.service;

import com.example.booklog.entity.BookApiDto;
import com.example.booklog.entity.RecommendedBookDto;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class NationalLibraryApiService {
    
    @Value("${nl.api.url}")
    private String apiUrl;
    
    @Value("${nl.api.key}")
    private String apiKey;
    
    private static final String RECOMMEND_API_URL = "https://nl.go.kr/NL/search/openApi/saseoApi.do";
   
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * HTTP 헤더 생성 (User-Agent 포함)
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json");
        headers.set("Accept-Charset", "UTF-8");
        return headers;
    }
    
    /**
     * 제목으로 책 검색
     */
    public List<BookApiDto> searchByTitle(String title) {
        try {
            String url = UriComponentsBuilder.fromUriString(apiUrl)
                    .queryParam("cert_key", apiKey)
                    .queryParam("result_style", "json")
                    .queryParam("page_no", "1")
                    .queryParam("page_size", "20")
                    .queryParam("title", title)
                    .build()
                    .encode()
                    .toUriString();
            
            System.out.println("=== 제목 검색 API 요청 ===");
            System.out.println("URL: " + url);
            
            // 헤더 추가
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            String responseBody = response.getBody();
            System.out.println("응답 길이: " + (responseBody != null ? responseBody.length() : 0));
            
            return parseResponse(responseBody);
            
        } catch (Exception e) {
            System.out.println("=== 제목 검색 에러 ===");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * 저자로 책 검색
     */
    public List<BookApiDto> searchByAuthor(String author) {
        try {
            String url = UriComponentsBuilder.fromUriString(apiUrl)
                    .queryParam("cert_key", apiKey)
                    .queryParam("result_style", "json")
                    .queryParam("page_no", "1")
                    .queryParam("page_size", "20")
                    .queryParam("author", author)
                    .build()
                    .encode()
                    .toUriString();
            
            System.out.println("=== 저자 검색 API 요청 ===");
            
            // 헤더 추가
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            return parseResponse(response.getBody());
            
        } catch (Exception e) {
            System.out.println("=== 저자 검색 에러 ===");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * 출판사로 책 검색
     */
    public List<BookApiDto> searchByPublisher(String publisher) {
        try {
            String url = UriComponentsBuilder.fromUriString(apiUrl)
                    .queryParam("cert_key", apiKey)
                    .queryParam("result_style", "json")
                    .queryParam("page_no", "1")
                    .queryParam("page_size", "20")
                    .queryParam("publisher", publisher)
                    .build()
                    .encode()
                    .toUriString();
            
            // 헤더 추가
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            return parseResponse(response.getBody());
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * 제목 또는 저자로 통합 검색
     */
    public List<BookApiDto> search(String query) {
        System.out.println("=== 통합 검색 시작 ===");
        System.out.println("검색어: " + query);
        
        List<BookApiDto> results = new ArrayList<>();
        
        List<BookApiDto> titleResults = searchByTitle(query);
        results.addAll(titleResults);
        System.out.println("제목 검색 결과: " + titleResults.size() + "건");
        
        List<BookApiDto> authorResults = searchByAuthor(query);
        for (BookApiDto book : authorResults) {
            boolean exists = results.stream()
                    .anyMatch(b -> b.getIsbn() != null && b.getIsbn().equals(book.getIsbn()));
            if (!exists) {
                results.add(book);
            }
        }
        System.out.println("저자 검색 추가: " + authorResults.size() + "건");
        
        System.out.println("=== 최종 검색 결과: " + results.size() + "건 ===");
        
        return results;
    }
    
    /**
     * ISBN으로 책 상세 정보 검색
     */
    public BookApiDto getBookByIsbn(String isbn) {
        try {
            String url = UriComponentsBuilder.fromUriString(apiUrl)
                    .queryParam("cert_key", apiKey)
                    .queryParam("result_style", "json")
                    .queryParam("page_no", "1")
                    .queryParam("page_size", "1")
                    .queryParam("isbn", isbn)
                    .build()
                    .encode()
                    .toUriString();
            
            // 헤더 추가
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            List<BookApiDto> books = parseResponse(response.getBody());
            
            return books.isEmpty() ? null : books.get(0);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * JSON 응답 파싱
     */
    private List<BookApiDto> parseResponse(String jsonResponse) {
        List<BookApiDto> books = new ArrayList<>();
        
        try {
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                System.out.println("응답이 비어있습니다");
                return books;
            }
            
            JSONObject json = new JSONObject(jsonResponse);
            
            String totalCount = json.optString("TOTAL_COUNT", "0");
            System.out.println("총 결과 수: " + totalCount);
            
            if (json.has("docs")) {
                JSONArray docs = json.getJSONArray("docs");
                System.out.println("docs 배열 크기: " + docs.length());
                
                for (int i = 0; i < docs.length(); i++) {
                    JSONObject doc = docs.getJSONObject(i);
                    
                    BookApiDto book = new BookApiDto();
                    book.setTitle(doc.optString("TITLE", ""));
                    book.setAuthor(doc.optString("AUTHOR", ""));
                    book.setPublisher(doc.optString("PUBLISHER", ""));
                    book.setIsbn(doc.optString("EA_ISBN", ""));
                    book.setCoverUrl(doc.optString("TITLE_URL", ""));
                    book.setPublishDate(doc.optString("PUBLISH_PREDATE", ""));
                    book.setPage(doc.optString("PAGE", ""));
                    book.setSubject(doc.optString("SUBJECT", ""));
                    
                    if (book.getCoverUrl() == null || book.getCoverUrl().isEmpty()) {
                        book.setCoverUrl(null);
                    }
                    
                    System.out.println("책 파싱: " + book.getTitle());
                    books.add(book);
                }
            }
        } catch (Exception e) {
            System.out.println("=== JSON 파싱 에러 ===");
            e.printStackTrace();
        }
        
        return books;
    }
    
    // ==========================================
    // 사서추천도서 API (기존 코드 유지)
    // ==========================================
    
    /**
     * 최신 사서 추천 도서 가져오기 (랜덤)
     */
    public List<RecommendedBookDto> getLatestRecommendedBooks(int count) {
        try {
            // API에서 더 많은 책을 가져와서 랜덤으로 선택
            int fetchCount = Math.max(count * 10, 50); // 최소 50개 또는 요청 수의 10배

            String url = UriComponentsBuilder.fromUriString(RECOMMEND_API_URL)
                    .queryParam("key", apiKey)
                    .queryParam("startRowNumApi", "1")
                    .queryParam("endRowNumApi", String.valueOf(fetchCount))
                    .build()
                    .encode()
                    .toUriString();

            System.out.println("=== 사서추천 API 요청 (랜덤 선택) ===");

            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.trim().isEmpty()) {
                System.out.println("사서추천 API 응답 없음");
                return new ArrayList<>();
            }

            List<RecommendedBookDto> allBooks = parseRecommendXmlResponse(response);

            // 랜덤으로 섞어서 요청한 개수만큼 반환
            if (allBooks.size() > count) {
                Collections.shuffle(allBooks);
                return allBooks.subList(0, count);
            }

            return allBooks;

        } catch (Exception e) {
            System.out.println("=== 사서추천 API 에러 ===");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * 이달의 사서 추천 도서
     */
    public List<RecommendedBookDto> getMonthlyRecommendedBooks(int count) {
        try {
            LocalDate now = LocalDate.now();
            LocalDate firstDay = now.withDayOfMonth(1);
            LocalDate lastDay = now.withDayOfMonth(now.lengthOfMonth());
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String startDate = firstDay.format(formatter);
            String endDate = lastDay.format(formatter);
            
            String url = UriComponentsBuilder.fromUriString(RECOMMEND_API_URL)
                    .queryParam("key", apiKey)
                    .queryParam("startRowNumApi", "1")
                    .queryParam("endRowNumApi", String.valueOf(count))
                    .queryParam("start_date", startDate)
                    .queryParam("end_date", endDate)
                    .build()
                    .encode()
                    .toUriString();
            
            String response = restTemplate.getForObject(url, String.class);
            return parseRecommendXmlResponse(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * 분류별 사서 추천 도서
     */
    public List<RecommendedBookDto> getRecommendedBooksByCategory(String categoryCode, int count) {
        try {
            String url = UriComponentsBuilder.fromUriString(RECOMMEND_API_URL)
                    .queryParam("key", apiKey)
                    .queryParam("startRowNumApi", "1")
                    .queryParam("endRowNumApi", String.valueOf(count))
                    .queryParam("drcode", categoryCode)
                    .build()
                    .encode()
                    .toUriString();
            
            String response = restTemplate.getForObject(url, String.class);
            return parseRecommendXmlResponse(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * 사서추천도서 XML 응답 파싱
     */
    private List<RecommendedBookDto> parseRecommendXmlResponse(String xmlResponse) {
        List<RecommendedBookDto> books = new ArrayList<>();
        
        try {
            System.out.println("=== XML 파싱 시작 ===");
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8)));
            
            doc.getDocumentElement().normalize();
            
            NodeList totalCountNodes = doc.getElementsByTagName("totalCount");
            if (totalCountNodes.getLength() > 0) {
                String totalCount = totalCountNodes.item(0).getTextContent();
                System.out.println("총 추천 도서 수: " + totalCount);
            }
            
            NodeList itemList = doc.getElementsByTagName("item");
            System.out.println("item 개수: " + itemList.getLength());
            
            for (int i = 0; i < itemList.getLength(); i++) {
                Element item = (Element) itemList.item(i);
                
                RecommendedBookDto book = new RecommendedBookDto();
                
                // 올바른 태그 이름 사용
                String title = getTagValue("recomtitle", item);
                String author = getTagValue("recomauthor", item);
                String publisher = getTagValue("recompublisher", item);
                String isbn = getTagValue("recomisbn", item);
                
                System.out.println("추천 책 " + (i+1) + ": " + title);
                
                book.setTitle(title);
                book.setAuthor(author);
                book.setPublisher(publisher);
                book.setIsbn(isbn);
                book.setCoverUrl(getTagValue("recomfilepath", item));
                book.setContents(getTagValue("recomcontens", item));
                book.setCategory(getTagValue("drCodeName", item));
                book.setCategoryCode(getTagValue("drCode", item));
                
                String publishYear = getTagValue("publishYear", item);
                if (publishYear != null && !publishYear.isEmpty()) {
                    try {
                        book.setPublishYear(Integer.parseInt(publishYear));
                    } catch (NumberFormatException e) {
                        book.setPublishYear(0);
                    }
                }
                
                if (book.getCoverUrl() == null || book.getCoverUrl().isEmpty()) {
                    book.setCoverUrl(null);
                }
                
                books.add(book);
            }
            
            System.out.println("=== 파싱 완료: " + books.size() + "권 ===");
            
        } catch (Exception e) {
            System.out.println("=== XML 파싱 에러 ===");
            e.printStackTrace();
        }
        
        return books;
    }
    
    /**
     * XML 태그 값 가져오기 헬퍼 메서드
     */
    private String getTagValue(String tag, Element element) {
        try {
            NodeList nodeList = element.getElementsByTagName(tag);
            if (nodeList.getLength() > 0) {
                String value = nodeList.item(0).getTextContent();
                return value != null && !value.trim().isEmpty() ? value : "";
            }
        } catch (Exception e) {
            // 태그 없음
        }
        return "";
    }
}