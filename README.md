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

### 계층별 책임

#### 1. Presentation Layer
**위치**: `{domain}.presentation.controller`

- **주요 책임**
    - HTTP 요청/응답 처리
    - 요청 데이터 검증 및 변환
    - 응답 데이터 포맷팅
    - API 문서화 (Swagger)

- **의존성**: Application Layer → Domain Layer

#### 2. Application Layer
**위치**: `{domain}.application.service`

- **주요 책임**
    - 비즈니스 유스케이스 구현
    - 도메인 객체 간 협력 조율
    - 트랜잭션 관리
    - 외부 서비스와의 통합

- **의존성**: Domain Layer만 참조

#### 3. Domain Layer
**위치**: `{domain}.domain`

- **Entity** (`{domain}.domain.entity`)
    - 핵심 비즈니스 규칙 및 상태 관리
    - 도메인 불변성 보장
    - 자체 검증 로직 포함

- **Repository Interface** (`{domain}.domain.repository`)
    - 데이터 저장소 추상화
    - 도메인 요구사항에 맞는 인터페이스 정의

- **의존성**: 다른 계층에 의존하지 않음 (순수한 비즈니스 로직)

### 아키텍처 원칙

1. **의존성 역전**: 상위 계층이 하위 계층을 의존하며, 인터페이스를 통한 추상화
2. **도메인 격리**: 각 도메인은 독립적으로 개발 및 배포 가능
3. **단일 책임**: 각 계층은 명확한 책임과 역할을 가짐
4. **테스트 용이성**: 계층별 독립적인 단위 테스트 가능

## Getting Started

### Prerequisites

#### Running Docker Containers

`local` profile 로 실행하기 위하여 인프라가 설정되어 있는 Docker 컨테이너를 실행해주셔야 합니다.

```bash
docker-compose up -d
```


