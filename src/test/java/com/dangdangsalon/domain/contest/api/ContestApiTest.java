package com.dangdangsalon.domain.contest.api;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.contest.controller.ContestController;
import com.dangdangsalon.domain.contest.dto.*;
import com.dangdangsalon.domain.contest.entity.Contest;
import com.dangdangsalon.domain.contest.service.ContestPostLikeService;
import com.dangdangsalon.domain.contest.service.ContestPostService;
import com.dangdangsalon.domain.contest.service.ContestService;
import com.dangdangsalon.domain.payment.service.PaymentGetService;
import com.dangdangsalon.util.JwtUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ContestApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContestService contestService;

    @MockBean
    private ContestPostService contestPostService;

    @MockBean
    private ContestPostLikeService contestPostLikeService;

    @MockBean
    private PaymentGetService paymentGetService;

    @MockBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssuredMockMvc.mockMvc(mockMvc);

        given(jwtUtil.isExpired(anyString())).willReturn(false);
        given(jwtUtil.getUserId(anyString())).willReturn(1L);
        given(jwtUtil.getUsername(anyString())).willReturn("testUser");
        given(jwtUtil.getRole(anyString())).willReturn("ROLE_USER");
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("최신 콘테스트 조회")
    void getLatestContest() {
        ContestInfoDto contestInfo = ContestInfoDto.builder()
                .title("최신 콘테스트")
                .startedAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusDays(5))
                .build();
        given(contestService.getLatestContest()).willReturn(contestInfo);

        RestAssuredMockMvc
                .given()
                .cookie("Authorization", "mock.jwt.token")
                .contentType(ContentType.JSON)
                .when()
                .get("/api/contests")
                .then()
                .statusCode(200)
                .body("response.title", equalTo("최신 콘테스트"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("콘테스트 상세 조회")
    void getContestDetails() {
        ContestDetailDto detailDto = ContestDetailDto.create(
                Contest.builder()
                        .title("콘테스트")
                        .description("설명")
                        .startedAt(LocalDateTime.now())
                        .endAt(LocalDateTime.now().plusDays(5))
                        .build(),
                SimpleWinnerInfoDto.builder()
                        .dogName("우승 강아지")
                        .imageUrl("winner.jpg")
                        .build()
        );
        given(contestService.getContestDetails(anyLong())).willReturn(detailDto);

        RestAssuredMockMvc
                .given()
                .cookie("Authorization", "mock.jwt.token")
                .contentType(ContentType.JSON)
                .when()
                .get("/api/contests/1")
                .then()
                .statusCode(200)
                .body("response.title", equalTo("콘테스트"))
                .body("response.recentWinner.dogName", equalTo("우승 강아지"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("콘테스트 참여 여부 확인")
    void checkAlreadyJoined() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        given(contestService.checkUserParticipated(anyLong(), anyLong())).willReturn(true);

        RestAssuredMockMvc
                .given()
                .cookie("Authorization", "mock.jwt.token")
                .contentType(ContentType.JSON)
                .when()
                .get("/api/contests/1/check")
                .then()
                .statusCode(200)
                .body("response.already_participated", equalTo(true));
    }

    @Test
    @DisplayName("지난 콘테스트 우승자 조회")
    void getLastContestWinner() {
        LastContestWinnerDto winnerDto = LastContestWinnerDto.builder()
                .contestId(1L)
                .post(PostInfoDto.builder()
                        .dogName("우승 강아지")
                        .build())
                .build();
        given(contestService.getLastContestWinner()).willReturn(winnerDto);

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/contests/winner/last")
                .then()
                .statusCode(200)
                .body("response.post.dogName", equalTo("우승 강아지"));
    }

    @Test
    @DisplayName("우승자 및 순위 게시글 조회")
    void getWinnerAndRankPost() {
        WinnerRankDto rankDto = WinnerRankDto.builder()
                .contestId(1L)
                .winnerPost(new PostRankDto(1L, 1L, "우승 강아지", "winner.jpg", 100L))
                .rankPosts(List.of(
                        new PostRankDto(2L, 2L, "강아지 2", "dog2.jpg", 90L),
                        new PostRankDto(3L, 3L, "강아지 3", "dog3.jpg", 80L)
                ))
                .build();
        given(contestService.getWinnerAndRankPost()).willReturn(rankDto);

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/contests/winner/rank")
                .then()
                .statusCode(200)
                .body("response.winnerPost.dogName", equalTo("우승 강아지"))
                .body("response.rankPosts", hasSize(2))
                .body("response.rankPosts[0].dogName", equalTo("강아지 2"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("콘테스트 참여 테스트")
    void joinContest() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        ContestJoinRequestDto requestDto = ContestJoinRequestDto.builder()
                .contestId(1L)
                .groomerProfileId(2L)
                .imageUrl("imageUrl")
                .description("description")
                .dogName("멍멍")
                .build();

        RestAssuredMockMvc
                .given()
                .cookie("Authorization", "mock.jwt.token")
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post("/api/posts")
                .then()
                .statusCode(200)
                .body("response", equalTo("콘테스트 참여에 성공했습니다!"));

        verify(contestPostService, times(1)).joinContest(any(ContestJoinRequestDto.class), anyLong());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("포스트 삭제 테스트")
    void deletePost() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        RestAssuredMockMvc
                .given()
                .cookie("Authorization", "mock.jwt.token")
                .when()
                .delete("/api/posts/1")
                .then()
                .statusCode(200)
                .body("response", equalTo("포스트 삭제가 완료되었습니다."));

        verify(contestPostService, times(1)).deletePost(eq(1L), anyLong());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("좋아요 누르기 테스트")
    void likePost() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        RestAssuredMockMvc
                .given()
                .cookie("Authorization", "mock.jwt.token")
                .when()
                .post("/api/posts/1/like")
                .then()
                .statusCode(200)
                .body("response", equalTo("해당 게시물에 좋아요를 눌렀습니다"));

        verify(contestPostLikeService, times(1)).likePost(eq(1L), anyLong());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("좋아요 취소 테스트")
    void unlikePost() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        RestAssuredMockMvc
                .given()
                .cookie("Authorization", "mock.jwt.token")
                .when()
                .delete("/api/posts/1/like")
                .then()
                .statusCode(200)
                .body("response", equalTo("좋아요를 취소했습니다."));

        verify(contestPostLikeService, times(1)).unlikePost(eq(1L), anyLong());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("콘테스트 결제 조회 성공 테스트")
    void testGetPaymentBetweenContest() throws Exception {
        Long userId = 1L;

        ContestPaymentRequestDto requestDto = new ContestPaymentRequestDto(
                LocalDateTime.of(2024, 12, 1, 0, 0),
                LocalDateTime.of(2024, 12, 31, 23, 59)
        );

        List<ContestPaymentDto> responseDto = List.of(
                ContestPaymentDto.builder()
                        .groomerName("Groomer A")
                        .groomerImage("image-key")
                        .paymentDate(LocalDateTime.of(2024, 12, 15, 14, 0))
                        .reservationDate(LocalDateTime.of(2024, 12, 20, 10, 0))
                        .totalAmount(100000)
                        .serviceList(List.of("Service A", "Service B"))
                        .build()
        );

        given(paymentGetService.getContestPayments(any(ContestPaymentRequestDto.class), eq(userId)))
                .willReturn(responseDto);

        RestAssuredMockMvc
                .given()
                .cookie("Authorization", "mock.jwt.token")
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .get("/api/contests/payment")
                .then()
                .statusCode(200)
                .body("response", hasSize(1))
                .body("response[0].groomerName", equalTo("Groomer A"))
                .body("response[0].totalAmount", equalTo(100000))
                .body("response[0].serviceList", hasSize(2))
                .body("response[0].serviceList[0]", equalTo("Service A"))
                .body("response[0].serviceList[1]", equalTo("Service B"));

        verify(paymentGetService, times(1)).getContestPayments(any(ContestPaymentRequestDto.class), eq(userId));
    }
}
