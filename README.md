# Hospital Billing Backend

병원 청구 관리 백엔드 프로젝트입니다.

## 기술 스택

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database (개발용)
- Gradle 8.5

## 프로젝트 구조

```
hospital-billing-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/hospital/billing/
│   │   │       ├── HospitalBillingApplication.java
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── entity/
│   │   │       ├── dto/
│   │   │       └── config/
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/
│           └── com/hospital/billing/
├── build.gradle
├── settings.gradle
└── README.md
```

## 실행 방법

### Gradle Wrapper 사용
```bash
./gradlew bootRun
```

### Windows
```bash
gradlew.bat bootRun
```

### 빌드
```bash
./gradlew build
```

## IntelliJ에서 열기

1. IntelliJ IDEA 실행
2. `File` → `Open`
3. `C:\dve\hospital\hospital-billing-backend` 선택
4. Gradle 프로젝트로 자동 인식되어 의존성 다운로드 시작
