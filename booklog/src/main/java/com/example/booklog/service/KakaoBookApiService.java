package com.example.booklog.service;

import com.example.booklog.entity.BookApiDto;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class KakaoBookApiService {
    
    @Value("${kakao.api.url}")
    private String apiUrl;
    
    @Value("${kakao.api.key}")
    private String apiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 책 검색 (제목, 저자, 출판사 통합 검색)
     */
    public List<BookApiDto> search(String query) {
        try {
            System.out.println("=== 카카오 책 검색 시작 ===");
            System.out.println("검색어: " + query);
            
            // API 키 확인
            System.out.println("API URL: " + apiUrl);
            System.out.println("API KEY 길이: " + (apiKey != null ? apiKey.length() : "null"));
            System.out.println("API KEY 앞 10자: " + (apiKey != null && apiKey.length() >= 10 ? apiKey.substring(0, 10) + "..." : apiKey));
            
            
            // API 요청 URL
            String url = apiUrl + "?query=" + query + "&size=50";
            
            System.out.println("요청 URL: " + url);
            
            // RestTemplate으로 API 호출
            RestTemplate template = new RestTemplate();
            template.getInterceptors().add((request, body, execution) -> {
                request.getHeaders().set("Authorization", "KakaoAK " + apiKey);
                return execution.execute(request, body);
            });
            
            String response = template.getForObject(url, String.class);
            
            System.out.println("=== 응답 받음 ===");
            // 응답 내용 출력 (처음 500자)
            System.out.println("응답 내용: " + (response != null ? response.substring(0, Math.min(500, response.length())) : "null"));
            
            return parseResponse(response);
            
        } catch (Exception e) {
            System.out.println("=== 카카오 API 에러 ===");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * ISBN으로 책 검색
     */
    public BookApiDto getBookByIsbn(String isbn) {
        try {
            String url = apiUrl + "?query=" + isbn + "&target=isbn";
            
            RestTemplate template = new RestTemplate();
            template.getInterceptors().add((request, body, execution) -> {
                request.getHeaders().set("Authorization", "KakaoAK " + apiKey);
                return execution.execute(request, body);
            });
            
            String response = template.getForObject(url, String.class);
            List<BookApiDto> books = parseResponse(response);
            
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
            JSONObject json = new JSONObject(jsonResponse);
            JSONArray documents = json.getJSONArray("documents");
            
            System.out.println("검색 결과: " + documents.length() + "건");
            
            for (int i = 0; i < documents.length(); i++) {
                JSONObject doc = documents.getJSONObject(i);
                
                BookApiDto book = new BookApiDto();
                
                // 제목
                book.setTitle(doc.optString("title", ""));
                
                // 저자 (배열을 문자열로 변환)
                JSONArray authors = doc.optJSONArray("authors");
                if (authors != null && authors.length() > 0) {
                    List<String> authorList = new ArrayList<>();
                    for (int j = 0; j < authors.length(); j++) {
                        authorList.add(authors.getString(j));
                    }
                    book.setAuthor(String.join(", ", authorList));
                }
                
                // 출판사
                book.setPublisher(doc.optString("publisher", ""));
                
                // ISBN
                book.setIsbn(doc.optString("isbn", "").split(" ")[0]); // 첫 번째 ISBN 사용
                
                // 표지 이미지
                String thumbnail = doc.optString("thumbnail", "");
                if (!thumbnail.isEmpty()) {
                    book.setCoverUrl(thumbnail);
                }
                
                // 출판일
                String datetime = doc.optString("datetime", "");
                if (!datetime.isEmpty()) {
                    book.setPublishDate(datetime.substring(0, 10)); // 날짜 부분만
                }
                
                // 가격
                book.setPage(doc.optString("price", ""));
                
                // 소개
                String contents = doc.optString("contents", "");
                if (!contents.isEmpty()) {
                    book.setDescription(contents.substring(0, Math.min(200, contents.length())) + "...");
                }
                
                System.out.println("책 파싱: " + book.getTitle());
                books.add(book);
            }
            
        } catch (Exception e) {
            System.out.println("=== JSON 파싱 에러 ===");
            e.printStackTrace();
        }
        
        return books;
    }
}