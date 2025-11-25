package com.example.booklog.service;

import com.example.booklog.entity.AladinBookDto;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AladinApiService {

    @Value("${aladin.api.key}")
    private String apiKey;

    private static final String API_URL = "http://www.aladin.co.kr/ttb/api/ItemList.aspx";

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 베스트셀러 가져오기 (날짜별 랜덤)
     */
    public List<AladinBookDto> getBestsellers(int count) {
        return getBookList("Bestseller", count);
    }

    /**
     * 신간 도서 가져오기 (날짜별 랜덤)
     */
    public List<AladinBookDto> getNewBooks(int count) {
        return getBookList("ItemNewAll", count);
    }

    /**
     * 알라딘 API 호출 (공통)
     */
    private List<AladinBookDto> getBookList(String queryType, int count) {
        try {
            // API에서 더 많은 책을 가져와서 랜덤으로 선택
            int fetchCount = Math.max(count * 10, 50); // 최소 50개 또는 요청 수의 10배

            String url = UriComponentsBuilder.fromUriString(API_URL)
                    .queryParam("ttbkey", apiKey)
                    .queryParam("QueryType", queryType)
                    .queryParam("MaxResults", String.valueOf(fetchCount))
                    .queryParam("start", "1")
                    .queryParam("SearchTarget", "Book")
                    .queryParam("output", "js")  // JSON 형식
                    .queryParam("Version", "20131101")
                    .build()
                    .encode()
                    .toUriString();

            System.out.println("=== 알라딘 API 요청 (" + queryType + ") ===");
            System.out.println("URL: " + url);

            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.trim().isEmpty()) {
                System.out.println("알라딘 API 응답 없음");
                return new ArrayList<>();
            }

            List<AladinBookDto> allBooks = parseResponse(response);

            // 날짜 기반 시드로 랜덤 섞기 (매일 바뀜, 하루 동안은 고정)
            if (allBooks.size() > count) {
                long seed = LocalDate.now().toEpochDay(); // 오늘 날짜를 시드로 사용
                Collections.shuffle(allBooks, new java.util.Random(seed));
                return allBooks.subList(0, count);
            }

            return allBooks;

        } catch (Exception e) {
            System.out.println("=== 알라딘 API 에러 ===");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * JSON 응답 파싱
     */
    private List<AladinBookDto> parseResponse(String jsonResponse) {
        List<AladinBookDto> books = new ArrayList<>();

        try {
            JSONObject json = new JSONObject(jsonResponse);

            if (!json.has("item")) {
                System.out.println("item 배열 없음");
                return books;
            }

            JSONArray items = json.getJSONArray("item");
            System.out.println("검색 결과: " + items.length() + "건");

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);

                AladinBookDto book = new AladinBookDto();

                // 제목
                book.setTitle(item.optString("title", ""));

                // 저자
                book.setAuthor(item.optString("author", ""));

                // 출판사
                book.setPublisher(item.optString("publisher", ""));

                // ISBN13
                book.setIsbn(item.optString("isbn13", ""));

                // 표지 이미지
                book.setCoverUrl(item.optString("cover", ""));

                // 출판일
                book.setPubDate(item.optString("pubDate", ""));

                // 설명
                book.setDescription(item.optString("description", ""));

                // 카테고리
                book.setCategoryName(item.optString("categoryName", ""));

                // 가격
                book.setPriceStandard(item.optInt("priceStandard", 0));
                book.setPriceSales(item.optInt("priceSales", 0));

                // 링크
                book.setLink(item.optString("link", ""));

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
