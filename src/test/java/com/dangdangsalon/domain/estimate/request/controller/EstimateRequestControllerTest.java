package com.dangdangsalon.domain.estimate.request.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.dogprofile.dto.DogProfileResponseDto;
import com.dangdangsalon.domain.estimate.request.dto.*;
import com.dangdangsalon.domain.estimate.request.entity.RequestStatus;
import com.dangdangsalon.domain.estimate.request.service.EstimateRequestDetailService;
import com.dangdangsalon.domain.estimate.request.service.EstimateRequestServices;
import com.dangdangsalon.domain.estimate.request.service.GroomerEstimateRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = EstimateRequestController.class)
@MockBean(JpaMetamodelMappingContext.class)
class EstimateRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EstimateRequestServices estimateRequestServices;

    @MockBean
    private GroomerEstimateRequestService groomerEstimateRequestService;

    @MockBean
    private EstimateRequestDetailService estimateRequestDetailService;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "testUser",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
    }

    @Test
    @DisplayName("견적 요청 등록 성공 테스트")
    public void createEstimateRequestSuccess() throws Exception {
        // Mock CustomOAuth2User 설정
        Long mockUserId = 1L; // Mock 사용자 ID
        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
        when(customOAuth2User.getUserId()).thenReturn(mockUserId);

        // SecurityContext에 사용자 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customOAuth2User,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Mock 데이터 생성
        DogEstimateRequestDto dogRequest1 = new DogEstimateRequestDto(
                1L, // 강아지 프로필 ID
                "currentImageKey123", // 현재 이미지 키
                "styleRefImageKey123", // 스타일 참조 이미지 키
                false, // 공격성 여부
                true, // 건강 문제 여부
                "강아지가 긴장합니다.", // 설명
                List.of(1L, 2L, 3L) // 제공되는 서비스 목록
        );

        EstimateRequestDto mockRequest = new EstimateRequestDto(
                1L, // 미용사 아이디
                1L, // 구 아이디
                LocalDateTime.now(), // 날짜
                "발톱 정리", // 서비스 타입
                List.of(dogRequest1) // 강아지 견적 요청 목록
        );

        // 서비스 계층 호출 Mock 설정
        doNothing().when(estimateRequestServices).insertEstimateRequest(any(EstimateRequestDto.class), anyLong());

        // 요청 수행 및 응답 검증
        mockMvc.perform(post("/api/estimaterequest")
                        .contentType(MediaType.APPLICATION_JSON) // JSON 요청
                        .content(objectMapper.writeValueAsString(mockRequest)) // 요청 본문 설정
                        .with(csrf()) // CSRF 토큰 추가
                        .principal(authentication)) // 사용자 인증 추가
                .andExpect(status().isOk()) // HTTP 200 응답 확인
                .andExpect(jsonPath("$.response").value("견적 요청 등록에 성공하였습니다.")); // 메시지 확인
    }


    @Test
    @DisplayName("유저의 견적 요청 목록 조회 테스트")
    public void getMyEstimateRequestsSuccess() throws Exception {
        // Mock CustomOAuth2User 설정
        Long mockUserId = 1L;
        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
        when(customOAuth2User.getUserId()).thenReturn(mockUserId);

        // SecurityContext에 사용자 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customOAuth2User,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Mock 데이터 생성
        DogNameResponseDto dog1 = DogNameResponseDto.builder()
                .dogName("강아지1")
                .build();

        DogNameResponseDto dog2 = DogNameResponseDto.builder()
                .dogName("강아지2")
                .build();

        MyEstimateRequestResponseDto request1 = MyEstimateRequestResponseDto.builder()
                .requestId(1L)
                .dogList(List.of(dog1, dog2))
                .date(LocalDateTime.now())
                .status(RequestStatus.PENDING) // 가정: RequestStatus 열거형 값
                .build();

        MyEstimateRequestResponseDto request2 = MyEstimateRequestResponseDto.builder()
                .requestId(2L)
                .dogList(List.of(dog1))
                .date(LocalDateTime.now())
                .status(RequestStatus.COMPLETED)
                .build();

        List<MyEstimateRequestResponseDto> myEstimateRequests = List.of(request1, request2);

        // 서비스 모킹
        when(estimateRequestServices.getMyEstimateRequest(mockUserId)).thenReturn(myEstimateRequests);

        // 요청 및 검증
        mockMvc.perform(get("/api/estimaterequest/my")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response[0].requestId").value(1L)) // 첫 번째 요청의 ID 확인
                .andExpect(jsonPath("$.response[0].dogList[0].dogName").value("강아지1")) // 첫 번째 강아지 이름 확인
                .andExpect(jsonPath("$.response[0].status").value("PENDING")) // 첫 번째 요청 상태 확인
                .andExpect(jsonPath("$.response[1].requestId").value(2L)) // 두 번째 요청의 ID 확인
                .andExpect(jsonPath("$.response[1].dogList[0].dogName").value("강아지1")) // 두 번째 요청의 강아지 이름 확인
                .andExpect(jsonPath("$.response[1].status").value("COMPLETED")); // 두 번째 요청 상태 확인
    }

    @Test
    @DisplayName("견적 요청 목록 조회 - 성공")
    void getEstimateRequests_Success() throws Exception {
        // Given
        Long groomerProfileId = 1L;

        EstimateRequestResponseDto request1 = EstimateRequestResponseDto.builder()
                .estimateId(100L)
                .name("홍길동")
                .date(LocalDate.of(2024, 11, 25))
                .serviceType("VISIT")
                .region("서울 강남구")
                .imageKey("image123")
                .estimateRequestStatus("PENDING")
                .groomerEstimateRequestStatus("APPROVED")
                .build();

        EstimateRequestResponseDto request2 = EstimateRequestResponseDto.builder()
                .estimateId(101L)
                .name("김철수")
                .date(LocalDate.of(2024, 11, 26))
                .serviceType("VISIT")
                .region("서울 서초구")
                .imageKey("image456")
                .estimateRequestStatus("COMPLETED")
                .groomerEstimateRequestStatus("PENDING")
                .build();

        List<EstimateRequestResponseDto> responseDtoList = List.of(request1, request2);

        when(groomerEstimateRequestService.getEstimateRequest(groomerProfileId)).thenReturn(responseDtoList);

        // When & Then
        mockMvc.perform(get("/api/estimaterequest/{groomerProfileId}", groomerProfileId)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                SecurityContextHolder.getContext().getAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", hasSize(2)))

                // 첫 번째 요청 검증
                .andExpect(jsonPath("$.response[0].estimateId", is(100)))
                .andExpect(jsonPath("$.response[0].name", is("홍길동")))
                .andExpect(jsonPath("$.response[0].date", is("2024-11-25")))
                .andExpect(jsonPath("$.response[0].serviceType", is("VISIT")))
                .andExpect(jsonPath("$.response[0].region", is("서울 강남구")))
                .andExpect(jsonPath("$.response[0].imageKey", is("image123")))
                .andExpect(jsonPath("$.response[0].estimateRequestStatus", is("PENDING")))
                .andExpect(jsonPath("$.response[0].groomerEstimateRequestStatus", is("APPROVED")))

                // 두 번째 요청 검증
                .andExpect(jsonPath("$.response[1].estimateId", is(101)))
                .andExpect(jsonPath("$.response[1].name", is("김철수")))
                .andExpect(jsonPath("$.response[1].date", is("2024-11-26")))
                .andExpect(jsonPath("$.response[1].serviceType", is("VISIT")))
                .andExpect(jsonPath("$.response[1].region", is("서울 서초구")))
                .andExpect(jsonPath("$.response[1].imageKey", is("image456")))
                .andExpect(jsonPath("$.response[1].estimateRequestStatus", is("COMPLETED")))
                .andExpect(jsonPath("$.response[1].groomerEstimateRequestStatus", is("PENDING")));
    }

    @Test
    @DisplayName("반려견별 견적 요청 상세 조회 - 성공")
    void getEstimateRequestDetail_Success() throws Exception {
        // Given
        Long requestId = 1L;

        // Mock 데이터 생성
        DogProfileResponseDto dogProfile1 = new DogProfileResponseDto(1L, "dog1.jpg", "골댕이");
        ServiceResponseDto service1 = new ServiceResponseDto(1L, "목욕");
        ServiceResponseDto service2 = new ServiceResponseDto(2L, "털 미용");
        FeatureResponseDto feature1 = new FeatureResponseDto("물을 무서워해요");
        FeatureResponseDto feature2 = new FeatureResponseDto("사람을 좋아해요");

        EstimateDetailResponseDto detail1 = EstimateDetailResponseDto.builder()
                .dogProfileResponseDto(dogProfile1)
                .currentPhotoKey("currentPhoto123")
                .styleRefPhotoKey("styleRefPhoto456")
                .aggression(false)
                .healthIssue(true)
                .description("발 만지는 걸 싫어합니다")
                .serviceList(List.of(service1, service2))
                .featureList(List.of(feature1, feature2))
                .build();

        DogProfileResponseDto dogProfile2 = new DogProfileResponseDto(2L, "dog2.jpg", "구름이");
        ServiceResponseDto service3 = new ServiceResponseDto(3L, "전체 미용");
        FeatureResponseDto feature3 = new FeatureResponseDto("사람을 무서워해요");

        EstimateDetailResponseDto detail2 = EstimateDetailResponseDto.builder()
                .dogProfileResponseDto(dogProfile2)
                .currentPhotoKey("currentPhoto789")
                .styleRefPhotoKey("styleRefPhoto012")
                .aggression(true)
                .healthIssue(false)
                .description("목욕을 좋아합니다")
                .serviceList(List.of(service3))
                .featureList(List.of(feature3))
                .build();

        List<EstimateDetailResponseDto> mockResponse = List.of(detail1, detail2);

        when(estimateRequestDetailService.getEstimateRequestDetail(requestId)).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/estimaterequest/detail/{requestId}", requestId)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                SecurityContextHolder.getContext().getAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", hasSize(2)))

                // 첫 번째 반려견 검증
                .andExpect(jsonPath("$.response[0].dogProfileResponseDto.dogProfileId", is(1)))
                .andExpect(jsonPath("$.response[0].dogProfileResponseDto.profileImage", is("dog1.jpg")))
                .andExpect(jsonPath("$.response[0].dogProfileResponseDto.name", is("골댕이")))
                .andExpect(jsonPath("$.response[0].currentPhotoKey", is("currentPhoto123")))
                .andExpect(jsonPath("$.response[0].styleRefPhotoKey", is("styleRefPhoto456")))
                .andExpect(jsonPath("$.response[0].aggression", is(false)))
                .andExpect(jsonPath("$.response[0].healthIssue", is(true)))
                .andExpect(jsonPath("$.response[0].description", is("발 만지는 걸 싫어합니다")))
                .andExpect(jsonPath("$.response[0].serviceList", hasSize(2)))
                .andExpect(jsonPath("$.response[0].serviceList[0].serviceId", is(1)))
                .andExpect(jsonPath("$.response[0].serviceList[0].description", is("목욕")))
                .andExpect(jsonPath("$.response[0].featureList", hasSize(2)))
                .andExpect(jsonPath("$.response[0].featureList[0].description", is("물을 무서워해요")))

                // 두 번째 반려견 검증
                .andExpect(jsonPath("$.response[1].dogProfileResponseDto.dogProfileId", is(2)))
                .andExpect(jsonPath("$.response[1].dogProfileResponseDto.profileImage", is("dog2.jpg")))
                .andExpect(jsonPath("$.response[1].dogProfileResponseDto.name", is("구름이")))
                .andExpect(jsonPath("$.response[1].currentPhotoKey", is("currentPhoto789")))
                .andExpect(jsonPath("$.response[1].styleRefPhotoKey", is("styleRefPhoto012")))
                .andExpect(jsonPath("$.response[1].aggression", is(true)))
                .andExpect(jsonPath("$.response[1].healthIssue", is(false)))
                .andExpect(jsonPath("$.response[1].description", is("목욕을 좋아합니다")))
                .andExpect(jsonPath("$.response[1].serviceList", hasSize(1)))
                .andExpect(jsonPath("$.response[1].serviceList[0].serviceId", is(3)))
                .andExpect(jsonPath("$.response[1].serviceList[0].description", is("전체 미용")))
                .andExpect(jsonPath("$.response[1].featureList", hasSize(1)))
                .andExpect(jsonPath("$.response[1].featureList[0].description", is("사람을 무서워해요")));
    }

    @Test
    @DisplayName("미용사 견적 요청 삭제 - 성공")
    void deleteEstimateRequest_Success() throws Exception {
        // Given
        Long requestId = 1L;

        // 서비스 계층의 메서드 모킹
        doNothing().when(groomerEstimateRequestService).deleteGroomerEstimateRequest(requestId);

        // When & Then
        mockMvc.perform(delete("/api/estimaterequest/{requestId}", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                SecurityContextHolder.getContext().getAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", is("견적 요청 삭제에 성공하였습니다.")));
    }

    @Test
    @DisplayName("유저의 견적 요청 상세 조회 - 성공")
    void getMyEstimateRequestDetail_Success() throws Exception {
        // Given
        Long requestId = 1L;

        ServiceResponseDto service1 = new ServiceResponseDto(1L, "목욕");
        ServiceResponseDto service2 = new ServiceResponseDto(2L, "털 미용");

        MyEstimateRequestDetailResponseDto detail1 = MyEstimateRequestDetailResponseDto.builder()
                .imageKey("dogImage1.jpg")
                .dogName("골댕이")
                .aggression(false)
                .healthIssue(true)
                .description("발 만지는 걸 싫어합니다.")
                .serviceList(List.of(service1, service2))
                .build();

        MyEstimateRequestDetailResponseDto detail2 = MyEstimateRequestDetailResponseDto.builder()
                .imageKey("dogImage2.jpg")
                .dogName("푸들")
                .aggression(true)
                .healthIssue(false)
                .description("사람을 많이 경계합니다.")
                .serviceList(List.of(service1))
                .build();

        List<MyEstimateRequestDetailResponseDto> responseDtoList = List.of(detail1, detail2);

        when(estimateRequestDetailService.getMyEstimateDetailRequest(requestId)).thenReturn(responseDtoList);

        // When & Then
        mockMvc.perform(get("/api/estimaterequest/my/detail/{requestId}", requestId)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                SecurityContextHolder.getContext().getAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", hasSize(2)))

                // 첫 번째 상세 정보 검증
                .andExpect(jsonPath("$.response[0].imageKey", is("dogImage1.jpg")))
                .andExpect(jsonPath("$.response[0].dogName", is("골댕이")))
                .andExpect(jsonPath("$.response[0].aggression", is(false)))
                .andExpect(jsonPath("$.response[0].healthIssue", is(true)))
                .andExpect(jsonPath("$.response[0].description", is("발 만지는 걸 싫어합니다.")))
                .andExpect(jsonPath("$.response[0].serviceList", hasSize(2)))
                .andExpect(jsonPath("$.response[0].serviceList[0].serviceId", is(1)))
                .andExpect(jsonPath("$.response[0].serviceList[0].description", is("목욕")))

                // 두 번째 상세 정보 검증
                .andExpect(jsonPath("$.response[1].imageKey", is("dogImage2.jpg")))
                .andExpect(jsonPath("$.response[1].dogName", is("푸들")))
                .andExpect(jsonPath("$.response[1].aggression", is(true)))
                .andExpect(jsonPath("$.response[1].healthIssue", is(false)))
                .andExpect(jsonPath("$.response[1].description", is("사람을 많이 경계합니다.")))
                .andExpect(jsonPath("$.response[1].serviceList", hasSize(1)))
                .andExpect(jsonPath("$.response[1].serviceList[0].serviceId", is(1)))
                .andExpect(jsonPath("$.response[1].serviceList[0].description", is("목욕")));
    }

    @Test
    @DisplayName("견적 요청 상태를 CANCEL로 변경 - 성공")
    void stopEstimate_Success() throws Exception {
        // Given
        Long requestId = 1L;

        // 서비스 계층 모킹
        Mockito.doNothing().when(estimateRequestServices).stopEstimate(requestId);

        // When & Then
        mockMvc.perform(put("/api/estimaterequest/{requestId}/stop", requestId)
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                SecurityContextHolder.getContext().getAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", is("견적 그만 받기에 성공하였습니다.")));
    }


}
