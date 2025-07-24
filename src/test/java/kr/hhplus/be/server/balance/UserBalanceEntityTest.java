package kr.hhplus.be.server.balance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import kr.hhplus.be.server.balance.domain.entity.UserBalanceEntity;

class UserBalanceEntityTest {

    @Nested
    @DisplayName("잔액 충전 테스트")
    class ChargeTest {

        @Test
        void 정상적인_잔액_충전_성공() {
            // given
            UserBalanceEntity userBalance = new UserBalanceEntity(1L, 100000);
            int chargeAmount = 50000;

            // when
            userBalance.charge(chargeAmount);

            // then
            assertThat(userBalance.getAmount()).isEqualTo(150000);
        }

        @Test
        void 최소_충전_금액_성공() {
            // given
            UserBalanceEntity userBalance = new UserBalanceEntity(1L, 0);
            int chargeAmount = 1000;

            // when
            userBalance.charge(chargeAmount);

            // then
            assertThat(userBalance.getAmount()).isEqualTo(chargeAmount);
        }

        @Test
        void 최대_충전_금액_성공() {
            // given
            UserBalanceEntity userBalance = new UserBalanceEntity(1L, 0);
            int chargeAmount = 1000000;

            // when
            userBalance.charge(chargeAmount);

            // then
            assertThat(userBalance.getAmount()).isEqualTo(chargeAmount);
        }

        @Test
        void 최소_충전_금액_미만_실패() {
            // given
            UserBalanceEntity userBalance = new UserBalanceEntity(1L, 0);
            int chargeAmount = 999;

            // when & then
            assertThatThrownBy(() -> userBalance.charge(chargeAmount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("최소 충전 금액 미만");
        }

        @Test
        void 최대_충전_금액_초과_실패() {
            // given
            UserBalanceEntity userBalance = new UserBalanceEntity(1L, 0);
            int chargeAmount = 1000001;

            // when & then
            assertThatThrownBy(() -> userBalance.charge(chargeAmount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("최대 충전 금액 초과");
        }

        @Test
        void 음수_충전_금액_실패() {
            // given
            UserBalanceEntity userBalance = new UserBalanceEntity(1L, 0);
            int chargeAmount = -1000;

            // when & then
            assertThatThrownBy(() -> userBalance.charge(chargeAmount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("최소 충전 금액 미만");
        }

        @Test
        void 최대_잔액_초과_실패() {
            // given
            UserBalanceEntity userBalance = new UserBalanceEntity(1L, 9900000);
            int chargeAmount = 1000000;

            // when & then
            assertThatThrownBy(() -> userBalance.charge(chargeAmount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("최대 보유 잔액 초과");
        }

        @Test
        void 최대_잔액_경계값_성공() {
            // given
            UserBalanceEntity userBalance = new UserBalanceEntity(1L, 9000000);
            int chargeAmount = 1000000;

            // when
            userBalance.charge(chargeAmount);

            // then
            assertThat(userBalance.getAmount()).isEqualTo(10000000);
        }
    }

    @Nested
    @DisplayName("잔액 사용 테스트")
    class UseTest {

        @Test
        void 잔액_사용_성공() {
            // given
            UserBalanceEntity userBalance = new UserBalanceEntity(1L, 100000);
            int useAmount = 50000;

            // when
            userBalance.use(useAmount);

            // then
            assertThat(userBalance.getAmount()).isEqualTo(50000);
        }

        @Test
        void 잔액_부족_실패() {
            // given
            UserBalanceEntity userBalance = new UserBalanceEntity(1L, 30000);
            int useAmount = 50000;

            // when & then
            assertThatThrownBy(() -> userBalance.use(useAmount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("잔액 부족");
        }

        @Test
        void 음수_사용_금액_실패() {
            // given
            UserBalanceEntity userBalance = new UserBalanceEntity(1L, 100000);
            int useAmount = -1000;

            // when & then
            assertThatThrownBy(() -> userBalance.use(useAmount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("사용 금액 양수");
        }

        @Test
        void 전액_사용_성공() {
            // given
            UserBalanceEntity userBalance = new UserBalanceEntity(1L, 100000);
            int useAmount = 100000;

            // when
            userBalance.use(useAmount);

            // then
            assertThat(userBalance.getAmount()).isEqualTo(0);
        }
    }

}