package com.dangdangsalon.domain.estimate.controller;

import com.dangdangsalon.domain.dogprofile.dto.DogProfileResponseDto;
import com.dangdangsalon.domain.dogprofile.entity.Gender;
import com.dangdangsalon.domain.dogprofile.entity.Neutering;
import com.dangdangsalon.domain.estimate.dto.*;
import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.estimate.request.dto.FeatureResponseDto;
import com.dangdangsalon.domain.estimate.request.dto.ServicePriceResponseDto;
import com.dangdangsalon.domain.estimate.request.dto.ServiceResponseDto;
import com.dangdangsalon.domain.estimate.service.EstimateNotificationService;
import com.dangdangsalon.domain.estimate.service.EstimateService;
import com.dangdangsalon.domain.estimate.service.EstimateUpdateService;
import com.dangdangsalon.domain.estimate.service.EstimateWriteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = EstimateController.class)
@MockBean(JpaMetamodelMappingContext.class)
class EstimateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EstimateWriteService estimateWriteService;

    @MockBean
    private EstimateService estimateService;

    @MockBean
    private EstimateNotificationService estimateNotificationService;

    @MockBean
    private EstimateUpdateService estimateUpdateService;


    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "testUser",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
    }

    @Test
    @DisplayName("견적서 작성 반려견 요청 목록 조회 - 성공")
    void getEstimateRequestDog_Success() throws Exception {
        // Given
        Long requestId = 1L;

        ServiceResponseDto service1 = new ServiceResponseDto(1L, "목욕");
        ServiceResponseDto service2 = new ServiceResponseDto(2L, "털 미용");
        ServiceResponseDto service3 = new ServiceResponseDto(4L, "부분 가위컷");

        EstimateWriteResponseDto dog1 = EstimateWriteResponseDto.builder()
                .dogProfileResponseDto(new DogProfileResponseDto(1L, "dog1.jpg", "골댕이"))
                .description("발 만지는 걸 매우 싫어합니다")
                .serviceList(List.of(service1, service2, service3))
                .isAggression(false)
                .isHealthIssue(false)
                .build();

        ServiceResponseDto service4 = new ServiceResponseDto(1L, "목욕");
        ServiceResponseDto service5 = new ServiceResponseDto(2L, "털 미용");
        ServiceResponseDto service6 = new ServiceResponseDto(3L, "전체 클리닝");

        EstimateWriteResponseDto dog2 = EstimateWriteResponseDto.builder()
                .dogProfileResponseDto(new DogProfileResponseDto(2L, "dog2.jpg", "구름이"))
                .description("발 만지는 걸 매우 싫어합니다")
                .serviceList(List.of(service4, service5, service6))
                .isAggression(true)
                .isHealthIssue(false)
                .build();

        when(estimateWriteService.getEstimateRequestDog(requestId))
                .thenReturn(List.of(dog1, dog2));

        // When & Then
        mockMvc.perform(get("/api/estimate/dogrequest/{requestId}", requestId)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                SecurityContextHolder.getContext().getAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", hasSize(2)))
                .andExpect(jsonPath("$.response[0].dogProfileResponseDto.dogProfileId", is(1)))
                .andExpect(jsonPath("$.response[0].dogProfileResponseDto.profileImage", is("dog1.jpg")))
                .andExpect(jsonPath("$.response[0].dogProfileResponseDto.name", is("골댕이")))
                .andExpect(jsonPath("$.response[0].description", is("발 만지는 걸 매우 싫어합니다")))
                .andExpect(jsonPath("$.response[0].serviceList", hasSize(3)))
                .andExpect(jsonPath("$.response[0].serviceList[0].serviceId", is(1)))
                .andExpect(jsonPath("$.response[0].serviceList[0].description", is("목욕")))
                .andExpect(jsonPath("$.response[0].aggression", is(false)))
                .andExpect(jsonPath("$.response[0].healthIssue", is(false)))
                .andExpect(jsonPath("$.response[1].dogProfileResponseDto.dogProfileId", is(2)))
                .andExpect(jsonPath("$.response[1].dogProfileResponseDto.profileImage", is("dog2.jpg")))
                .andExpect(jsonPath("$.response[1].dogProfileResponseDto.name", is("구름이")))
                .andExpect(jsonPath("$.response[1].description", is("발 만지는 걸 매우 싫어합니다")))
                .andExpect(jsonPath("$.response[1].serviceList", hasSize(3)))
                .andExpect(jsonPath("$.response[1].serviceList[2].description", is("전체 클리닝")))
                .andExpect(jsonPath("$.response[1].aggression", is(true)))
                .andExpect(jsonPath("$.response[1].healthIssue", is(false)));
    }

    @Test
    @DisplayName("견적서 작성 반려견 요청 목록 조회 - 빈 결과")
    void getEstimateRequestDog_EmptyResult() throws Exception {
        // Given
        Long requestId = 1L;

        when(estimateWriteService.getEstimateRequestDog(requestId))
                .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/estimate/dogrequest/{requestId}", requestId)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                SecurityContextHolder.getContext().getAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", hasSize(0)));
    }

    @Test
    @DisplayName("견적서 작성 반려견 요청 상세 조회 - 성공")
    void getEstimateRequestDogDetail_Success() throws Exception {
        // Given
        Long requestId = 1L;
        Long dogProfileId = 1L;

        List<ServiceResponseDto> serviceList = List.of(
                new ServiceResponseDto(1L, "목욕"),
                new ServiceResponseDto(2L, "털 미용"),
                new ServiceResponseDto(4L, "부분 가위컷")
        );

        List<FeatureResponseDto> featureList = List.of(
                new FeatureResponseDto("물을 무서워해요"),
                new FeatureResponseDto("사람을 좋아해요")
        );

        EstimateWriteDetailResponseDto detailResponseDto = EstimateWriteDetailResponseDto.builder()
                .dogName("골댕이")
                .year(4)
                .month(10)
                .dogWeight(30)
                .gender(com.dangdangsalon.domain.dogprofile.entity.Gender.MALE)
                .neutering(com.dangdangsalon.domain.dogprofile.entity.Neutering.Y)
                .imageKey("dog1.jpg")
                .currentImageKey("currentPhoto123")
                .styleRefImageKey("stylePhoto456")
                .species("골든 리트리버")
                .serviceList(serviceList)
                .aggression(false)
                .healthIssue(false)
                .description("발 만지는 걸 매우 싫어합니다")
                .featureList(featureList)
                .build();

        when(estimateWriteService.getEstimateRequestDogDetail(requestId, dogProfileId))
                .thenReturn(detailResponseDto);

        // When & Then
        mockMvc.perform(get("/api/estimate/dogrequest/{requestId}/detail/{dogProfileId}", requestId, dogProfileId)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                SecurityContextHolder.getContext().getAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.dogName", is("골댕이")))
                .andExpect(jsonPath("$.response.year", is(4)))
                .andExpect(jsonPath("$.response.month", is(10)))
                .andExpect(jsonPath("$.response.dogWeight", is(30)))
                .andExpect(jsonPath("$.response.gender", is("MALE")))
                .andExpect(jsonPath("$.response.neutering", is("Y")))
                .andExpect(jsonPath("$.response.imageKey", is("dog1.jpg")))
                .andExpect(jsonPath("$.response.currentImageKey", is("currentPhoto123")))
                .andExpect(jsonPath("$.response.styleRefImageKey", is("stylePhoto456")))
                .andExpect(jsonPath("$.response.species", is("골든 리트리버")))
                .andExpect(jsonPath("$.response.serviceList", hasSize(3)))
                .andExpect(jsonPath("$.response.serviceList[0].serviceId", is(1)))
                .andExpect(jsonPath("$.response.serviceList[0].description", is("목욕")))
                .andExpect(jsonPath("$.response.aggression", is(false)))
                .andExpect(jsonPath("$.response.healthIssue", is(false)))
                .andExpect(jsonPath("$.response.description", is("발 만지는 걸 매우 싫어합니다")))
                .andExpect(jsonPath("$.response.featureList", hasSize(2)))
                .andExpect(jsonPath("$.response.featureList[0].description", is("물을 무서워해요")))
                .andExpect(jsonPath("$.response.featureList[1].description", is("사람을 좋아해요")));
    }

    @Test
    @DisplayName("견적서 등록 - 성공")
    void insertEstimate_Success() throws Exception {
        // Given
        ServiceRequestDto service1 = ServiceRequestDto.builder()
                .serviceId(1L)
                .price(30000)
                .build();

        ServiceRequestDto service2 = ServiceRequestDto.builder()
                .serviceId(2L)
                .price(70000)
                .build();

        DogPriceRequestDto dogPriceRequest = DogPriceRequestDto.builder()
                .dogProfileId(1L)
                .aggressionCharge(10000)
                .healthIssueCharge(20000)
                .serviceList(List.of(service1, service2))
                .build();

        EstimateWriteRequestDto requestDto = EstimateWriteRequestDto.builder()
                .requestId(1L)
                .groomerProfileId(1L)
                .aggressionCharge(5000)
                .healthIssueCharge(10000)
                .description("테스트 견적 등록")
                .imageKey("testImageKey123")
                .totalAmount(150000)
                .dogPriceList(List.of(dogPriceRequest))
                .build();

        EstimateIdResponseDto responseDto = new EstimateIdResponseDto(4L);
        when(estimateService.insertEstimate(any(EstimateWriteRequestDto.class)))
                .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                SecurityContextHolder.getContext().getAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.estimateId", is(4)));
    }


    @Test
    @DisplayName("견적서 수정 조회 - 성공")
    void getEstimateGroomer_Success() throws Exception {
        // Given
        Long estimateId = 1L;

        // Mock 데이터 생성
        ServicePriceResponseDto service1 = new ServicePriceResponseDto(1L, "목욕", 30000);
        ServicePriceResponseDto service2 = new ServicePriceResponseDto(2L, "털 미용", 70000);
        ServicePriceResponseDto service3 = new ServicePriceResponseDto(4L, "부분 가위컷", 30000);

        DogProfileResponseDto dog1Profile = new DogProfileResponseDto(1L, "dog1.jpg", "골댕이");
        EstimateDogResponseDto dog1 = EstimateDogResponseDto.builder()
                .dogProfileResponseDto(dog1Profile)
                .description("발 만지는 걸 매우 싫어합니다")
                .dogPrice(130000)
                .serviceList(List.of(service1, service2, service3))
                .isAggression(false)
                .isHealthIssue(false)
                .build();

        EstimateResponseDto responseDto = EstimateResponseDto.builder()
                .comment("추가로 시간이 더 필요할 수 있습니다.")
                .totalAmount(120000)
                .date(LocalDateTime.of(2024, 11, 25, 10, 0, 0))
                .estimateList(List.of(dog1))
                .build();

        when(estimateService.getEstimateGroomer(estimateId)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/estimate/{estimateId}", estimateId)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                SecurityContextHolder.getContext().getAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.comment", is("추가로 시간이 더 필요할 수 있습니다.")))
                .andExpect(jsonPath("$.response.totalAmount", is(120000)))
                .andExpect(jsonPath("$.response.date", is("2024-11-25T10:00:00")))

                // 첫 번째 강아지 검증
                .andExpect(jsonPath("$.response.estimateList[0].dogProfileResponseDto.dogProfileId", is(1)))
                .andExpect(jsonPath("$.response.estimateList[0].dogProfileResponseDto.profileImage", is("dog1.jpg")))
                .andExpect(jsonPath("$.response.estimateList[0].dogProfileResponseDto.name", is("골댕이")))
                .andExpect(jsonPath("$.response.estimateList[0].description", is("발 만지는 걸 매우 싫어합니다")))
                .andExpect(jsonPath("$.response.estimateList[0].dogPrice", is(130000)))
                .andExpect(jsonPath("$.response.estimateList[0].serviceList", hasSize(3)))
                .andExpect(jsonPath("$.response.estimateList[0].serviceList[0].serviceId", is(1)))
                .andExpect(jsonPath("$.response.estimateList[0].serviceList[0].description", is("목욕")))
                .andExpect(jsonPath("$.response.estimateList[0].serviceList[0].price", is(30000)))
                .andExpect(jsonPath("$.response.estimateList[0].aggression", is(false)))
                .andExpect(jsonPath("$.response.estimateList[0].healthIssue", is(false)));
    }

    @Test
    @DisplayName("견적서 수정 강아지별 상세 조회 - 성공")
    void getEstimateDogDetail_Success() throws Exception {
        // Given
        Long requestId = 1L;
        Long dogProfileId = 1L;

        ServicePriceResponseDto service1 = new ServicePriceResponseDto(1L, "목욕", 30000);
        ServicePriceResponseDto service2 = new ServicePriceResponseDto(2L, "털 미용", 70000);
        ServicePriceResponseDto service3 = new ServicePriceResponseDto(4L, "부분 가위컷", 30000);

        FeatureResponseDto feature1 = new FeatureResponseDto("물을 무서워해요");
        FeatureResponseDto feature2 = new FeatureResponseDto("사람을 좋아해요");

        EstimateDogDetailResponseDto responseDto = EstimateDogDetailResponseDto.builder()
                .dogName("골댕이")
                .year(4)
                .month(10)
                .dogWeight(30)
                .gender(Gender.valueOf("MALE"))
                .neutering(Neutering.valueOf("Y"))
                .imageKey("dog1.jpg")
                .currentImageKey("currentPhoto123")
                .styleRefImageKey("stylePhoto456")
                .species("골든 리트리버")
                .aggressionCharge(99000)
                .healthIssueCharge(10000)
                .serviceList(List.of(service1, service2, service3))
                .aggression(false)
                .healthIssue(false)
                .description("발 만지는 걸 매우 싫어합니다")
                .featureList(List.of(feature1, feature2))
                .build();

        when(estimateService.getEstimateDogDetail(requestId, dogProfileId)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/estimate/{requestId}/{dogProfileId}", requestId, dogProfileId)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                SecurityContextHolder.getContext().getAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.dogName", is("골댕이")))
                .andExpect(jsonPath("$.response.year", is(4)))
                .andExpect(jsonPath("$.response.month", is(10)))
                .andExpect(jsonPath("$.response.dogWeight", is(30)))
                .andExpect(jsonPath("$.response.gender", is("MALE")))
                .andExpect(jsonPath("$.response.neutering", is("Y")))
                .andExpect(jsonPath("$.response.imageKey", is("dog1.jpg")))
                .andExpect(jsonPath("$.response.currentImageKey", is("currentPhoto123")))
                .andExpect(jsonPath("$.response.styleRefImageKey", is("stylePhoto456")))
                .andExpect(jsonPath("$.response.species", is("골든 리트리버")))
                .andExpect(jsonPath("$.response.aggressionCharge", is(99000)))
                .andExpect(jsonPath("$.response.healthIssueCharge", is(10000)))
                .andExpect(jsonPath("$.response.serviceList", hasSize(3)))
                .andExpect(jsonPath("$.response.serviceList[0].serviceId", is(1)))
                .andExpect(jsonPath("$.response.serviceList[0].description", is("목욕")))
                .andExpect(jsonPath("$.response.serviceList[0].price", is(30000)))
                .andExpect(jsonPath("$.response.aggression", is(false)))
                .andExpect(jsonPath("$.response.healthIssue", is(false)))
                .andExpect(jsonPath("$.response.description", is("발 만지는 걸 매우 싫어합니다")))
                .andExpect(jsonPath("$.response.featureList", hasSize(2)))
                .andExpect(jsonPath("$.response.featureList[0].description", is("물을 무서워해요")))
                .andExpect(jsonPath("$.response.featureList[1].description", is("사람을 좋아해요")));
    }

    @Test
    @DisplayName("견적서 상세 조회 - 성공")
    void getEstimateDetail_Success() throws Exception {
        // Given
        Long estimateId = 1L;

        MyEstimateDetailResponseDto responseDto = MyEstimateDetailResponseDto.builder()
                .status(EstimateStatus.SEND)
                .description("추가로 시간이 더 필요할 수 있습니다.")
                .imageKey("photo12345")
                .totalAmount(120000)
                .date(LocalDateTime.of(2024, 11, 25, 10, 0, 0))
                .startChat("사랑합니다")
                .build();

        when(estimateService.getEstimateDetail(estimateId)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/estimate/detail/{estimateId}", estimateId)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                SecurityContextHolder.getContext().getAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.status", is("SEND")))
                .andExpect(jsonPath("$.response.description", is("추가로 시간이 더 필요할 수 있습니다.")))
                .andExpect(jsonPath("$.response.imageKey", is("photo12345")))
                .andExpect(jsonPath("$.response.totalAmount", is(120000)))
                .andExpect(jsonPath("$.response.date", is("2024-11-25T10:00:00")))
                .andExpect(jsonPath("$.response.startChat", is("사랑합니다")));
    }

    @Test
    @DisplayName("내 견적서 조회 - 성공")
    void getMyEstimate_Success() throws Exception {
        // Given
        Long requestId = 1L;

        // Mock 데이터 생성
        MyEstimateResponseDto estimate1 = MyEstimateResponseDto.builder()
                .totalAmount(100000)
                .build();

        MyEstimateResponseDto estimate2 = MyEstimateResponseDto.builder()
                .totalAmount(150000)
                .build();

        when(estimateService.getMyEstimate(requestId)).thenReturn(List.of(estimate1, estimate2));

        // When & Then
        mockMvc.perform(get("/api/estimate/my/{requestId}", requestId)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                SecurityContextHolder.getContext().getAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", hasSize(2)))
                .andExpect(jsonPath("$.response[0].totalAmount", is(100000)))
                .andExpect(jsonPath("$.response[1].totalAmount", is(150000)));
    }

    @Test
    @DisplayName("내 견적서 조회 - 빈 결과")
    void getMyEstimate_EmptyResult() throws Exception {
        // Given
        Long requestId = 1L;

        when(estimateService.getMyEstimate(requestId)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/estimate/my/{requestId}", requestId)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                SecurityContextHolder.getContext().getAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", hasSize(0)));
    }

    @Test
    @DisplayName("견적서 상태 업데이트 - 성공")
    void updateEstimateStatus_Success() throws Exception {
        // Given
        Long estimateId = 1L;

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.put("/api/estimate/{estimateId}", estimateId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                SecurityContextHolder.getContext().getAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", is("견적서 상태 업데이트 완료")));
    }
}
