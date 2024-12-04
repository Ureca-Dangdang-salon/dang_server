package com.dangdangsalon.domain.estimate.request.api;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.dogprofile.dto.DogProfileResponseDto;
import com.dangdangsalon.domain.estimate.request.dto.*;
import com.dangdangsalon.domain.estimate.request.service.EstimateRequestDetailService;
import com.dangdangsalon.domain.estimate.request.service.EstimateRequestServices;
import com.dangdangsalon.domain.estimate.request.service.GroomerEstimateRequestService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class EstimateRequestApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EstimateRequestServices estimateRequestServices;

    @MockBean
    private GroomerEstimateRequestService groomerEstimateRequestService;

    @MockBean
    private EstimateRequestDetailService estimateRequestDetailService;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("견적 요청 등록 테스트")
    void createEstimateRequest() {
        CustomOAuth2User mockLoginUser = Mockito.mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        EstimateRequestDto requestDto = new EstimateRequestDto();
        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post("/api/estimaterequest")
                .then()
                .statusCode(200)
                .body("response", equalTo("견적 요청 등록에 성공하였습니다."));

        verify(estimateRequestServices).insertEstimateRequest(Mockito.any(EstimateRequestDto.class), anyLong());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("미용사에게 전달된 견적 요청 조회 테스트")
    void getEstimateRequests() {
        EstimateRequestResponseDto mockResponseDto = EstimateRequestResponseDto.builder()
                .estimateId(1L)
                .name("이민수")
                .date(LocalDate.of(2024, 11, 25))
                .serviceType("VISIT")
                .region("서울특별시 강남구")
                .imageKey("image-key")
                .estimateRequestStatus("PENDING")
                .groomerEstimateRequestStatus("ACCEPTED")
                .build();

        List<EstimateRequestResponseDto> mockResponse = List.of(mockResponseDto);

        given(groomerEstimateRequestService.getEstimateRequest(anyLong())).willReturn(mockResponse);

        // When & Then
        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/estimaterequest/1")
                .then()
                .statusCode(200)
                .body("response", hasSize(1)) // 리스트 크기 확인
                .body("response[0].estimateId", equalTo(1))
                .body("response[0].name", equalTo("이민수"))
                .body("response[0].date", equalTo("2024-11-25"))
                .body("response[0].serviceType", equalTo("VISIT"))
                .body("response[0].region", equalTo("서울특별시 강남구"))
                .body("response[0].imageKey", equalTo("image-key"))
                .body("response[0].estimateRequestStatus", equalTo("PENDING"))
                .body("response[0].groomerEstimateRequestStatus", equalTo("ACCEPTED"));

        verify(groomerEstimateRequestService).getEstimateRequest(anyLong());
    }


    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("견적 요청 상세 정보 조회 테스트")
    void getEstimateRequestDetail() {
        // Mock 데이터 생성
        DogProfileResponseDto dogProfileResponseDto = DogProfileResponseDto.builder()
                .dogProfileId(1L)
                .name("구름이")
                .profileImage("profile-image-key")
                .build();

        List<ServiceResponseDto> serviceList = List.of(
                new ServiceResponseDto(1L, "기본 미용"),
                new ServiceResponseDto(2L, "스페셜 미용")
        );

        List<FeatureResponseDto> featureList = List.of(
                new FeatureResponseDto("친근함"),
                new FeatureResponseDto("활발함")
        );

        EstimateDetailResponseDto mockResponse = EstimateDetailResponseDto.builder()
                .dogProfileResponseDto(dogProfileResponseDto)
                .currentPhotoKey("current-photo-key")
                .styleRefPhotoKey("style-ref-photo-key")
                .aggression(false)
                .healthIssue(true)
                .description("견적 요청 상세 설명")
                .serviceList(serviceList)
                .featureList(featureList)
                .build();

        List<EstimateDetailResponseDto> mockDetailResponse = List.of(mockResponse);

        given(estimateRequestDetailService.getEstimateRequestDetail(anyLong())).willReturn(mockDetailResponse);

        // When & Then
        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/estimaterequest/detail/1")
                .then()
                .statusCode(200)
                .body("response", hasSize(1))
                .body("response[0].dogProfileResponseDto.dogProfileId", equalTo(1))
                .body("response[0].dogProfileResponseDto.name", equalTo("구름이"))
                .body("response[0].dogProfileResponseDto.profileImage", equalTo("profile-image-key"))
                .body("response[0].currentPhotoKey", equalTo("current-photo-key"))
                .body("response[0].styleRefPhotoKey", equalTo("style-ref-photo-key"))
                .body("response[0].aggression", equalTo(false))
                .body("response[0].healthIssue", equalTo(true))
                .body("response[0].description", equalTo("견적 요청 상세 설명"))
                .body("response[0].serviceList", hasSize(2))
                .body("response[0].serviceList[0].serviceId", equalTo(1))
                .body("response[0].serviceList[0].description", equalTo("기본 미용"))
                .body("response[0].serviceList[1].serviceId", equalTo(2))
                .body("response[0].serviceList[1].description", equalTo("스페셜 미용"))
                .body("response[0].featureList", hasSize(2))
                .body("response[0].featureList[0].description", equalTo("친근함"))
                .body("response[0].featureList[1].description", equalTo("활발함"));

        verify(estimateRequestDetailService).getEstimateRequestDetail(anyLong());
    }


    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("미용사 견적 요청 삭제 테스트")
    void deleteEstimateRequest() {
        RestAssuredMockMvc
                .given()
                .when()
                .delete("/api/estimaterequest/1")
                .then()
                .statusCode(200)
                .body("response", equalTo("견적 요청 삭제에 성공하였습니다."));

        verify(groomerEstimateRequestService).deleteGroomerEstimateRequest(anyLong());
    }


    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("유저 본인의 견적 요청 조회 테스트")
    void getMyEstimateRequests() {
        CustomOAuth2User mockLoginUser = Mockito.mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        List<MyEstimateRequestResponseDto> mockResponse = List.of(new MyEstimateRequestResponseDto());
        given(estimateRequestServices.getMyEstimateRequest(anyLong())).willReturn(mockResponse);

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/estimaterequest/my")
                .then()
                .statusCode(200)
                .body("response", hasSize(1));

        verify(estimateRequestServices).getMyEstimateRequest(anyLong());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("유저 본인의 견적 요청 상세 조회 테스트 (채팅)")
    void getMyEstimateRequestDetail() {

        List<MyEstimateRequestDetailResponseDto> mockResponse = List.of(new MyEstimateRequestDetailResponseDto());
        given(estimateRequestDetailService.getMyEstimateDetailRequest(anyLong())).willReturn(mockResponse);

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/estimaterequest/my/detail/1")
                .then()
                .statusCode(200)
                .body("response", hasSize(1));

        verify(estimateRequestDetailService).getMyEstimateDetailRequest(anyLong());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("견적 요청 상태 CANCEL 변경 테스트")
    void stopEstimate() {
        RestAssuredMockMvc
                .given()
                .when()
                .put("/api/estimaterequest/1/stop")
                .then()
                .statusCode(200)
                .body("response", equalTo("견적 그만 받기에 성공하였습니다."));

        verify(estimateRequestServices).stopEstimate(anyLong());
    }
}
