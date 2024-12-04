package com.dangdangsalon.domain.orders.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dangdangsalon.domain.orders.dto.OrdersRequestDto;
import com.dangdangsalon.domain.orders.dto.OrdersResponseDto;
import com.dangdangsalon.domain.orders.service.OrdersService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

@WebMvcTest(OrdersController.class)
@ActiveProfiles("test")
@MockBean(JpaMetamodelMappingContext.class)
class OrdersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrdersService ordersService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("주문 등록 성공 테스트")
    void testInsertOrders() throws Exception {
        // Given
        Long estimateId = 1L;

        OrdersRequestDto requestDto = OrdersRequestDto.builder()
                .amount(120000)
                .tossOrderId("19_XR8395y-HtJQb7dsds55L")
                .orderName("샘플 주문")
                .build();

        OrdersResponseDto responseDto = OrdersResponseDto.builder()
                .amount(120000)
                .tossOrderId("19_XR8395y-HtJQb7dsds55L")
                .requestedAt(LocalDateTime.of(2023, 11, 26, 10, 30))
                .orderName("샘플 주문")
                .build();

        given(ordersService.insertOrders(eq(estimateId), any(OrdersRequestDto.class)))
                .willReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/orders/{estimateId}", estimateId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)) // ObjectMapper 사용
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.amount").value(120000))
                .andExpect(jsonPath("$.response.tossOrderId").value("19_XR8395y-HtJQb7dsds55L"))
                .andExpect(jsonPath("$.response.orderName").value("샘플 주문"))
                .andExpect(jsonPath("$.response.requestedAt").exists());
    }
}
