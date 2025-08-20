# Redis 기반 인기상품 캐싱 전략 보고서

## 1. 캐싱 대상 분석

### 인기상품 조회 (`getPopularProducts`)
- **특징**: 메인페이지 접속 시마다 호출, 자주 조회되지만 자주 변경되지 않음
- **문제점**: 정렬 연산으로 인한 DB 부하, 대량 트래픽 시 지연 발생
- **해결책**: Redis 캐싱으로 응답 시간 단축

### 인기상품 업데이트 (`updatePopularProducts`)
- **특징**: 배치 작업으로 주기적 실행, 복잡한 JOIN 연산
- **문제점**: 높은 CPU 사용량, 긴 처리 시간
- **해결책**: 업데이트 후 캐시 무효화로 일관성 보장

## 2. 캐싱 전략 구현

```java
// 인기상품 조회 시 캐시 적용
@Cacheable(value = "popular-products")
@Transactional
public List<PopularProductEntity> getPopularProducts() {
    return popularProductRepository.findPopularProductsOrderedByPriority();
}

// 인기상품 업데이트 시 캐시 무효화
@CacheEvict(value = "popular-products", allEntries = true)
@Transactional
public void updatePopularProducts() {
    // DB 업데이트 로직
}
```

## 3. 캐시 동작 흐름

```
1. 첫 번째 조회: DB 조회 → Redis 캐시 저장 → 응답
2. 두 번째 조회: Redis 캐시에서 응답 (빠름!)
3. 데이터 업데이트: DB 업데이트 → Redis 캐시 삭제
4. 다음 조회: 새로운 데이터로 캐시 재생성
```

