package com.dangdangsalon.domain.estimate.service;

import com.dangdangsalon.domain.chat.service.ChatMessageService;
import com.dangdangsalon.domain.chat.service.ChatRoomService;
import com.dangdangsalon.domain.chat.service.ChatService;
import com.dangdangsalon.domain.estimate.dto.DogPriceRequestDto;
import com.dangdangsalon.domain.estimate.dto.EstimateUpdateRequestDto;
import com.dangdangsalon.domain.estimate.dto.ServiceRequestDto;
import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestService;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestProfilesRepository;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestServiceRepository;
import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import com.dangdangsalon.domain.groomerservice.repository.GroomerServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

class EstimateUpdateServiceTest {

    @Mock
    private EstimateRepository estimateRepository;

    @Mock
    private GroomerServiceRepository groomerServiceRepository;

    @Mock
    private EstimateRequestProfilesRepository estimateRequestProfilesRepository;

    @Mock
    private EstimateRequestServiceRepository estimateRequestServiceRepository;

    @Mock
    private ChatRoomService chatRoomService;

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private EstimateUpdateService estimateUpdateService;

    private Estimate mockEstimate;
    private EstimateRequestProfiles mockProfile;
    private GroomerService mockService;
    private EstimateRequestService mockRequestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockProfile = EstimateRequestProfiles.builder().build();
        ReflectionTestUtils.setField(mockProfile, "id", 1L);

        EstimateRequest mockEstimateRequest = EstimateRequest.builder()
                .build();
        ReflectionTestUtils.setField(mockEstimateRequest, "id", 1L);

        mockService = GroomerService.builder()
                .build();
        ReflectionTestUtils.setField(mockService, "id", 1L);

        mockRequestService = EstimateRequestService.builder()
                .build();

        mockEstimate = Estimate.builder()
                .description("Original description")
                .totalAmount(10000)
                .estimateRequest(mockEstimateRequest)
                .build();
        ReflectionTestUtils.setField(mockEstimate, "id", 1L);
    }


    @Test
    @DisplayName("견적서 업데이트 테스트 - 성공")
    void testUpdateEstimate_Success() {

        ServiceRequestDto serviceDto = ServiceRequestDto.builder()
                .serviceId(1L)
                .price(5000)
                .build();

        DogPriceRequestDto dogPriceDto = DogPriceRequestDto.builder()
                .dogProfileId(1L)
                .aggressionCharge(1000)
                .healthIssueCharge(2000)
                .serviceList(List.of(serviceDto))
                .build();

        EstimateUpdateRequestDto requestDto = EstimateUpdateRequestDto.builder()
                .estimateId(1L)
                .description("Updated description")
                .totalAmount(15000)
                .date(LocalDateTime.now())
                .dogPriceList(List.of(dogPriceDto))
                .build();

        given(estimateRepository.findById(1L)).willReturn(Optional.of(mockEstimate));
        given(estimateRequestProfilesRepository.findByDogProfileIdAndEstimateRequestId(1L, 1L))
                .willReturn(Optional.of(mockProfile));
        given(groomerServiceRepository.findById(1L)).willReturn(Optional.of(mockService));
        given(estimateRequestServiceRepository.findByEstimateRequestProfilesAndGroomerService(mockProfile, mockService))
                .willReturn(Optional.of(mockRequestService));

        estimateUpdateService.updateEstimate(requestDto);


        verify(estimateRepository, times(1)).findById(1L);
        verify(estimateRequestProfilesRepository, times(1)).findByDogProfileIdAndEstimateRequestId(1L, 1L);
        verify(groomerServiceRepository, times(1)).findById(1L);
        verify(estimateRequestServiceRepository, times(1))
                .findByEstimateRequestProfilesAndGroomerService(mockProfile, mockService);
    }

    @Test
    @DisplayName("견적서 업데이트 테스트 - 실패 (견적서 없음)")
    void testUpdateEstimate_Fail_EstimateNotFound() {

        EstimateUpdateRequestDto requestDto = EstimateUpdateRequestDto.builder()
                .estimateId(99L)
                .build();

        given(estimateRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> estimateUpdateService.updateEstimate(requestDto));

        verify(estimateRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("견적서 상태 거부 업데이트 테스트 - 성공")
    void testRejectedEstimate_Success() {
        given(estimateRepository.findById(1L)).willReturn(Optional.of(mockEstimate));

        estimateUpdateService.rejectedEstimate(1L);

        assertThat(mockEstimate.getStatus()).isEqualTo(EstimateStatus.REJECTED);
        verify(estimateRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("견적서 상태 거부 업데이트 테스트 - 실패 (견적서 없음)")
    void testRejectedEstimate_Fail_EstimateNotFound() {
        given(estimateRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> estimateUpdateService.rejectedEstimate(99L));

        verify(estimateRepository, times(1)).findById(99L);
    }
}
