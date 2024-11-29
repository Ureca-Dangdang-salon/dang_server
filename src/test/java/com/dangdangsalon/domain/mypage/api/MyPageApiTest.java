package com.dangdangsalon.domain.mypage.api;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.groomerprofile.entity.ServiceType;
import com.dangdangsalon.domain.mypage.dto.req.*;
import com.dangdangsalon.domain.mypage.dto.res.*;
import com.dangdangsalon.domain.mypage.service.MyPageCommonService;
import com.dangdangsalon.domain.mypage.service.MyPageDogProfileService;
import com.dangdangsalon.domain.mypage.service.MyPageGroomerService;
import com.dangdangsalon.domain.user.entity.Role;
import io.restassured.RestAssured;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class MyPageApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MyPageCommonService myPageCommonService;

    @MockBean
    private MyPageGroomerService myPageGroomerService;

    @MockBean
    private MyPageDogProfileService myPageDogProfileService;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    // myPageCommonController
    @Test
    @DisplayName("유저 프로필 조회 테스트")
    void getUserProfileTest() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        CommonProfileResponseDto mockResponse = CommonProfileResponseDto.builder()
                .district("종로구")
                .email("test@example.com")
                .build();

        given(myPageCommonService.getUserinfo(anyLong())).willReturn(mockResponse);

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/common")
                .then()
                .statusCode(200)
                .body("response.district", equalTo("종로구"))
                .body("response.email", equalTo("test@example.com"));
    }

    @Test
    @DisplayName("유저 프로필 업데이트 테스트")
    void updateUserProfileTest() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        CommonProfileRequestDto requestDto = CommonProfileRequestDto.builder()
                .imageKey("imageKey")
                .email("updated@example.com")
                .build();

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post("/api/common")
                .then()
                .statusCode(200)
                .body("response", equalTo("유저 정보가 변경되었습니다."));
    }

    // myPageGroomerController
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("미용사 프로필 조회 테스트")
    void getGroomerProfileTest() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        GroomerProfileResponseDto mockProfile = GroomerProfileResponseDto.builder()
                .name("Groomer")
                .role(Role.ROLE_SALON)
                .build();

        given(myPageGroomerService.getGroomerProfilePage(anyLong())).willReturn(mockProfile);

        // When & Then
        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/groomerprofile")
                .then()
                .statusCode(200)
                .body("response.name", equalTo("Groomer"))
                .body("response.role", equalTo("ROLE_SALON"));
    }

    @Test
    @DisplayName("미용사 프로필 상세 조회 테스트")
    void getGroomerProfileDetailsTest() {

        GroomerProfileDetailsResponseDto mockProfile = GroomerProfileDetailsResponseDto.builder()
                .serviceName("길동이네")
                .serviceType(ServiceType.SHOP)
                .build();

        given(myPageGroomerService.getGroomerProfile(anyLong())).willReturn(mockProfile);

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/groomerprofile/1")
                .then()
                .statusCode(200)
                .body("response.serviceName", equalTo("길동이네"))
                .body("response.serviceType", equalTo("SHOP"));
    }

    @Test
    @DisplayName("미용사 프로필 등록 테스트")
    void saveGroomerProfileTest() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        GroomerProfileRequestDto requestDto = GroomerProfileRequestDto.builder()
                .name("New Groomer")
                .phone("1234-1234-1234")
                .build();

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post("/api/groomerprofile")
                .then()
                .statusCode(200)
                .body("response", equalTo("미용사 프로필 등록이 완료되었습니다."));
    }

    @Test
    @DisplayName("미용사 프로필 상세 등록 테스트")
    void saveGroomerProfileDetailTest() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        GroomerProfileDetailsRequestDto requestDto = GroomerProfileDetailsRequestDto.builder()
                .imageKey("imageKey")
                .startMessage("startMessage")
                .build();

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post("/api/groomerprofile/detail")
                .then()
                .statusCode(200)
                .body("response", equalTo("미용사 프로필 상세 정보 등록이 완료되었습니다."));
    }

    @Test
    @DisplayName("미용사 프로필 업데이트 테스트")
    void updateGroomerProfileTest() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        GroomerDetailsUpdateRequestDto requestDto = GroomerDetailsUpdateRequestDto.builder()
                .imageKey("imageKey")
                .startMessage("startMessage")
                .build();

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .put("/api/groomerprofile/1")
                .then()
                .statusCode(200)
                .body("response", equalTo("미용사 프로필 상세 정보 등록이 완료되었습니다."));
    }

    @Test
    @DisplayName("미용사 프로필 삭제 테스트")
    void deleteGroomerProfileTest() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/groomerprofile/1")
                .then()
                .statusCode(200)
                .body("response", equalTo("미용사 프로필 삭제가 완료되었습니다."));
    }

    //MyDogProfileController
    @Test
    @DisplayName("사용자 프로필 조회 테스트")
    void getUserDogProfileTest() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        UserProfileResponseDto mockResponse = UserProfileResponseDto.builder()
                .profileImage("imageKey")
                .name("Test User")
                .build();

        given(myPageDogProfileService.getUserProfile(anyLong())).willReturn(mockResponse);

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/dogprofile")
                .then()
                .statusCode(200)
                .body("response.profileImage", equalTo("imageKey"))
                .body("response.name", equalTo("Test User"));
    }

    @Test
    @DisplayName("반려견 프로필 조회 테스트")
    void getDogProfileTest() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        MyDogProfileResponseDto mockResponse = MyDogProfileResponseDto.builder()
                .species("말티즈")
                .name("Buddy")
                .build();

        given(myPageDogProfileService.getDogProfile(anyLong(), anyLong())).willReturn(mockResponse);

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/dogprofile/1")
                .then()
                .statusCode(200)
                .body("response.species", equalTo("말티즈"))
                .body("response.name", equalTo("Buddy"));
    }

    @Test
    @DisplayName("반려견 프로필 저장 테스트")
    void saveDogProfileTest() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        DogProfileRequestDto requestDto = DogProfileRequestDto.builder()
                .name("Buddy")
                .ageYear(2)
                .ageMonth(4)
                .build();

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post("/api/dogprofile")
                .then()
                .statusCode(200)
                .body("response", equalTo("반려견 프로필 등록이 완료되었습니다."));
    }

    @Test
    @DisplayName("반려견 프로필 수정 테스트")
    void updateDogProfileTest() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        DogProfileRequestDto requestDto = DogProfileRequestDto.builder()
                .name("Max")
                .ageYear(3)
                .ageMonth(4)
                .build();

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .put("/api/dogprofile/1")
                .then()
                .statusCode(200)
                .body("response", equalTo("반려견 프로필 수정이 완료되었습니다."));
    }

    @Test
    @DisplayName("반려견 프로필 삭제 테스트")
    void deleteDogProfileTest() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/dogprofile/1")
                .then()
                .statusCode(200)
                .body("response", equalTo("반려견 프로필 삭제가 완료되었습니다."));
    }
}
