package com.dangdangsalon.domain.estimate.api;

import com.dangdangsalon.domain.dogprofile.dto.DogProfileResponseDto;
import com.dangdangsalon.domain.dogprofile.entity.Gender;
import com.dangdangsalon.domain.dogprofile.entity.Neutering;
import com.dangdangsalon.domain.estimate.dto.*;
import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.estimate.request.dto.FeatureResponseDto;
import com.dangdangsalon.domain.estimate.request.dto.ServicePriceResponseDto;
import com.dangdangsalon.domain.estimate.request.dto.ServiceResponseDto;
import com.dangdangsalon.domain.estimate.service.EstimateService;
import com.dangdangsalon.domain.estimate.service.EstimateWriteService;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class EstimateApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EstimateWriteService estimateWriteService;

    @MockBean
    private EstimateService estimateService;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("견적서 작성 반려견 요청 목록 조회 테스트")
    void getEstimateRequestDog() {
        List<EstimateWriteResponseDto> mockResponse = List.of(new EstimateWriteResponseDto());
        given(estimateWriteService.getEstimateRequestDog(anyLong())).willReturn(mockResponse);

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/estimate/dogrequest/1")
                .then()
                .statusCode(200)
                .body("response", hasSize(1));

        verify(estimateWriteService).getEstimateRequestDog(anyLong());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("견적서 작성 반려견 요청 상세 보기 테스트")
    void getEstimateRequestDogDetail() {
        // Mock 데이터 생성
        EstimateWriteDetailResponseDto mockResponse = EstimateWriteDetailResponseDto.builder()
                .dogName("구름이")
                .year(3)
                .month(6)
                .dogWeight(10)
                .gender(Gender.MALE)
                .neutering(Neutering.Y)
                .imageKey("image-key")
                .currentImageKey("current-image-key")
                .styleRefImageKey("style-ref-image-key")
                .species("푸들")
                .serviceList(List.of(new ServiceResponseDto(1L, "기본 미용")))
                .aggression(false)
                .healthIssue(true)
                .description("반려견은 친근하지만 약간의 건강 문제가 있습니다.")
                .featureList(List.of(new FeatureResponseDto("차분함"), new FeatureResponseDto("활발함")))
                .build();

        given(estimateWriteService.getEstimateRequestDogDetail(anyLong(), anyLong())).willReturn(mockResponse);

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/estimate/dogrequest/1/detail/1")
                .then()
                .statusCode(200)
                .body("response.dogName", equalTo("구름이"))
                .body("response.year", equalTo(3))
                .body("response.month", equalTo(6))
                .body("response.dogWeight", equalTo(10))
                .body("response.gender", equalTo("MALE"))
                .body("response.neutering", equalTo("Y"))
                .body("response.imageKey", equalTo("image-key"))
                .body("response.currentImageKey", equalTo("current-image-key"))
                .body("response.styleRefImageKey", equalTo("style-ref-image-key"))
                .body("response.species", equalTo("푸들"))
                .body("response.serviceList[0].serviceId", equalTo(1))
                .body("response.serviceList[0].description", equalTo("기본 미용"))
                .body("response.aggression", equalTo(false))
                .body("response.healthIssue", equalTo(true))
                .body("response.description", equalTo("반려견은 친근하지만 약간의 건강 문제가 있습니다."))
                .body("response.featureList[0].description", equalTo("차분함"))
                .body("response.featureList[1].description", equalTo("활발함"));

        // 서비스 호출 검증
        verify(estimateWriteService).getEstimateRequestDogDetail(anyLong(), anyLong());
    }



    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("견적서 등록 테스트")
    void insertEstimate() {

        EstimateIdResponseDto responseDto = new EstimateIdResponseDto(4L);
        when(estimateService.insertEstimate(any(EstimateWriteRequestDto.class)))
                .thenReturn(responseDto);

        List<ServiceRequestDto> serviceList = List.of(
                ServiceRequestDto.builder()
                        .serviceId(1L)
                        .price(10000)
                        .build(),
                ServiceRequestDto.builder()
                        .serviceId(2L)
                        .price(15000)
                        .build()
        );

        List<DogPriceRequestDto> dogPriceList = List.of(
                DogPriceRequestDto.builder()
                        .dogProfileId(1L)
                        .aggressionCharge(5000)
                        .healthIssueCharge(3000)
                        .serviceList(serviceList)
                        .build(),
                DogPriceRequestDto.builder()
                        .dogProfileId(2L)
                        .aggressionCharge(4000)
                        .healthIssueCharge(2000)
                        .serviceList(serviceList)
                        .build()
        );

        EstimateWriteRequestDto requestDto = EstimateWriteRequestDto.builder()
                .requestId(123L)
                .groomerProfileId(456L)
                .aggressionCharge(7000)
                .healthIssueCharge(5000)
                .description("견적 설명")
                .imageKey("example-image-key")
                .totalAmount(50000)
                .date(LocalDateTime.now())
                .dogPriceList(dogPriceList)
                .build();

        // When & Then
        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post("/api/estimate")
                .then()
                .statusCode(200)
                .body("response.estimateId", equalTo(4)); // 예상 ID 검증

        verify(estimateService).insertEstimate(any(EstimateWriteRequestDto.class));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("견적서 수정 조회 테스트")
    void getEstimateGroomer() {
        // Mock 데이터 생성
        ServicePriceResponseDto service1 = new ServicePriceResponseDto(1L, "기본 미용", 30000);
        ServicePriceResponseDto service2 = new ServicePriceResponseDto(2L, "스페셜 미용", 70000);

        DogProfileResponseDto dogProfile = DogProfileResponseDto.builder()
                .dogProfileId(1L)
                .profileImage("image-url")
                .name("구름이")
                .build();

        EstimateDogResponseDto dogResponse = EstimateDogResponseDto.builder()
                .dogProfileResponseDto(dogProfile)
                .description("테스트 견적 설명")
                .serviceList(List.of(service1, service2))
                .isAggression(false)
                .isHealthIssue(true)
                .dogPrice(120000)
                .build();

        EstimateResponseDto mockResponse = EstimateResponseDto.builder()
                .comment("견적 설명")
                .totalAmount(150000)
                .date(LocalDateTime.of(2024, 11, 25, 10, 0, 0))
                .estimateList(List.of(dogResponse))
                .build();

        given(estimateService.getEstimateGroomer(anyLong())).willReturn(mockResponse);

        // When & Then
        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/estimate/1")
                .then()
                .statusCode(200)
                .body("response.comment", equalTo("견적 설명"))
                .body("response.totalAmount", equalTo(150000))
                .body("response.date", equalTo("2024-11-25T10:00:00"))
                .body("response.estimateList", hasSize(1))
                .body("response.estimateList[0].dogProfileResponseDto.dogProfileId", equalTo(1))
                .body("response.estimateList[0].dogProfileResponseDto.profileImage", equalTo("image-url"))
                .body("response.estimateList[0].dogProfileResponseDto.name", equalTo("구름이"))
                .body("response.estimateList[0].description", equalTo("테스트 견적 설명"))
                .body("response.estimateList[0].dogPrice", equalTo(120000))
                .body("response.estimateList[0].aggression", equalTo(false))
                .body("response.estimateList[0].healthIssue", equalTo(true))
                .body("response.estimateList[0].serviceList", hasSize(2))
                .body("response.estimateList[0].serviceList[0].serviceId", equalTo(1))
                .body("response.estimateList[0].serviceList[0].description", equalTo("기본 미용"))
                .body("response.estimateList[0].serviceList[0].price", equalTo(30000))
                .body("response.estimateList[0].serviceList[1].serviceId", equalTo(2))
                .body("response.estimateList[0].serviceList[1].description", equalTo("스페셜 미용"))
                .body("response.estimateList[0].serviceList[1].price", equalTo(70000));

        // 서비스 호출 검증
        verify(estimateService).getEstimateGroomer(anyLong());
    }


    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("견적서 수정 강아지별 상세 조회 테스트")
    void getEstimateDogDetail() {
        List<ServicePriceResponseDto> serviceList = List.of(
                new ServicePriceResponseDto(1L, "기본 미용", 30000),
                new ServicePriceResponseDto(2L, "스페셜 미용", 70000)
        );

        List<FeatureResponseDto> featureList = List.of(
                new FeatureResponseDto("친근함"),
                new FeatureResponseDto("활발함")
        );

        EstimateDogDetailResponseDto mockResponse = EstimateDogDetailResponseDto.builder()
                .dogName("구름이")
                .year(3)
                .month(6)
                .dogWeight(10)
                .gender(Gender.MALE)
                .neutering(Neutering.Y)
                .imageKey("image-key")
                .currentImageKey("current-image-key")
                .styleRefImageKey("style-ref-image-key")
                .species("푸들")
                .aggressionCharge(5000)
                .healthIssueCharge(3000)
                .serviceList(serviceList)
                .aggression(false)
                .healthIssue(true)
                .description("테스트 견적 상세 설명")
                .featureList(featureList)
                .build();

        given(estimateService.getEstimateDogDetail(anyLong(), anyLong())).willReturn(mockResponse);

        // When & Then
        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/estimate/1/1")
                .then()
                .statusCode(200)
                .body("response.dogName", equalTo("구름이"))
                .body("response.year", equalTo(3))
                .body("response.month", equalTo(6))
                .body("response.dogWeight", equalTo(10))
                .body("response.gender", equalTo("MALE"))
                .body("response.neutering", equalTo("Y"))
                .body("response.imageKey", equalTo("image-key"))
                .body("response.currentImageKey", equalTo("current-image-key"))
                .body("response.styleRefImageKey", equalTo("style-ref-image-key"))
                .body("response.species", equalTo("푸들"))
                .body("response.aggressionCharge", equalTo(5000))
                .body("response.healthIssueCharge", equalTo(3000))
                .body("response.aggression", equalTo(false))
                .body("response.healthIssue", equalTo(true))
                .body("response.description", equalTo("테스트 견적 상세 설명"))
                .body("response.featureList", hasSize(2))
                .body("response.featureList[0].description", equalTo("친근함"))
                .body("response.featureList[1].description", equalTo("활발함"))
                .body("response.serviceList", hasSize(2))
                .body("response.serviceList[0].serviceId", equalTo(1))
                .body("response.serviceList[0].description", equalTo("기본 미용"))
                .body("response.serviceList[0].price", equalTo(30000))
                .body("response.serviceList[1].serviceId", equalTo(2))
                .body("response.serviceList[1].description", equalTo("스페셜 미용"))
                .body("response.serviceList[1].price", equalTo(70000));

        verify(estimateService).getEstimateDogDetail(anyLong(), anyLong());
    }


    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("견적서 상세 조회 테스트")
    void getEstimateDetail() {
        // Mock 데이터 생성
        MyEstimateDetailResponseDto mockResponse = MyEstimateDetailResponseDto.builder()
                .status(EstimateStatus.ACCEPTED)
                .description("견적 상세 설명")
                .imageKey("image-key")
                .totalAmount(150000)
                .date(LocalDateTime.of(2024, 11, 25, 10, 0, 0))
                .startChat("chat-room-id")
                .build();

        given(estimateService.getEstimateDetail(anyLong())).willReturn(mockResponse);

        // When & Then
        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/estimate/detail/1")
                .then()
                .statusCode(200)
                .body("response.status", equalTo("ACCEPTED"))
                .body("response.description", equalTo("견적 상세 설명"))
                .body("response.imageKey", equalTo("image-key"))
                .body("response.totalAmount", equalTo(150000))
                .body("response.date", equalTo("2024-11-25T10:00:00"))
                .body("response.startChat", equalTo("chat-room-id"));

        verify(estimateService).getEstimateDetail(anyLong());
    }


    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("내 견적 조회 테스트")
    void getMyEstimate() {
        List<MyEstimateResponseDto> mockResponse = List.of(new MyEstimateResponseDto());
        given(estimateService.getMyEstimate(anyLong())).willReturn(mockResponse);

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/estimate/my/1")
                .then()
                .statusCode(200)
                .body("response", hasSize(1));

        verify(estimateService).getMyEstimate(anyLong());
    }
}
