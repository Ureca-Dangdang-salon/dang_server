package com.dangdangsalon.domain.chat.api;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.any;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.domain.chat.dto.ChatRoomDetailDto;
import com.dangdangsalon.domain.chat.dto.ChatRoomListDto;
import com.dangdangsalon.domain.chat.dto.CreateChatRoomRequestDto;
import com.dangdangsalon.domain.chat.dto.CreateChatRoomResponseDto;
import com.dangdangsalon.domain.chat.service.ChatMessageService;
import com.dangdangsalon.domain.chat.service.ChatRoomService;
import com.dangdangsalon.util.JwtUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ChatApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatRoomService chatRoomService;

    @MockBean
    private ChatMessageService chatMessageService;

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
    @DisplayName("채팅방 생성 요청 성공 테스트")
    void createChatRoomTest() {
        CreateChatRoomRequestDto requestDto = new CreateChatRoomRequestDto(1L);

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(requestDto)
                .cookie("Authorization", "mock.jwt.token")
                .when()
                .post("/api/chatrooms")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("채팅방 목록 조회 성공 테스트")
    void getChatRoomListTest() {
        ChatRoomListDto roomListDto = ChatRoomListDto.builder()
                .roomId(1L)
                .lastMessage("Last message")
                .unreadCount(5)
                .build();

        given(chatRoomService.getChatRoomList(anyLong(), anyString())).willReturn(List.of(roomListDto));

        RestAssuredMockMvc
                .given()
                .cookie("Authorization", "mock.jwt.token")
                .when()
                .get("/api/chatrooms")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("response.size()", greaterThanOrEqualTo(0))
                .body("response[0].roomId", equalTo(1))
                .body("response[0].lastMessage", equalTo("Last message"))
                .body("response[0].unreadCount", equalTo(5));
    }

    @Test
    @DisplayName("채팅방 상세 정보 조회 성공 테스트")
    void getChatRoomDetailTest() {
        ChatRoomDetailDto detailDto = ChatRoomDetailDto.builder()
                .roomId(1L)
                .build();

        given(chatRoomService.getChatRoomDetail(anyLong(), anyString())).willReturn(detailDto);

        RestAssuredMockMvc
                .given()
                .cookie("Authorization", "mock.jwt.token")
                .when()
                .get("/api/chatrooms/1/enter")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("response.roomId", equalTo(1));
    }

    @Test
    @DisplayName("이전 메시지 조회 성공 테스트")
    void getPreviousMessagesTest() {
        ChatMessageDto messageDto = ChatMessageDto.builder()
                .messageId("1L")
                .messageText("Hello")
                .build();

        given(chatMessageService.getPreviousMessages(anyLong(), anyLong())).willReturn(List.of(messageDto));

        RestAssuredMockMvc
                .given()
                .cookie("Authorization", "mock.jwt.token")
                .when()
                .get("/api/chatrooms/1/messages/previous")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("response.size()", greaterThanOrEqualTo(0))
                .body("response[0].messageId", equalTo("1L"))
                .body("response[0].messageText", equalTo("Hello"));
    }

    @Test
    @DisplayName("채팅방 종료 성공 테스트")
    void exitChatRoomTest() {
        RestAssuredMockMvc
                .given()
                .cookie("Authorization", "mock.jwt.token")
                .when()
                .post("/api/chatrooms/1/exit")
                .then()
                .statusCode(HttpStatus.OK.value());
    }
}
