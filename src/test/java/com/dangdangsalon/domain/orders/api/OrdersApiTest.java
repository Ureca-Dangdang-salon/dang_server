package com.dangdangsalon.domain.orders.api;

import static org.mockito.BDDMockito.*;
import static org.hamcrest.Matchers.equalTo;

import com.dangdangsalon.domain.orders.dto.OrdersRequestDto;
import com.dangdangsalon.domain.orders.dto.OrdersResponseDto;
import com.dangdangsalon.domain.orders.service.OrdersService;
import io.restassured.RestAssured;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class OrdersApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrdersService ordersService;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Test
    @DisplayName("주문 등록 테스트")
    void insertOrders() {
        OrdersRequestDto requestDto = OrdersRequestDto.builder()
                .amount(50000)
                .tossOrderId("T123456789")
                .orderName("테스트 주문")
                .build();

        OrdersResponseDto responseDto = OrdersResponseDto.builder()
                .amount(50000)
                .tossOrderId("T123456789")
                .requestedAt(LocalDateTime.now())
                .orderName("테스트 주문")
                .build();

        given(ordersService.insertOrders(eq(1L), any(OrdersRequestDto.class))).willReturn(responseDto);

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post("/api/orders/1")
                .then()
                .statusCode(200)
                .body("response.amount", equalTo(50000))
                .body("response.tossOrderId", equalTo("T123456789"))
                .body("response.orderName", equalTo("테스트 주문"));
    }
}
