```mermaid
erDiagram

 USER {
        BIGINT id PK "사용자 ID"
        STRING name "이름"
        DATETIME reg_dt "등록일"
        DATETIME mdfcn_dt "수정일"
    }

    USER_BALANCE {
        BIGINT user_id PK,FK "사용자 ID"
        INT balance "잔액"
        DATETIME reg_dt "등록일"
        DATETIME mdfcn_dt "수정일"
    }

    BALANCE_HISTORY {
        BIGINT id PK "ID"
        BIGINT user_id FK "사용자 ID"
        INT amount "금액"
        STRING type "타입: CHARGE, USE"
        DATETIME reg_dt "등록일"
    }

    PRODUCT {
        BIGINT id PK "상품 ID"
        STRING name "상품명"
        INT price "상품 가격"
        INT total_quantity "총 수량"
        INT stock_quantity "남은 수량"
        STRING status "상품 상태"
        DATETIME reg_dt "등록일"
        DATETIME mdfcn_dt "수정일"
    }

    PRODUCT_STATISTICS {
        BIGINT product_id PK,FK "상품 ID"
        INT view_count "조회수"
        INT sales_count "판매량"
        DATETIME reg_dt "등록일"
        DATETIME mdfcn_dt "수정일"
    }

    POPULAR_PRODUCT {
        BIGINT product_id PK,FK "상품 ID"
        INT view_count "조회수"
        INT sales_count "판매량"
        DATETIME reg_dt "등록일"
    }

    COUPON {
        BIGINT id PK "쿠폰 ID"
        STRING coupon_name "쿠폰 이름"
        INT discount_rate "할인율"
        INT max_amount "최대 할인 금액"
        INT quantity "총 발급 수량"
        INT issued_count "발급된 수량"
        STRING status "쿠폰 상태"
        DATETIME start_dt "사용 시작일"
        DATETIME end_dt "사용 마감일"
        DATETIME reg_dt "등록일"
        DATETIME mdfcn_dt "수정일"
    }

    USER_COUPON {
        BIGINT id PK "사용자쿠폰 ID"
        BIGINT user_id FK "사용자 ID"
        BIGINT coupon_id FK "쿠폰 ID"
        STRING coupon_name "쿠폰 이름"
        STRING status "쿠폰 상태"
        DATETIME reg_dt "등록일"
    }

    ORDER_RESULT {
        BIGINT id PK "주문 ID"
        BIGINT user_id FK "사용자 ID"
        BIGINT user_coupon_id FK "사용한 쿠폰 ID"
        INT total_amount "총 주문 금액"
        INT discount_amount "할인 금액"
        INT final_amount "최종 결제 금액"
        STRING status "주문 상태"
        DATETIME order_time "주문 시간"
        DATETIME reg_dt "등록일"
        DATETIME mdfcn_dt "수정일"
    }

    ORDER_ITEM {
        BIGINT id PK "주문 아이템 ID"
        BIGINT order_id FK "주문 ID"
        BIGINT product_id FK "상품 ID"
        INT quantity "수량"
        INT price "상품 가격"
        DATETIME reg_dt "등록일"
    }

    PAYMENT {
        BIGINT id PK "결제 ID"
        BIGINT order_id FK "주문 ID"
        BIGINT user_id FK "사용자 ID"
        INT paid_amount "결제 금액"
        INT original_amount "원래 금액"
        INT discount_amount "할인 금액"
        STRING payment_method "결제 수단"
        STRING payment_status "결제 상태"
        DATETIME paid_at "결제 시간"
        DATETIME reg_dt "등록일"
    }

    %% 관계 정의
    USER ||--|| USER_BALANCE : "잔액 보유"
    USER ||--o{ BALANCE_HISTORY : "잔액 이력"
    USER ||--o{ USER_COUPON : "쿠폰 보유"
    USER ||--o{ ORDER_RESULT : "주문함"
    COUPON ||--o{ USER_COUPON : "발급됨"
    USER_COUPON ||--|| ORDER_RESULT : "쿠폰 사용"
    ORDER_RESULT ||--|| PAYMENT : "결제"
    ORDER_RESULT ||--o{ ORDER_ITEM : "주문 상세"
    PRODUCT ||--o{ ORDER_ITEM : "주문됨"
    PRODUCT ||--|| PRODUCT_STATISTICS : "통계"
    PRODUCT_STATISTICS ||--|| POPULAR_PRODUCT : "인기상품"