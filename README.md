# E-Commerce Backend Platform

> 고동시성 환경을 고려한 이커머스 백엔드 시스템  
> Spring Boot + Redis + Kafka + MySQL 기반 분산 시스템 설계 및 구현

---

## 목차

- [프로젝트 소개](#프로젝트-소개)
- [기술 스택](#기술-스택)
- [주요 기술 구현](#주요-기술-구현)
  - [1. 동시성 제어 — 선착순 쿠폰 · 재고 · 잔액](#1-동시성-제어--선착순-쿠폰--재고--잔액)
  - [2. 이벤트 기반 아키텍처 — Kafka 비동기 처리](#2-이벤트-기반-아키텍처--kafka-비동기-처리)
  - [3. Redis 다중 활용 — 캐싱 · 랭킹 · 분산락 · 큐](#3-redis-다중-활용--캐싱--랭킹--분산락--큐)
  - [4. 쿼리 성능 최적화 — 16.4s → 0.008s](#4-쿼리-성능-최적화--164s--0008s)
  - [5. 도메인 주도 설계 — 4-Layer Architecture](#5-도메인-주도-설계--4-layer-architecture)
- [시스템 아키텍처](#시스템-아키텍처)
- [API 명세](#api-명세)
- [ERD](#erd)
- [설계 문서](#설계-문서)
- [테스트 전략](#테스트-전략)
- [실행 방법](#실행-방법)

---

## 프로젝트 소개

선착순 쿠폰 발급, 상품 재고 관리, 결제 처리 등 **동시 요청이 집중되는 이커머스 핵심 시나리오**를 다루는 백엔드 플랫폼입니다.


| 도메인 | 핵심 기능 |
|--------|----------|
| Balance | 사용자 잔액 충전 · 사용 · 이력 조회 |
| Product | 상품 목록 · 재고 관리 · 인기 상품 랭킹 |
| Order | 주문 생성 · 상태 관리 |
| Payment | 결제 처리 (분산락 기반 동시성 제어) |
| Coupon | 선착순 쿠폰 발급 (Redis 큐 + Kafka) |

---

## 기술 스택

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.4.1-6DB33F?style=flat&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL_8.0-4479A1?style=flat&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis&logoColor=white)
![Kafka](https://img.shields.io/badge/Kafka-231F20?style=flat&logo=apachekafka&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)

| 분류 | 기술 |
|------|------|
| Framework | Spring Boot 3.4.1, Spring Data JPA |
| Database | MySQL 8.0 (HikariCP 커넥션 풀) |
| Cache / Lock | Redis, Redisson 3.29.0 |
| Message Queue | Apache Kafka 7.4.0 |
| Build | Gradle (Kotlin DSL) |
| Test | JUnit 5, Testcontainers, MockMvc |
| Docs | SpringDoc OpenAPI (Swagger) |

---

## 주요 기술 구현

### 1. 동시성 제어 — 선착순 쿠폰 · 재고 · 잔액

#### 문제

여러 사용자가 동시에 동일한 자원(재고, 잔액, 쿠폰)에 접근할 때 **Race Condition(경쟁 상태)** 이 발생합니다.
예를 들어 재고 1개인 상품에 100명이 동시에 주문하면, DB 조회-차감 사이 간격으로 재고가 마이너스가 됩니다.

#### 해결 전략 — 3중 동시성 제어

```
요청 진입
   ↓
[1단계] Redisson 분산락 (@DistributedLock AOP)  ← 결제 레벨 직렬화
   ↓
[2단계] DB 비관적 락 (SELECT FOR UPDATE)        ← 재고/잔액 레코드 잠금
   ↓
[3단계] Redis 원자 연산 (INCR/DECR)            ← 쿠폰 재고 원자적 처리
```

**분산락 — SpEL 기반 동적 키 생성**

```java
// 사용자+주문 조합으로 유니크한 락 키를 자동 생성
@DistributedLock(key = "'payment:' + #userId + ':' + #orderId", waitTime = 10, leaseTime = 30)
public PaymentResponse processPayment(Long userId, Long orderId, PaymentRequest request) { ... }
```

AOP로 구현하여 비즈니스 로직과 락 로직을 완전히 분리했습니다.

**시나리오별 적용 방식**

| 시나리오 | 적용 기술 | 선택 이유 |
|---------|---------|---------|
| 결제 처리 | Redisson 분산락 | 다중 도메인 조율 필요, 락 범위를 넓게 잡아야 함 |
| 상품 재고 차감 | DB 비관적 락 | 돈과 직결, 충돌 시 재시도보다 데이터 정합성 우선 |
| 사용자 잔액 차감 | DB 비관적 락 | 동일 이유 |
| 쿠폰 선착순 | Redis 원자 연산 | 수천 TPS 처리, DB 락은 병목 발생 |

**동시성 테스트 결과 (100 스레드 동시 요청)**

```
재고 1개 상품 → 100명 동시 주문 시도 → 최종 재고: 0개 (정합성 보장)
잔액 100,000원 → 100명 동시 1,000원 차감 → 최종 잔액: 0원 (손실 없음)
쿠폰 100개 → 150명 동시 발급 시도 → 최종 발급: 100건 (초과 발급 없음)
```

> 설계 상세: [동시성 보고서](docs/동시성_보고서.md)

---

### 2. 이벤트 기반 아키텍처 — Kafka 비동기 처리

#### 문제

결제 완료 후 외부 데이터 플랫폼 연동, 쿠폰 DB 반영 등을 동기로 처리하면 **결제 응답 속도가 외부 시스템 지연에 종속**됩니다.
또한 도메인 간 강결합이 발생합니다.

#### 해결 — 이중 이벤트 레이어

```
[결제 완료]
    │
    ├─ Spring ApplicationEventPublisher
    │       └─ @TransactionalEventListener(AFTER_COMMIT)  ← 트랜잭션 커밋 후 실행
    │               └─ @Async 비동기 외부 플랫폼 연동
    │
    └─ Kafka Publisher → "order-completed" 토픽
            └─ OrderEventConsumer → 주문 완료 처리

[선착순 쿠폰 요청]
    └─ Kafka Publisher → "coupon-publish-request" 토픽
            └─ CouponEventConsumer → DB 영속화
```

**핵심 설계 포인트**

- `@TransactionalEventListener(phase = AFTER_COMMIT)`: 트랜잭션이 완전히 커밋된 후에만 이벤트 발행 → 롤백 시 외부 연동 방지
- `@Async`: 외부 시스템 응답 지연이 결제 응답에 영향 없도록 완전 분리
- Kafka Consumer Group으로 순차 처리 보장

> 설계 상세: [Kafka 설계](docs/카프카_설계.md) | [도메인 분리 및 분산 트랜잭션](docs/도메인분리_분산트랜잭션_설계.md)

---

### 3. Redis 다중 활용 — 캐싱 · 랭킹 · 분산락 · 큐

Redis를 단순 캐시가 아닌 **4가지 역할**로 활용했습니다.

#### 3-1. 인기 상품 캐싱 (@Cacheable)

```java
@Cacheable(value = "popular-products")     // TTL: 24시간
public List<PopularProductResponse> getPopularProducts() { ... }

@CacheEvict(value = "popular-products", allEntries = true)
public void updatePopularProducts() { ... }
```

#### 3-2. 판매 랭킹 (Sorted Set)

```
Redis Sorted Set: product:sales:ranking
  → member: productId, score: 판매량

ZINCRBY product:sales:ranking {판매수량} {productId}   // 판매 발생 시 점수 갱신
ZREVRANGEBYSCORE ...                                   // Top-N 실시간 조회
```

DB fallback 로직: Redis miss 시 DB에서 전체 판매량 로드 후 재시도

#### 3-3. 분산락 (Redisson)

```
LOCK:payment:{userId}:{orderId}   → Redisson RLock (waitTime: 10s, leaseTime: 30s)
```

#### 3-4. 선착순 쿠폰 큐 (List + Set)

```
coupon:stock:{couponId}     → 잔여 재고 (DECR 원자 연산)
coupon:users:{couponId}     → 이미 받은 사용자 Set (중복 방지)
coupon:queue:{couponId}     → 발급 대기 큐 (RPUSH / LPOP, FIFO)
```

큐에 진입 = 발급 확정, @Scheduled(300ms) 워커가 DB 영속화

> 설계 상세: [Redis 캐싱 전략](docs/Redis_캐싱_전략_보고서.md) | [랭킹 및 비동기 쿠폰 설계](docs/랭킹_및_비동기_쿠폰_설계_구현_회고.md)

---

### 4. 쿼리 성능 최적화 — 16.4s → 0.008s

#### 문제

인기 상품 TOP 5 조회 쿼리가 5백만 건 데이터에서 16초 이상 소요.

```sql
-- 기존 쿼리: 500만 건 전체 정렬 → 16.394초
SELECT p.id, ps.sales_count, pv.view_count
FROM product p
LEFT JOIN product_sales_count ps ON p.id = ps.product_id
LEFT JOIN product_view_count pv ON p.id = pv.product_id
ORDER BY ps.sales_count DESC, pv.view_count DESC
LIMIT 5;
```

#### 해결 — 서브쿼리로 정렬 범위 축소

```sql
-- 최적화 쿼리: 50건만 정렬 → 0.008초 (인덱스 적용 후)
SELECT p.id, ps.sales_count, pv.view_count
FROM (
    SELECT product_id, sales_count
    FROM product_sales_count
    ORDER BY sales_count DESC
    LIMIT 50                          -- 정렬 대상을 50건으로 제한
) ps
JOIN product p ON p.id = ps.product_id
JOIN product_view_count pv ON p.id = pv.product_id
ORDER BY ps.sales_count DESC, pv.view_count DESC
LIMIT 5;
```

**성능 비교**

| 버전 | 실행 시간 | 정렬 대상 |
|------|---------|---------|
| LEFT JOIN (기존) | 16.394초 | 5,000,000건 |
| INNER JOIN 변경 | 13.183초 | 5,000,000건 |
| 인덱스만 추가 | 13.034초 | 5,000,000건 |
| **서브쿼리 + 인덱스** | **0.008초** | **50건** |

**2,049배 성능 향상**

> 설계 상세: [성능 최적화 보고서](docs/성능_최적화_보고서.md)

---

### 5. 도메인 주도 설계 — 4-Layer Architecture

#### 구조

```
kr.hhplus.be.server.{domain}/
├── presentation/    # Controller, DTO (HTTP 요청/응답)
├── application/     # Service, Event (비즈니스 유스케이스, 트랜잭션)
├── domain/          # Entity, Repository Interface (핵심 비즈니스 규칙)
└── infrastructure/  # JPA Repository 구현체, 외부 연동
```

#### 핵심 원칙

**도메인 엔티티에 비즈니스 로직 집중**

```java
// 서비스가 아닌 엔티티가 비즈니스 규칙 결정
public class ProductEntity {
    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) throw new IllegalArgumentException("재고 부족");
        this.stockQuantity -= quantity;
        if (this.stockQuantity == 0) this.status = ProductStatus.SOLD_OUT; // 자동 상태 전환
    }
}

public class CouponEntity {
    public int calculateDiscount(int originalPrice) {
        int discounted = (int)(originalPrice * discountRate / 100);
        return Math.min(discounted, maxAmount); // 최대 할인 금액 캡 적용
    }
}
```

**의존성 역전 — 도메인은 인프라를 모른다**

```
Domain Layer (Repository Interface 정의)
    ↑ implements
Infrastructure Layer (JPA 구현체)
```

도메인 계층이 JPA, Redis, Kafka 등 외부 기술에 전혀 의존하지 않아 테스트 용이성과 기술 교체 유연성을 확보했습니다.

---

## 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                         Client                              │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTP
┌─────────────────────────▼───────────────────────────────────┐
│              Spring Boot Application                        │
│                                                             │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐   │
│  │ Balance  │ │ Product  │ │  Order   │ │   Payment    │   │
│  │  Domain  │ │  Domain  │ │  Domain  │ │    Domain    │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────────┘   │
│                                          ┌──────────────┐   │
│  ┌────────────────────────────────────┐  │   Coupon     │   │
│  │   Distributed Lock (AOP)          │  │   Domain     │   │
│  │   @DistributedLock + Redisson     │  └──────────────┘   │
│  └────────────────────────────────────┘                     │
└──────┬───────────────────┬────────────────────┬─────────────┘
       │                   │                    │
┌──────▼──────┐  ┌─────────▼──────┐  ┌─────────▼──────┐
│   MySQL 8.0 │  │  Redis         │  │  Apache Kafka  │
│             │  │  - Cache       │  │  - order-topic │
│  HikariCP   │  │  - SortedSet   │  │  - coupon-topic│
│  (max: 20)  │  │  - Redisson    │  │                │
│             │  │  - Queue       │  │                │
└─────────────┘  └────────────────┘  └────────────────┘
```

---

## API 명세

Swagger UI: `http://localhost:8080/swagger-ui.html` (로컬 실행 후 접근)

| 도메인 | Method | URI | 설명 |
|--------|--------|-----|------|
| Balance | GET | `/api/v1/users/{userId}/balance` | 잔액 조회 |
| Balance | POST | `/api/v1/users/{userId}/balance/charge` | 잔액 충전 |
| Product | GET | `/api/v1/products` | 상품 목록 조회 |
| Product | GET | `/api/v1/products/{productId}` | 상품 상세 조회 |
| Product | GET | `/api/v1/products/popular` | 인기 상품 조회 (캐싱) |
| Product | GET | `/api/v1/products/popular/ranking` | 판매 랭킹 (Redis SortedSet) |
| Order | POST | `/api/v1/users/{userId}/orders` | 주문 생성 |
| Payment | POST | `/api/v1/users/{userId}/orders/{orderId}/payment` | 결제 처리 (분산락) |
| Coupon | GET | `/api/v1/coupons` | 쿠폰 목록 조회 |
| Coupon | POST | `/api/v1/users/{userId}/coupons/{couponId}/issue` | 선착순 쿠폰 발급 |
| Coupon | GET | `/api/v1/users/{userId}/coupons` | 사용자 보유 쿠폰 조회 |

---

## ERD

```
USER ──1:1── USER_BALANCE
 │                │
 │                └── BALANCE_HISTORY (CHARGE/USE 이력)
 │
 ├──1:N── ORDER_RESULT
 │              │
 │              ├──1:N── ORDER_ITEM ───N:1── PRODUCT
 │              │                               │
 │              └──1:1── PAYMENT       ┌────────┴────────┐
 │                                     │                 │
 └──1:N── USER_COUPON ──N:1── COUPON   PRODUCT_VIEW_COUNT PRODUCT_SALES_COUNT
                                             └─────────── POPULAR_PRODUCT ──┘
```

전체 ERD: [ERD 상세](.docs/erd.md)

---

## 설계 문서

| 문서 | 내용 |
|------|------|
| [동시성 보고서](docs/동시성_보고서.md) | 비관적 락 / 낙관적 락 / 분산락 비교 및 시나리오별 적용 근거 |
| [성능 최적화 보고서](docs/성능_최적화_보고서.md) | MySQL 5M rows 쿼리 튜닝 과정 (16.4s → 0.008s) |
| [Kafka 설계](docs/카프카_설계.md) | 토픽 설계, Consumer Group 전략, 이벤트 플로우 |
| [Redis 캐싱 전략](docs/Redis_캐싱_전략_보고서.md) | @Cacheable 전략, TTL 설정, 캐시 무효화 |
| [랭킹 및 비동기 쿠폰](docs/랭킹_및_비동기_쿠폰_설계_구현_회고.md) | Redis ZSET 랭킹, Redis List 기반 선착순 큐 설계 회고 |
| [도메인 분리 및 분산 트랜잭션](docs/도메인분리_분산트랜잭션_설계.md) | AFTER_COMMIT 이벤트, Saga 패턴 고려 |

---

## 테스트 전략

총 **36개** 테스트 (통합 10 · 동시성 6 · 단위 20)

```
테스트 종류           기술                     목적
─────────────────────────────────────────────────────
통합 테스트          Testcontainers (MySQL)    실제 DB 환경에서 전체 흐름 검증
동시성 테스트        CountDownLatch +           Race Condition 재현 및 방어 확인
                    ExecutorService (100 threads)
단위 테스트          JUnit 5, MockMvc           비즈니스 로직 개별 검증
```

**동시성 테스트 예시**

```java
// 100명이 동시에 결제 시도 → 분산락이 직렬화 보장하는지 검증
CountDownLatch latch = new CountDownLatch(100);
ExecutorService executor = Executors.newFixedThreadPool(100);

for (int i = 0; i < 100; i++) {
    executor.submit(() -> {
        latch.countDown();
        latch.await();
        paymentService.processPayment(userId, orderId, request);
    });
}
// 최종 재고, 잔액, 쿠폰 발급 수 정합성 검증
```

---

## 실행 방법

### 1. 인프라 실행 (Docker)

```bash
docker-compose up -d
# MySQL 8.0 (3306), Kafka (9092), Zookeeper (2181), Redis (6379) 실행
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 3. Swagger UI 접근

```
http://localhost:8080/swagger-ui.html
```

### 4. 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 동시성 테스트만
./gradlew test --tests "*ConcurrencyTest*"

# 통합 테스트만
./gradlew test --tests "*IntegrationTest*"
```

### docker-compose 구성

```yaml
services:
  mysql:    # MySQL 8.0 / port 3306 / db: hhplus
  kafka:    # Kafka 7.4.0 / port 9092
  redis:    # Redis / port 6379 / appendonly 영속화
```
