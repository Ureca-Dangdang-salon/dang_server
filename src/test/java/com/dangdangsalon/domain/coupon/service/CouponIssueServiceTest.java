//package com.dangdangsalon.domain.coupon.service;
//
//import com.dangdangsalon.config.RedisPublisher;
//import com.dangdangsalon.domain.coupon.dto.QueueStatusDto;
//import com.dangdangsalon.domain.coupon.entity.Coupon;
//import com.dangdangsalon.domain.coupon.entity.CouponEvent;
//import com.dangdangsalon.domain.coupon.repository.CouponEventRepository;
//import com.dangdangsalon.domain.coupon.repository.CouponRepository;
//import com.dangdangsalon.domain.user.entity.User;
//import com.dangdangsalon.domain.user.repository.UserRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.SetOperations;
//import org.springframework.data.redis.core.ValueOperations;
//import org.springframework.data.redis.core.ZSetOperations;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class CouponIssueServiceTest {
//
//    @InjectMocks
//    private CouponIssueService couponIssueService;
//
//    @Mock
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @Mock
//    private ValueOperations<String, Object> valueOperations;
//
//    @Mock
//    private SetOperations<String, Object> setOperations;
//
//    @Mock
//    private ZSetOperations<String, Object> zSetOperations;
//
//    @Mock
//    private RedisPublisher redisPublisher;
//
//    @Mock
//    private CouponRepository couponRepository;
//
//    @Mock
//    private CouponEventRepository couponEventRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Test
//    @DisplayName("대기열에 사용자를 추가")
//    void joinQueue() {
//        // Mock 설정
//        when(redisTemplate.opsForValue()).thenReturn(valueOperations); // opsForValue Mock 설정
//        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations); // opsForZSet Mock 설정
//        when(redisTemplate.opsForSet()).thenReturn(setOperations); // opsForSet Mock 설정
//
//        // Mock 동작 설정
//        when(redisTemplate.hasKey("coupon_remaining:1")).thenReturn(true); // 쿠폰 잔여 키 존재
//        when(valueOperations.get("coupon_remaining:1")).thenReturn(10); // 잔여 쿠폰 수
//        when(zSetOperations.rank("coupon_queue:1", "1")).thenReturn(null); // 대기열에 없는 상태
//        when(setOperations.isMember("issued_users:1", "1")).thenReturn(false); // 발급되지 않은 상태
//
//        // When
//        String result = couponIssueService.joinQueue(1L, 1L);
//
//        // Then
//        assertThat(result).isEqualTo("대기열에 참여했습니다.");
//        verify(zSetOperations, times(1)).add(eq("coupon_queue:1"), eq("1"), anyDouble());
//    }
//
//    @Test
//    @DisplayName("대기열 상태를 전송")
//    void publishQueueStatus() {
//        // Mock 설정
//        when(redisTemplate.opsForValue()).thenReturn(valueOperations); // opsForValue Mock 설정
//        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations); // opsForZSet Mock 설정
//
//        // Mock 동작 설정
//        when(zSetOperations.zCard("coupon_queue:1")).thenReturn(10L); // 대기열 총 사용자 수
//        when(valueOperations.get("coupon_remaining:1")).thenReturn(5); // 잔여 쿠폰 수
//        when(zSetOperations.range("coupon_queue:1", 0, -1)).thenReturn(Set.of("1", "2")); // 대기열 사용자 ID
//
//        // When
//        couponIssueService.publishQueueStatus(1L);
//
//        // Then
//        verify(redisPublisher, times(2)).sendToEmitter(anyLong(), any(QueueStatusDto.class), eq("queue_status"));
//    }
//
//    @Test
//    @DisplayName("SSE 구독을 처리")
//    void subscribeQueueUpdates() {
//        // Mock 설정
//        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations); // opsForZSet() 반환값 설정
//        when(zSetOperations.rank("coupon_queue:1", "1")).thenReturn(0L); // rank 반환값 설정
//
//        when(redisTemplate.opsForValue()).thenReturn(valueOperations); // opsForValue() 반환 Mock
//        when(valueOperations.get("coupon_remaining:1")).thenReturn(10); // get 호출 Mock 설정
//
//        // When
//        SseEmitter emitter = couponIssueService.subscribeQueueUpdates(1L, 1L);
//
//        // Then
//        assertThat(emitter).isNotNull();
//        verify(redisPublisher, times(1)).registerEmitter(eq(1L), any(SseEmitter.class)); // registerEmitter 호출 검증
//    }
//
//    @Test
//    @DisplayName("대기열 사용자에 대해 쿠폰 발급을 처리")
//    void processQueue() {
//        // Mock 설정
//        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations); // opsForZSet Mock 설정
//        when(redisTemplate.opsForValue()).thenReturn(valueOperations); // opsForValue Mock 설정
//        when(redisTemplate.opsForSet()).thenReturn(setOperations); // opsForSet Mock 설정
//
//        // 대기열에서 첫 번째 사용자 반환 설정
//        when(zSetOperations.range("coupon_queue:1", 0, 0)).thenReturn(Set.of("1")).thenReturn(null); // 두 번째 호출은 null
//
//        // 쿠폰 남은 개수 반환 설정
//        when(valueOperations.get("coupon_remaining:1")).thenReturn(5); // 초기 쿠폰 개수
//
//        // CouponEvent와 User Mock 설정
//        CouponEvent mockEvent = CouponEvent.builder().build();
//        User mockUser = User.builder().build();
//        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(mockEvent));
//        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
//
//        // When: 서비스 호출
//        couponIssueService.processQueue(1L);
//
//        // Then: Mock 객체에 대한 상호작용 검증
//        verify(valueOperations, times(1)).decrement("coupon_remaining:1"); // 쿠폰 개수 감소 검증
//        verify(setOperations, times(1)).add("issued_users:1", "1"); // 발급 사용자 추가 검증
//        verify(zSetOperations, times(1)).remove("coupon_queue:1", "1"); // 대기열에서 사용자 제거 검증
//        verify(couponRepository, times(1)).save(any(Coupon.class)); // 쿠폰 저장 검증
//    }
//
//    @Test
//    @DisplayName("대기열 사용자에 대해 쿠폰 발급 처리 실패 - 대기열에 사용자 없음")
//    void processQueue_Failure_NoUsers() {
//        // Mock 설정
//        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
//        when(redisTemplate.opsForZSet().range("coupon_queue:1", 0, 0)).thenReturn(null); // 대기열이 비어 있음
//
//        // When
//        couponIssueService.processQueue(1L);
//
//        // Then
//        verify(valueOperations, never()).decrement("coupon_remaining:1"); // 호출되지 않아야 함
//        verify(setOperations, never()).add("issued_users:1", "1"); // 호출되지 않아야 함
//        verify(zSetOperations, never()).remove("coupon_queue:1", "1"); // 호출되지 않아야 함
//    }
//
//    @Test
//    @DisplayName("대기열 참여 실패 - 이미 대기열에 있거나 발급된 상태")
//    void joinQueue_Failure_AlreadyQueuedOrIssued() {
//        // Mock 설정
//        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
//        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
//        when(redisTemplate.opsForSet()).thenReturn(setOperations);
//
//        when(redisTemplate.hasKey("coupon_remaining:1")).thenReturn(true);
//        when(valueOperations.get("coupon_remaining:1")).thenReturn(10);
//        when(zSetOperations.rank("coupon_queue:1", "1")).thenReturn(0L); // 이미 대기열에 있음
//
//        // When
//        String result = couponIssueService.joinQueue(1L, 1L);
//
//        // Then
//        assertThat(result).isEqualTo("이미 대기열에 참여한 사용자입니다.");
//        verify(zSetOperations, never()).add(anyString(), anyString(), anyDouble());
//    }
//
//    @Test
//    @DisplayName("대기열 상태 전송 실패")
//    void publishQueueStatus_Failure() {
//        // Mock 설정
//        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
//        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
//
//        when(zSetOperations.zCard("coupon_queue:1")).thenReturn(null); // 대기열 크기를 가져올 수 없음
//        when(valueOperations.get("coupon_remaining:1")).thenReturn(null); // 남은 쿠폰 정보를 가져올 수 없음
//
//        // When
//        couponIssueService.publishQueueStatus(1L);
//
//        // Then
//        verify(redisPublisher, never()).sendToEmitter(anyLong(), any(), anyString());
//    }
//
//    @Test
//    @DisplayName("활성화된 쿠폰 이벤트 초기화 테스트")
//    void initializeEvents() {
//        // Given
//        CouponEvent mockEvent1 = CouponEvent.builder().totalQuantity(100).build();
//        CouponEvent mockEvent2 = CouponEvent.builder().totalQuantity(200).build();
//        ReflectionTestUtils.setField(mockEvent1, "id", 1L);
//        ReflectionTestUtils.setField(mockEvent2, "id", 2L);
//
//        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
//        when(couponEventRepository.findActiveEvents(any(LocalDateTime.class)))
//                .thenReturn(List.of(mockEvent1, mockEvent2));
//
//        // When
//        couponIssueService.initializeEvents();
//
//        // Then
//        verify(valueOperations, times(1)).set("coupon_remaining:1", 100);
//        verify(valueOperations, times(1)).set("coupon_remaining:2", 200);
//        verify(redisTemplate, times(1)).delete("coupon_queue:1");
//        verify(redisTemplate, times(1)).delete("coupon_queue:2");
//        verify(redisTemplate, times(1)).delete("issued_users:1");
//        verify(redisTemplate, times(1)).delete("issued_users:2");
//    }
//}