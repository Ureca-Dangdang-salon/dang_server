package com.dangdangsalon.domain.coupon.listener;

import com.dangdangsalon.domain.coupon.service.CouponEventManageService;
//import com.dangdangsalon.domain.coupon.service.CouponQueueService;
import com.dangdangsalon.domain.coupon.service.CouponQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponEventListener implements MessageListener {

//    private final CouponQueueService couponQueueService;
    private final CouponEventManageService couponEventManageService;
    private final CouponQueueService couponQueueService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String eventName = new String(message.getBody()).replace("\"", "");;
        System.out.println("Received message: " + eventName); // 메시지 로그 추가
        couponEventManageService.issueCoupons(eventName);

//        couponQueueService.processQueue(eventName);
//        // 모든 사용자에게 대기열 상태 알림 전송
        couponQueueService.notifyAllUsers(eventName);
    }
}
