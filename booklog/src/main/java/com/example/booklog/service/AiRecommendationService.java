package com.example.booklog.service;

import com.example.booklog.entity.AiRecommendedBookDto;
import com.example.booklog.entity.Book;
import com.example.booklog.repository.BookRepository;
import com.example.booklog.entity.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 기반 책 추천 서비스
 * 하이브리드 방식: 사용자 독서 데이터가 있으면 개인화 추천, 없으면 트렌드 추천
 */
@Service
public class AiRecommendationService {

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final int MIN_BOOKS_FOR_PERSONALIZED = 3; // 개인화 추천에 필요한 최소 책 개수

    private final BookRepository bookRepository;
    private final RestTemplate restTemplate;

    public AiRecommendationService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * 하이브리드 책 추천
     * 사용자의 독서 기록을 분석하여 개인화 또는 트렌드 추천 제공
     */
    public List<AiRecommendedBookDto> getRecommendations(User user, int count) {
        try {
            // API 키 확인
            if (openaiApiKey == null || openaiApiKey.trim().isEmpty()) {
                System.out.println("⚠️ OpenAI API 키가 설정되지 않았습니다. application.properties에 openai.api.key를 추가하세요.");
                return new ArrayList<>();
            }

            // 사용자의 독서 데이터 가져오기
            List<Book> userBooks = bookRepository.findByUserOrderByCreatedAtDesc(user);

            String prompt;
            if (userBooks.size() >= MIN_BOOKS_FOR_PERSONALIZED) {
                // 개인화 추천
                System.out.println("📚 개인화 추천 생성 중... (독서 기록: " + userBooks.size() + "권)");
                prompt = buildPersonalizedPrompt(userBooks, count);
            } else {
                // 트렌드 추천
                System.out.println("🌟 트렌드 추천 생성 중... (독서 기록 부족)");
                prompt = buildTrendPrompt(count);
            }

            // OpenAI API 호출
            List<AiRecommendedBookDto> recommendations = callOpenAiApi(prompt, count);

            System.out.println("✅ AI 추천 완료: " + recommendations.size() + "권");
            return recommendations;

        } catch (Exception e) {
            System.out.println("❌ AI 추천 에러: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 개인화 추천을 위한 프롬프트 생성
     */
    private String buildPersonalizedPrompt(List<Book> userBooks, int count) {
        // 사용자의 독서 패턴 분석
        Map<String, Long> genreCounts = userBooks.stream()
                .filter(b -> b.getGenre() != null && !b.getGenre().isEmpty())
                .collect(Collectors.groupingBy(Book::getGenre, Collectors.counting()));

        Map<String, Long> authorCounts = userBooks.stream()
                .filter(b -> b.getAuthor() != null && !b.getAuthor().isEmpty())
                .collect(Collectors.groupingBy(Book::getAuthor, Collectors.counting()));

        // 최근 읽은 책 (최대 5권)
        List<Book> recentBooks = userBooks.stream()
                .filter(b -> "READ".equals(b.getStatus()))
                .limit(5)
                .collect(Collectors.toList());

        // 평점 높은 책 (최대 5권)
        List<Book> highRatedBooks = userBooks.stream()
                .filter(b -> b.getRating() != null && b.getRating() >= 4)
                .sorted((b1, b2) -> Integer.compare(b2.getRating(), b1.getRating()))
                .limit(5)
                .collect(Collectors.toList());

        // 프롬프트 작성
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 전문 책 추천 AI입니다. 다음 사용자의 독서 기록을 분석하여 맞춤형 책을 추천해주세요.\n\n");

        prompt.append("【독서 통계】\n");
        prompt.append("- 총 등록 책: ").append(userBooks.size()).append("권\n");

        if (!genreCounts.isEmpty()) {
            prompt.append("- 선호 장르: ");
            genreCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(3)
                    .forEach(entry -> prompt.append(entry.getKey()).append("(").append(entry.getValue()).append("권) "));
            prompt.append("\n");
        }

        if (!recentBooks.isEmpty()) {
            prompt.append("\n【최근 읽은 책】\n");
            recentBooks.forEach(book -> {
                prompt.append("- 『").append(book.getTitle()).append("』");
                if (book.getAuthor() != null) prompt.append(" - ").append(book.getAuthor());
                if (book.getRating() != null) prompt.append(" (평점: ").append(book.getRating()).append("/5)");
                prompt.append("\n");
            });
        }

        if (!highRatedBooks.isEmpty()) {
            prompt.append("\n【높은 평점을 준 책】\n");
            highRatedBooks.forEach(book -> {
                prompt.append("- 『").append(book.getTitle()).append("』");
                if (book.getAuthor() != null) prompt.append(" - ").append(book.getAuthor());
                prompt.append(" (평점: ").append(book.getRating()).append("/5)\n");
            });
        }

        prompt.append("\n【요청사항】\n");
        prompt.append("위 독서 패턴을 바탕으로 이 사용자가 좋아할만한 한국 도서 ").append(count).append("권을 추천해주세요.\n");
        prompt.append("각 책에 대해 JSON 형식으로 다음 정보를 제공해주세요:\n");
        prompt.append("{\n");
        prompt.append("  \"recommendations\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"title\": \"책 제목\",\n");
        prompt.append("      \"author\": \"저자명\",\n");
        prompt.append("      \"publisher\": \"출판사\",\n");
        prompt.append("      \"category\": \"장르\",\n");
        prompt.append("      \"description\": \"책 소개 (2-3문장)\",\n");
        prompt.append("      \"reason\": \"이 사용자에게 추천하는 이유 (1-2문장)\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        prompt.append("\n실제로 출판된 한국 도서만 추천해주세요. JSON 형식만 출력하고 다른 설명은 불필요합니다.");

        return prompt.toString();
    }

    /**
     * 트렌드 추천을 위한 프롬프트 생성
     */
    private String buildTrendPrompt(int count) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 전문 책 추천 AI입니다.\n\n");
        prompt.append("【요청사항】\n");
        prompt.append("2024-2025년 한국에서 인기 있는 베스트셀러 도서 중 평점이 높고 폭넓게 사랑받는 책 ");
        prompt.append(count).append("권을 추천해주세요.\n");
        prompt.append("소설, 에세이, 자기계발, 인문 등 다양한 장르를 골고루 포함해주세요.\n\n");
        prompt.append("각 책에 대해 JSON 형식으로 다음 정보를 제공해주세요:\n");
        prompt.append("{\n");
        prompt.append("  \"recommendations\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"title\": \"책 제목\",\n");
        prompt.append("      \"author\": \"저자명\",\n");
        prompt.append("      \"publisher\": \"출판사\",\n");
        prompt.append("      \"category\": \"장르\",\n");
        prompt.append("      \"description\": \"책 소개 (2-3문장)\",\n");
        prompt.append("      \"reason\": \"많은 독자들에게 사랑받는 이유 (1-2문장)\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        prompt.append("\n실제로 출판된 한국 도서만 추천해주세요. JSON 형식만 출력하고 다른 설명은 불필요합니다.");

        return prompt.toString();
    }

    /**
     * OpenAI API 호출
     */
    private List<AiRecommendedBookDto> callOpenAiApi(String prompt, int count) {
        try {
            // 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            // 요청 본문 설정
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("temperature", 0.7);

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            messages.put(message);
            requestBody.put("messages", messages);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            // API 호출
            System.out.println("🤖 OpenAI API 호출 중...");
            ResponseEntity<String> response = restTemplate.exchange(
                    OPENAI_API_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 응답 파싱
            return parseOpenAiResponse(response.getBody());

        } catch (Exception e) {
            System.out.println("❌ OpenAI API 호출 실패: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * OpenAI API 응답 파싱
     */
    private List<AiRecommendedBookDto> parseOpenAiResponse(String responseBody) {
        List<AiRecommendedBookDto> books = new ArrayList<>();

        try {
            JSONObject response = new JSONObject(responseBody);
            JSONArray choices = response.getJSONArray("choices");

            if (choices.length() > 0) {
                JSONObject firstChoice = choices.getJSONObject(0);
                String content = firstChoice.getJSONObject("message").getString("content");

                // JSON 추출 (마크다운 코드 블록 제거)
                content = content.trim();
                if (content.startsWith("```json")) {
                    content = content.substring(7);
                }
                if (content.startsWith("```")) {
                    content = content.substring(3);
                }
                if (content.endsWith("```")) {
                    content = content.substring(0, content.length() - 3);
                }
                content = content.trim();

                // JSON 파싱
                JSONObject jsonContent = new JSONObject(content);
                JSONArray recommendations = jsonContent.getJSONArray("recommendations");

                for (int i = 0; i < recommendations.length(); i++) {
                    JSONObject bookJson = recommendations.getJSONObject(i);

                    AiRecommendedBookDto book = new AiRecommendedBookDto();
                    book.setTitle(bookJson.optString("title", ""));
                    book.setAuthor(bookJson.optString("author", ""));
                    book.setPublisher(bookJson.optString("publisher", ""));
                    book.setCategory(bookJson.optString("category", ""));
                    book.setDescription(bookJson.optString("description", ""));
                    book.setReason(bookJson.optString("reason", ""));

                    books.add(book);
                    System.out.println("✓ 추천: " + book.getTitle() + " - " + book.getAuthor());
                }
            }

        } catch (Exception e) {
            System.out.println("❌ JSON 파싱 실패: " + e.getMessage());
            e.printStackTrace();
        }

        return books;
    }

    /**
     * 사용자 독서 데이터 분석 요약 (디버깅/로깅용)
     */
    public Map<String, Object> analyzeUserReadingPattern(User user) {
        List<Book> userBooks = bookRepository.findByUserOrderByCreatedAtDesc(user);

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("totalBooks", userBooks.size());
        analysis.put("canPersonalize", userBooks.size() >= MIN_BOOKS_FOR_PERSONALIZED);

        // 장르 분포
        Map<String, Long> genreCounts = userBooks.stream()
                .filter(b -> b.getGenre() != null && !b.getGenre().isEmpty())
                .collect(Collectors.groupingBy(Book::getGenre, Collectors.counting()));
        analysis.put("genreDistribution", genreCounts);

        // 평균 평점
        double avgRating = userBooks.stream()
                .filter(b -> b.getRating() != null)
                .mapToInt(Book::getRating)
                .average()
                .orElse(0.0);
        analysis.put("averageRating", avgRating);

        return analysis;
    }
}
