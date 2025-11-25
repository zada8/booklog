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

                // 장르 추측 (카카오 API에 카테고리 정보가 없으므로 제목/내용으로 추측)
                String genre = inferGenreFromTitleAndContent(book.getTitle(), contents);
                book.setSubject(genre);

                System.out.println("책 파싱: " + book.getTitle() + " / 장르: " + genre);
                books.add(book);
            }
            
        } catch (Exception e) {
            System.out.println("=== JSON 파싱 에러 ===");
            e.printStackTrace();
        }
        
        return books;
    }

    /**
     * 제목과 내용으로 장르 추측
     */
    private String inferGenreFromTitleAndContent(String title, String content) {
        String text = (title + " " + content).toLowerCase();

        // 소설 관련 키워드
        if (text.matches(".*(소설|이야기|장편|단편|novel).*")) {
            return "소설";
        }

        // 에세이 관련 키워드
        if (text.matches(".*(에세이|수필|산문|일상|기록).*")) {
            return "에세이";
        }

        // 자기계발 관련 키워드
        if (text.matches(".*(자기계발|성공|습관|동기부여|자존감|행복|마음|심리|치유).*")) {
            return "자기계발";
        }

        // 경제/경영 관련 키워드
        if (text.matches(".*(경제|경영|투자|재테크|마케팅|비즈니스|창업|부자).*")) {
            return "경제/경영";
        }

        // IT/컴퓨터 관련 키워드
        if (text.matches(".*(프로그래밍|코딩|개발|java|python|javascript|컴퓨터|알고리즘|데이터).*")) {
            return "IT/컴퓨터";
        }

        // 역사 관련 키워드
        if (text.matches(".*(역사|문화|전쟁|세계사|한국사|조선|고려).*")) {
            return "역사";
        }

        // 과학 관련 키워드
        if (text.matches(".*(과학|물리|화학|생물|수학|우주|진화).*")) {
            return "과학";
        }

        // 인문 관련 키워드
        if (text.matches(".*(철학|인문|사상|사회|교양|예술).*")) {
            return "인문";
        }

        // 시 관련 키워드
        if (text.matches(".*(시집|시|poetry|poem).*") && !text.contains("역사") && !text.contains("시대")) {
            return "시";
        }

        // 기본값
        return "기타";
    }

    /**
     * 카테고리/주제분류를 장르로 매핑
     * 카테고리 형식:
     * - 카카오: "국내도서>소설>한국소설"
     * - 국립도서관: "813.7" (KDC), "문학", "소설" 등
     */
    public String mapCategoryToGenre(String category) {
        if (category == null || category.isEmpty()) {
            return "기타";
        }

        String lowerCategory = category.toLowerCase();

        // KDC(한국십진분류법) 숫자 분류 처리
        if (category.matches("^\\d+.*")) {
            String kdcCode = category.substring(0, Math.min(3, category.length()));

            // 800번대: 문학
            if (kdcCode.startsWith("8")) {
                // 810-820: 한국문학 (소설)
                if (kdcCode.startsWith("81")) {
                    return "소설";
                }
                // 840: 영미문학
                if (kdcCode.startsWith("84")) {
                    return "소설";
                }
                // 800번대 전체는 일단 소설로
                return "소설";
            }

            // 100번대: 철학
            if (kdcCode.startsWith("1")) {
                return "인문";
            }

            // 300번대: 사회과학
            if (kdcCode.startsWith("3")) {
                // 320: 경제
                if (kdcCode.startsWith("32")) {
                    return "경제/경영";
                }
                return "인문";
            }

            // 400번대: 자연과학
            if (kdcCode.startsWith("4")) {
                return "과학";
            }

            // 500번대: 기술과학 (컴퓨터 포함)
            if (kdcCode.startsWith("5")) {
                return "IT/컴퓨터";
            }

            // 900번대: 역사
            if (kdcCode.startsWith("9")) {
                return "역사";
            }
        }

        // 텍스트 기반 분류
        // 소설
        if (lowerCategory.contains("소설") || lowerCategory.contains("문학")) {
            return "소설";
        }

        // 에세이
        if (lowerCategory.contains("에세이") || lowerCategory.contains("수필")) {
            return "에세이";
        }

        // 시
        if (lowerCategory.contains("시") && !lowerCategory.contains("역사") && !lowerCategory.contains("시대")) {
            return "시";
        }

        // 자기계발
        if (lowerCategory.contains("자기계발") || lowerCategory.contains("자기관리") ||
            lowerCategory.contains("성공") || lowerCategory.contains("동기부여") ||
            lowerCategory.contains("자기개발")) {
            return "자기계발";
        }

        // 경제/경영
        if (lowerCategory.contains("경제") || lowerCategory.contains("경영") ||
            lowerCategory.contains("재테크") || lowerCategory.contains("투자") ||
            lowerCategory.contains("마케팅") || lowerCategory.contains("비즈니스")) {
            return "경제/경영";
        }

        // 인문
        if (lowerCategory.contains("인문") || lowerCategory.contains("철학") ||
            lowerCategory.contains("심리") || lowerCategory.contains("사회") ||
            lowerCategory.contains("교양")) {
            return "인문";
        }

        // 역사
        if (lowerCategory.contains("역사") || lowerCategory.contains("문화")) {
            return "역사";
        }

        // 과학
        if (lowerCategory.contains("과학") || lowerCategory.contains("수학") ||
            lowerCategory.contains("물리") || lowerCategory.contains("화학") ||
            lowerCategory.contains("생물") || lowerCategory.contains("자연")) {
            return "과학";
        }

        // IT/컴퓨터
        if (lowerCategory.contains("컴퓨터") || lowerCategory.contains("프로그래밍") ||
            lowerCategory.contains("it") || lowerCategory.contains("개발") ||
            lowerCategory.contains("코딩") || lowerCategory.contains("웹") ||
            lowerCategory.contains("앱") || lowerCategory.contains("소프트웨어")) {
            return "IT/컴퓨터";
        }

        // 기타
        return "기타";
    }
}