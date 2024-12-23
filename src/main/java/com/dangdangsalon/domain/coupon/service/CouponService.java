package com.dangdangsalon.domain.coupon.service;

import com.dangdangsalon.domain.coupon.dto.CouponInfoResponseDto;
import com.dangdangsalon.domain.coupon.dto.CouponMainResponseDto;
import com.dangdangsalon.domain.coupon.dto.CouponUserResponseDto;
import com.dangdangsalon.domain.coupon.entity.CouponEvent;
import com.dangdangsalon.domain.coupon.entity.CouponStatus;
import com.dangdangsalon.domain.coupon.repository.CouponEventRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponEventRepository couponEventRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CouponMainResponseDto> getCouponValidMainPage() {
        List<CouponEvent> activeEvent = couponEventRepository.findUpcomingEvents();

        return activeEvent.stream()
                .map(CouponMainResponseDto::create)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CouponUserResponseDto> getUserCoupon(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("유저 아이디를 찾을 수 없습니다. userId : " + userId));

        return user.getCoupons().stream()
                .filter(coupon -> coupon.getStatus() == CouponStatus.NOT_USED) // NOT_USED 상태만 필터링
                .map(CouponUserResponseDto::create)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CouponInfoResponseDto getCouponInfo(Long eventId) {
        CouponEvent couponEvent = couponEventRepository.findById(eventId).orElseThrow(() ->
                new IllegalArgumentException("쿠폰 이벤트를 찾을 수 없습니다. couponEventId : " + eventId));

        return CouponInfoResponseDto.create(couponEvent);
    }

}
