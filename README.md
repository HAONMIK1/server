## 프로젝트

## 아키텍처

이 프로젝트는 **도메인 중심 계층형 아키텍처(Domain-Centric Layered Architecture)**를 적용했습니다.

### 패키지 구조
```
kr.hhplus.be.server.{domain}
├── presentation/     # 표현 계층
├── application/      # 애플리케이션 계층  
├── domain/           # 도메인 계층
└── infrastructure/   # 인프라 계층
```


---

### 계층별 책임

#### 1. Presentation Layer
**위치**: `{domain}.presentation.controller`

- **책임**
  - HTTP 요청 수신 및 응답 반환
  - 요청 데이터 검증 및 변환
  - 응답 데이터 포맷 정의
  - API 문서화 (Swagger 등)

- **의존성**: Application Layer → Domain Layer

---

#### 2. Application Layer
**위치**: `{domain}.application.service`

- **책임**
  - 도메인 객체 협력을 통한 비즈니스 유스케이스 구현
  - 트랜잭션 제어
  - 외부 서비스/API 호출 조정

- **의존성**: Domain Layer만 참조

---

#### 3. Domain Layer
**위치**: `{domain}.domain`

- **Entity** (`{domain}.domain.entity`)
  - 핵심 비즈니스 규칙 구현
  - 도메인 상태 및 불변성 관리
  - 자체 유효성 검증 로직 포함

- **Repository Interface** (`{domain}.domain.repository`)
  - 영속성 계층 추상화
  - 도메인 요구사항에 맞는 인터페이스 정의

- **의존성**: 외부 계층에 전혀 의존하지 않음  
  *(순수한 비즈니스 계층으로서 독립 유지)*

---

#### 4. Infrastructure Layer
**위치**: `{domain}.infrastructure`

- **책임**
  - 도메인에서 정의한 Repository 인터페이스의 구현체 제공 (예: JPA, MyBatis)
  - 외부 시스템과의 통신 (예: DB, Redis, 외부 API)
  - 기술적 세부 사항 캡슐화 (예: 메시지 큐, 스케줄러, 설정 등)

- **의존성**
  - Domain Layer에 정의된 인터페이스를 구현
  - 하위 계층으로만 의존 (순환 참조 없음)

---

### 아키텍처 원칙

1. **의존성 역전**: 상위 계층은 하위 계층의 구체 구현이 아닌 추상(인터페이스)에 의존합니다.
2. **도메인 격리**: 각 도메인은 독립적으로 개발 및 배포가 가능합니다.
3. **단일 책임 원칙**: 각 계층은 하나의 명확한 책임만 가집니다.
4. **테스트 용이성**: 계층별로 독립적인 단위 테스트가 가능합니다.

---

## Getting Started

### Prerequisites

#### Run Docker Containers

`local` 프로파일로 실행하기 위해서는 인프라 구성이 완료된 Docker 컨테이너를 먼저 실행해야 합니다.

```bash
docker-compose up -d
