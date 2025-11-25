# BookLog

> 나만의 독서 기록과 AI 기반 책 추천을 한 곳에서

Spring Boot 기반의 개인 독서 관리 웹 애플리케이션입니다. 읽은 책을 기록하고, 다양한 추천 시스템을 통해 새로운 책을 발견하세요.

## 주요 기능

### 독서 기록 관리
- **읽은 책 / 읽고 있는 책 / 읽고 싶은 책** 상태별 관리
- 평점(1-5점) 및 리뷰 작성
- 책 검색을 통한 간편 등록 (Kakao Book API 연동)
- 다른 사용자의 독서 기록 열람

### 4가지 추천 시스템

#### 1. 국립중앙도서관 사서 추천
전문 사서가 선정한 양질의 도서 추천

#### 2️. 베스트셀러
알라딘 API 기반 실시간 베스트셀러 도서

#### 3️. 신간 도서
알라딘 API 기반 최신 출간 도서

#### 4️. AI 개인화 추천 
- **개인화 모드**: 독서 기록 3권 이상 시, 취향 분석 기반 맞춤 추천
- **트렌드 모드**: 독서 기록 부족 시, 인기 베스트셀러 추천
- 버튼 클릭 방식으로 빠른 페이지 로딩 보장

### 독서 커뮤니티
- 자유게시판
- 책 리뷰
- 책 추천 요청
- 독서 모임

## 실행 화면

|랜딩 페이지|회원가입 및 로그인|
|:---:|:---:|
|![Image](https://github.com/user-attachments/assets/4becfdc3-10e7-462d-b6db-58ff00e0c947)|![Image](https://github.com/user-attachments/assets/44760454-1c4d-4463-8a6b-4d7f56647776)|
|메인 페이지|책 검색 및 읽은 책 등록|
|![Image](https://github.com/user-attachments/assets/b5ec99f8-dc88-4589-8f1d-399667c9c785)|![Image](https://github.com/user-attachments/assets/cbebce97-9082-421a-9454-79a242ffefed)|
|추천 도서에서 책 등록|읽고 싶은 책, 읽고 있는 책 등록|
|![Image](https://github.com/user-attachments/assets/db365007-8be9-4ae2-a20e-0fbb5eb097ee)|![Image](https://github.com/user-attachments/assets/7ff37180-4f4d-4988-86c9-3bfbff3a76a4)|
|AI 추천|마이 페이지|
|![Image](https://github.com/user-attachments/assets/438cfb6e-123d-4c74-b046-db3c6badfef6)|![Image](https://github.com/user-attachments/assets/cce10fe0-891d-4033-b58d-3f7f68c5308f)|
|커뮤니티|
|![Image](https://github.com/user-attachments/assets/3f995764-75ff-49d2-8040-3bf89b609c10)|



## 기술 스택

### Backend
- **Spring Boot** 3.5.7
- **Spring Data JPA** - 데이터 영속성
- **Spring Security** - 인증/인가
- **MySQL** 

### Frontend
- **Thymeleaf** - 서버 사이드 템플릿 엔진
- **HTML/CSS/JavaScript** - 반응형 디자인
- **AJAX** - 비동기 데이터 로딩

### External APIs
- **OpenAI GPT-3.5-turbo** - AI 책 추천
- **국립중앙도서관 API** - 사서 추천 도서
- **알라딘 API** - 베스트셀러 및 신간 도서
- **Kakao Book API** - 책 검색

## 시작하기

### 사전 요구사항
- Java 17 이상
- Maven 3.6 이상

### 설치 및 실행

1. **프로젝트 클론**
   ```bash
   git clone https://github.com/zada8/booklog.git
   cd booklog
   ```

2. **API 키 설정**

   `booklog/src/main/resources/application.properties.example` 파일을 복사하여 `application.properties` 생성:
   ```bash
   cp booklog/src/main/resources/application.properties.example booklog/src/main/resources/application.properties
   ```

   다음 API 키들을 설정하세요:
   ```properties
   # Kakao Book API
   kakao.api.key=your-kakao-api-key

   # Aladin API
   aladin.api.key=your-aladin-api-key

   # OpenAI API (선택 사항)
   openai.api.key=your-openai-api-key
   ```

3. **애플리케이션 실행**
   ```bash
   cd booklog
   ./mvnw spring-boot:run
   ```

4. **브라우저에서 접속**
   ```
   http://localhost:8080
   ```

## 프로젝트 구조

```
booklog/
├── src/
│   ├── main/
│   │   ├── java/com/example/booklog/
│   │   │   ├── controller/       # MVC 컨트롤러
│   │   │   │   ├── BookController.java
│   │   │   │   └── UserController.java
│   │   │   ├── entity/            # JPA 엔티티 및 DTO
│   │   │   │   ├── Book.java
│   │   │   │   ├── User.java
│   │   │   │   ├── AiRecommendedBookDto.java
│   │   │   │   └── AladinBookDto.java
│   │   │   ├── repository/        # JPA 리포지토리
│   │   │   ├── service/           # 비즈니스 로직
│   │   │   │   ├── BookService.java
│   │   │   │   ├── AiRecommendationService.java
│   │   │   │   ├── AladinApiService.java
│   │   │   │   └── NationalLibraryApiService.java
│   │   │   └── security/          # Spring Security 설정
│   │   └── resources/
│   │       ├── templates/         # Thymeleaf 템플릿
│   │       │   ├── books/
│   │       │   └── fragments/
│   │       ├── static/            # 정적 리소스
│   │       └── application.properties
│   └── test/                      # 테스트 코드
└── pom.xml                        # Maven 의존성
```

##  주요 기능 상세

### AI 추천 시스템

```java
// 3권 이상 등록 시
if (userBooks.size() >= 3) {
    // 사용자 독서 취향 분석
    return getPersonalizedRecommendations(user);
} else {
    // 트렌드 기반 추천
    return getTrendingRecommendations();
}
```

**개인화 추천 예시**:
- 사용자가 주로 읽은 장르와 저자 분석
- 비슷한 테마와 스타일의 책 추천
- AI가 추천 이유를 함께 제공

**트렌드 추천 예시**:
- 현재 인기있는 베스트셀러 추천
- 최신 트렌드 반영

### 성능 최적화
- **지연 로딩**: AI 추천은 버튼 클릭 시에만 API 호출
- **병렬 처리**: 사서 추천, 베스트셀러, 신간 도서 동시 로드
- **예외 처리**: API 오류 시에도 서비스 중단 없음

## 보안

- Spring Security 기반 인증/인가
- 비밀번호 BCrypt 암호화
- CSRF 보호




## 개발자

- GitHub: [@zada8](https://github.com/zada8)
- Email: qqaz0609@naver.com


