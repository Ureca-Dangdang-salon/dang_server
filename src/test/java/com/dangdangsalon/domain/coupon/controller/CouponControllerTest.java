//package com.dangdangsalon.domain.coupon.controller;
//
//import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
//import com.dangdangsalon.domain.coupon.dto.CouponInfoResponseDto;
//import com.dangdangsalon.domain.coupon.dto.CouponMainResponseDto;
//import com.dangdangsalon.domain.coupon.dto.CouponUserResponseDto;
//import com.dangdangsalon.domain.coupon.service.CouponIssueService;
//import com.dangdangsalon.domain.coupon.service.CouponService;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
//import org.springframework.http.MediaType;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.*;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@ActiveProfiles("test")
//@WebMvcTest(controllers = CouponController.class)
//@MockBean(JpaMetamodelMappingContext.class)
//class CouponControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private CouponIssueService couponIssueService;
//
//    @MockBean
//    private CouponService couponService;
//
//    @Test
//    @DisplayName("쿠폰 발급 요청 테스트")
//    void issueCoupon() throws Exception {
//        // Mock CustomOAuth2User 설정
//        Long mockUserId = 1L;
//        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
//        when(customOAuth2User.getUserId()).thenReturn(mockUserId);
//
//        // SecurityContext에 사용자 설정
//        Authentication authentication = new UsernamePasswordAuthenticationToken(
//                customOAuth2User,
//                null,
//                List.of(new SimpleGrantedAuthority("ROLE_USER")) // 권한 추가
//        );
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        // Given
//        when(couponIssueService.joinQueue(anyLong(), anyLong())).thenReturn("대기열에 참여했습니다.");
//
//        // When & Then
//        mockMvc.perform(MockMvcRequestBuilders.post("/api/coupons/issued")
//                        .param("userId", "1")
//                        .param("eventId", "2")
//                        .with(csrf()))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.response").value("대기열에 참여했습니다."));
//
//        verify(couponIssueService, times(1)).joinQueue(1L, 2L);
//    }
//
//    @Test
//    @DisplayName("SSE 구독 요청 테스트")
//    void subscribeQueueUpdates() throws Exception {
//        // Mock CustomOAuth2User 설정
//        Long mockUserId = 1L;
//        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
//        when(customOAuth2User.getUserId()).thenReturn(mockUserId);
//
//        // SecurityContext에 사용자 설정
//        Authentication authentication = new UsernamePasswordAuthenticationToken(
//                customOAuth2User,
//                null,
//                List.of(new SimpleGrantedAuthority("ROLE_USER")) // 권한 추가
//        );
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        // Given
//        SseEmitter emitter = new SseEmitter();
//        when(couponIssueService.subscribeQueueUpdates(anyLong(), anyLong())).thenReturn(emitter);
//
//        // When & Then
//        mockMvc.perform(get("/api/coupons/queue/updates")
//                        .param("userId", "1")
//                        .param("eventId", "2")
//                        .accept(MediaType.TEXT_EVENT_STREAM))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andReturn();
//
//        verify(couponIssueService, times(1)).subscribeQueueUpdates(1L, 2L);
//    }
//
//    @Test
//    @DisplayName("메인 페이지 유효 쿠폰 조회 테스트")
//    void getCouponValidMainPage() throws Exception {
//        // Mock CustomOAuth2User 설정
//        Long mockUserId = 1L;
//        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
//        when(customOAuth2User.getUserId()).thenReturn(mockUserId);
//
//        // SecurityContext에 사용자 설정
//        Authentication authentication = new UsernamePasswordAuthenticationToken(
//                customOAuth2User,
//                null,
//                List.of(new SimpleGrantedAuthority("ROLE_USER")) // 권한 추가
//        );
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        // Given
//        List<CouponMainResponseDto> response = List.of(
//                CouponMainResponseDto.builder().eventName("이벤트1").build(),
//                CouponMainResponseDto.builder().eventName("이벤트2").build()
//        );
//        when(couponService.getCouponValidMainPage()).thenReturn(response);
//
//        // When & Then
//        mockMvc.perform(get("/api/coupons/main"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.response[0].eventName").value("이벤트1"))
//                .andExpect(jsonPath("$.response[1].eventName").value("이벤트2"));
//
//        verify(couponService, times(1)).getCouponValidMainPage();
//    }
//
//    @Test
//    @DisplayName("특정 이벤트 정보 조회 테스트")
//    void getCouponInfo() throws Exception {
//        // Mock CustomOAuth2User 설정
//        Long mockUserId = 1L;
//        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
//        when(customOAuth2User.getUserId()).thenReturn(mockUserId);
//
//        // SecurityContext에 사용자 설정
//        Authentication authentication = new UsernamePasswordAuthenticationToken(
//                customOAuth2User,
//                null,
//                List.of(new SimpleGrantedAuthority("ROLE_USER")) // 권한 추가
//        );
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        // Given
//        CouponInfoResponseDto response = CouponInfoResponseDto.builder()
//                .eventId(1L)
//                .name("이벤트1")
//                .discountAmount(5000)
//                .build();
//
//        when(couponService.getCouponInfo(anyLong())).thenReturn(response);
//
//        // When & Then
//        mockMvc.perform(get("/api/coupons/1"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.response.name").value("이벤트1"))
//                .andExpect(jsonPath("$.response.discountAmount").value(5000));
//
//        verify(couponService, times(1)).getCouponInfo(1L);
//    }
//
//    @Test
//    @DisplayName("사용자 쿠폰 조회 테스트")
//    void getUserCoupon() throws Exception {
//        // Mock CustomOAuth2User 설정
//        Long mockUserId = 1L;
//        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
//        when(customOAuth2User.getUserId()).thenReturn(mockUserId);
//
//        // SecurityContext에 사용자 설정
//        Authentication authentication = new UsernamePasswordAuthenticationToken(
//                customOAuth2User,
//                null,
//                List.of(new SimpleGrantedAuthority("ROLE_USER")) // 권한 추가
//        );
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        // Given
//        List<CouponUserResponseDto> response = List.of(
//                CouponUserResponseDto.builder()
//                        .couponId(1L)
//                        .name("쿠폰1")
//                        .build()
//                ,
//                CouponUserResponseDto.builder()
//                        .couponId(2L)
//                        .name("쿠폰2")
//                        .build()
//
//        );
//        when(couponService.getUserCoupon(anyLong())).thenReturn(response);
//
//        // When & Then
//        mockMvc.perform(get("/api/coupons/users"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.response[0].couponId").value(1))
//                .andExpect(jsonPath("$.response[0].name").value("쿠폰1"))
//                .andExpect(jsonPath("$.response[1].couponId").value(2))
//                .andExpect(jsonPath("$.response[1].name").value("쿠폰2"));
//
//        verify(couponService, times(1)).getUserCoupon(anyLong());
//    }
//}