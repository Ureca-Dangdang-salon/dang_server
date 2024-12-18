package com.dangdangsalon.domain.payment.service;

import com.dangdangsalon.domain.contest.dto.ContestPaymentDto;
import com.dangdangsalon.domain.contest.dto.ContestPaymentRequestDto;
import com.dangdangsalon.domain.coupon.entity.Coupon;
import com.dangdangsalon.domain.coupon.repository.CouponRepository;
import com.dangdangsalon.domain.estimate.request.dto.ServicePriceResponseDto;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestServiceRepository;
import com.dangdangsalon.domain.orders.entity.OrderStatus;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.orders.repository.OrdersRepository;
import com.dangdangsalon.domain.payment.dto.PaymentDogProfileResponseDto;
import com.dangdangsalon.domain.payment.dto.PaymentResponseDto;
import com.dangdangsalon.domain.payment.entity.Payment;
import com.dangdangsalon.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentGetService {

    private final OrdersRepository ordersRepository;
    private final PaymentRepository paymentRepository;
    private final CouponRepository couponRepository;
    private final EstimateRequestServiceRepository estimateRequestServiceRepository;

    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPayments(Long userId) {
        // 1. PENDING이 아닌 상태의 모든 주문 조회
        List<Orders> orders = ordersRepository.findAllByUserIdAndStatusNot(userId, OrderStatus.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("결제 관련 주문이 없습니다."));

        // 2. 각 주문에 대한 결제 정보와 프로필별 서비스 정보 생성
        return orders.stream().map(order -> {
            Payment payment = paymentRepository.findByOrders(order)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "주문 ID " + order.getId() + "에 대한 결제 정보가 없습니다."
                    ));

            Coupon coupon = findCouponForPayment(payment);

            List<PaymentDogProfileResponseDto> dogProfileList = getDogProfileServices(order);

            return PaymentResponseDto.from(payment, order, coupon, dogProfileList);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<ContestPaymentDto> getContestPayments(ContestPaymentRequestDto requestDto, Long userId) {
        // 1. ACCEPTED 상태의 콘테스트 기간에 해당하는 모든 주문 조회
        List<Orders> acceptedOrders = ordersRepository.findAllByUserIdAndStatusAndContestDate(
                        userId, OrderStatus.ACCEPTED, requestDto.getStartDate(), requestDto.getEndDate())
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 기간에 예약된 주문이 없습니다. ContestStartDate: " + requestDto.getStartDate().toString()
                                + " ContestEndDate: " + requestDto.getEndDate().toString()));

        // 2. 각 주문에 대한 결제 정보와 프로필별 서비스 정보 생성
        return acceptedOrders.stream().map(order -> {
            // 결제 정보 가져오기
            Payment payment = paymentRepository.findByOrders(order)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "주문 ID " + order.getId() + "에 대한 결제 정보가 없습니다."
                    ));

            return ContestPaymentDto.builder()
                    .groomerProfileId(order.getEstimate().getGroomerProfile().getId())
                    .groomerName(order.getEstimate().getGroomerProfile().getName())
                    .groomerImage(order.getEstimate().getGroomerProfile().getImageKey())
                    .paymentDate(payment.getRequestedAt())
                    .reservationDate(order.getEstimate().getDate())
                    .totalAmount(payment.getTotalAmount())
                    .serviceList(getOrderServices(order))
                    .build();
        }).toList();
    }

    private List<String> getOrderServices(Orders order) {
        List<Long> profileIds = order.getEstimate()
                .getEstimateRequest()
                .getEstimateRequestProfiles()
                .stream()
                .map(EstimateRequestProfiles::getId)
                .toList();

        return estimateRequestServiceRepository.findByEstimateRequestServicesProfilesIdIn(profileIds)
                .stream()
                .map(service -> service.getGroomerService().getDescription())
                .distinct()
                .toList();
    }

    private List<PaymentDogProfileResponseDto> getDogProfileServices(Orders order) {
        // 1. 주문과 관련된 모든 반려견 프로필 조회
        List<EstimateRequestProfiles> profiles = order.getEstimate()
                .getEstimateRequest()
                .getEstimateRequestProfiles();

        // 2. 각 프로필에 대한 서비스 정보 생성
        return profiles.stream().map(profile -> {
            // 3. 해당 프로필의 서비스 리스트 조회
            List<ServicePriceResponseDto> services = estimateRequestServiceRepository
                    .findByEstimateRequestProfilesId(profile.getId())
                    .stream()
                    .map(service -> new ServicePriceResponseDto(
                            service.getGroomerService().getId(),
                            service.getGroomerService().getDescription(),
                            service.getPrice()
                    ))
                    .toList();

            return PaymentDogProfileResponseDto.builder()
                    .profileId(profile.getId())
                    .dogName(profile.getDogProfile().getName())
                    .servicePriceList(services)
                    .aggressionCharge(profile.getAggressionCharge())
                    .healthIssueCharge(profile.getHealthIssueCharge())
                    .build();
        }).toList();
    }

    private Coupon findCouponForPayment(Payment payment) {
        if (payment.getCoupon() == null) {
            return null;
        }
        return couponRepository.findById(payment.getCoupon().getId())
                .orElseThrow(() -> new IllegalArgumentException("결제에 맞는 쿠폰이 없습니다."));
    }
}

