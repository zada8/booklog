# booklog
A personal book log web application built with Spring Boot, JPA, and MySQL

## Features

- 📚 개인 독서 기록 관리 (읽은 책, 읽고 있는 책, 읽고 싶은 책)
- ⭐ 책 평점 및 리뷰 작성
- 📖 국립중앙도서관 사서 추천 도서
- 🤖 **AI 기반 개인화 책 추천** (NEW!)
- 👥 커뮤니티 게시판 (자유, 리뷰, 추천, 독서모임)
- 🔍 책 검색 (Kakao API 연동)

## AI 책 추천 기능 설정

AI 추천 기능은 **하이브리드 방식**으로 작동합니다:
- **개인화 추천**: 독서 기록이 3권 이상일 때, 사용자의 취향을 분석하여 맞춤 추천
- **트렌드 추천**: 독서 기록이 부족할 때, 인기 베스트셀러 추천

### OpenAI API 키 설정

1. **OpenAI API 키 발급**
   - https://platform.openai.com/signup 에서 계정 생성
   - https://platform.openai.com/api-keys 에서 API 키 발급

2. **application.properties 설정**
   ```properties
   # AI 추천 기능 활성화
   openai.api.key=your-openai-api-key-here
   ```

3. **API 사용 요금**
   - GPT-3.5-turbo 모델 사용
   - 추천 1회당 약 0.001~0.003 USD (약 1~4원)
   - 무료 크레딧: 신규 가입 시 $5 제공 (3개월 유효)

### API 키 없이 사용하기

API 키를 설정하지 않으면:
- AI 추천 섹션이 자동으로 숨겨집니다
- 다른 모든 기능은 정상적으로 작동합니다
- 사서 추천 도서는 계속 사용 가능합니다

## 기술 스택

- **Backend**: Spring Boot 3.5.7, Spring Data JPA, Spring Security
- **Frontend**: Thymeleaf, HTML/CSS, JavaScript
- **Database**: MySQL
- **External APIs**:
  - 국립중앙도서관 API (사서 추천)
  - Kakao Book API (책 검색)
  - OpenAI API (AI 추천)
