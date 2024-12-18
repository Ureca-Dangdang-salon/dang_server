package com.dangdangsalon.domain.coupon.service;

import com.dangdangsalon.config.RedisPublisher;
import com.dangdangsalon.domain.coupon.dto.CouponInfoResponseDto;
import com.dangdangsalon.domain.coupon.dto.CouponMainResponseDto;
import com.dangdangsalon.domain.coupon.dto.CouponUserResponseDto;
import com.dangdangsalon.domain.coupon.dto.QueueStatusDto;
import com.dangdangsalon.domain.coupon.entity.Coupon;
import com.dangdangsalon.domain.coupon.entity.CouponEvent;
import com.dangdangsalon.domain.coupon.entity.CouponStatus;
import com.dangdangsalon.domain.coupon.repository.CouponEventRepository;
import com.dangdangsalon.domain.coupon.repository.CouponRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @InjectMocks
    private CouponService couponService;

    @Mock
    private CouponEventRepository couponEventRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("유효한 쿠폰 이벤트를 조회")
    void getCouponValidMainPage() {
        // Given
        CouponEvent event1 = CouponEvent.builder()
                .name("여름 이벤트")
                .startedAt(LocalDateTime.now().minusDays(1))
                .endedAt(LocalDateTime.now().plusDays(1))
                .build();

        CouponEvent event2 = CouponEvent.builder()
                .name("겨울 이벤트")
                .startedAt(LocalDateTime.now().minusDays(2))
                .endedAt(LocalDateTime.now().plusDays(2))
                .build();

        when(couponEventRepository.findUpcomingEvents()).thenReturn(List.of(event1, event2));

        // When
        List<CouponMainResponseDto> result = couponService.getCouponValidMainPage();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEventName()).isEqualTo("여름 이벤트");
        assertThat(result.get(1).getEventName()).isEqualTo("겨울 이벤트");
    }

    @Test
    @DisplayName("사용자의 쿠폰을 조회")
    void getUserCoupon() {
        // Given
        User user = User.builder().build();
        Coupon coupon1 = Coupon.builder()
                .couponName("할인 쿠폰 1")
                .status(CouponStatus.NOT_USED)
                .build();
        Coupon coupon2 = Coupon.builder()
                .couponName("할인 쿠폰 2")
                .status(CouponStatus.USED)
                .build();

        user.getCoupons().add(coupon1);
        user.getCoupons().add(coupon2);
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        List<CouponUserResponseDto> result = couponService.getUserCoupon(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("할인 쿠폰 1");
        assertThat(result.get(0).getCouponStatus()).isEqualTo(CouponStatus.NOT_USED);
    }

    @Test
    @DisplayName("특정 이벤트의 쿠폰 정보를 조회")
    void getCouponInfo() {
        // Given
        CouponEvent event = CouponEvent.builder()
                .name("봄 이벤트")
                .discountAmount(5000)
                .discountType(null) // DiscountType은 미리 정의되어 있어야 함
                .build();

        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(event));

        // When
        CouponInfoResponseDto result = couponService.getCouponInfo(1L);

        // Then
        assertThat(result.getName()).isEqualTo("봄 이벤트");
        assertThat(result.getDiscountAmount()).isEqualTo(5000);
    }

    @Test
    @DisplayName("특정 이벤트의 쿠폰 정보 조회 실패")
    void getCouponInfo_Failure() {
        // Given
        when(couponEventRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> couponService.getCouponInfo(1L));
    }

    @Test
    @DisplayName("사용자의 쿠폰 조회 실패")
    void getUserCoupon_Failure() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> couponService.getUserCoupon(1L));
    }

    @Test
    @DisplayName("유효한 쿠폰 이벤트 조회 실패")
    void getCouponValidMainPage_Failure() {
        // Given
        when(couponEventRepository.findUpcomingEvents()).thenReturn(Collections.emptyList());

        // When
        List<CouponMainResponseDto> result = couponService.getCouponValidMainPage();

        // Then
        assertThat(result).isEmpty(); // 결과가 비어 있어야 함
    }

}