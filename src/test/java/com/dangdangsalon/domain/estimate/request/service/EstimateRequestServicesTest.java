package com.dangdangsalon.domain.estimate.request.service;

import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.estimate.request.dto.EstimateRequestDto;
import com.dangdangsalon.domain.estimate.request.dto.MyEstimateRequestResponseDto;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.estimate.request.entity.RequestStatus;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestRepository;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerProfileRepository;
import com.dangdangsalon.domain.region.entity.District;
import com.dangdangsalon.domain.region.repository.DistrictRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@DisplayName("EstimateRequestServicesTest")
class EstimateRequestServicesTest {

    @InjectMocks
    private EstimateRequestServices estimateRequestServices;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DistrictRepository districtRepository;

    @Mock
    private EstimateRequestInsertService estimateRequestInsertService;

    @Mock
    private GroomerEstimateRequestService groomerEstimateRequestService;

    @Mock
    private GroomerProfileRepository groomerProfileRepository;

    @Mock
    private EstimateRequestRepository estimateRequestRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("견적 요청 등록 성공")
    void testInsertEstimateRequest_Success() {
        // given
        Long userId = 1L;
        Long districtId = 1L;
        User mockUser = mock(User.class);
        District mockDistrict = mock(District.class);
        GroomerProfile mockGroomerProfile = mock(GroomerProfile.class);
        EstimateRequest mockEstimateRequest = mock(EstimateRequest.class);

        EstimateRequestDto estimateRequestDto = new EstimateRequestDto(
                1L,
                districtId,
                LocalDateTime.now(),
                "ANY",
                Collections.emptyList()
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(districtRepository.findById(estimateRequestDto.getDistrictId())).willReturn(Optional.of(mockDistrict));
        given(groomerProfileRepository.findById(districtId)).willReturn(Optional.of(mockGroomerProfile));
        given(estimateRequestInsertService.insertEstimateRequest(estimateRequestDto, mockUser, mockDistrict))
                .willReturn(mockEstimateRequest);

        // when
        estimateRequestServices.insertEstimateRequest(estimateRequestDto, userId);

        // then
        verify(userRepository, times(1)).findById(userId);
        verify(districtRepository, times(1)).findById(estimateRequestDto.getDistrictId());
        verify(groomerProfileRepository, times(1)).findById(districtId);
        verify(estimateRequestInsertService, times(1)).insertEstimateRequest(estimateRequestDto, mockUser, mockDistrict);
        verify(groomerEstimateRequestService, times(1))
                .insertGroomerEstimateRequestForSpecificGroomer(mockEstimateRequest, mockGroomerProfile, estimateRequestDto);
    }


    @Test
    @DisplayName("견적 요청 등록 실패 - 유저 ID 없음")
    void testInsertEstimateRequest_UserNotFound() {
        // given
        Long userId = 1L;
        EstimateRequestDto estimateRequestDto = new EstimateRequestDto(
                1L,
                1L,
                LocalDateTime.now(),
                "ANY",
                Collections.emptyList()
        );

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                estimateRequestServices.insertEstimateRequest(estimateRequestDto, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유저를 찾을 수 없습니다: " + userId);

        verify(userRepository, times(1)).findById(userId);
        verify(districtRepository, never()).findById(any());
        verify(estimateRequestInsertService, never()).insertEstimateRequest(any(), any(), any());
        verify(groomerEstimateRequestService, never()).insertGroomerEstimateRequests(any(), any(), any());
    }

    @Test
    @DisplayName("내 견적 요청 조회 성공")
    void testGetMyEstimateRequest_Success() {
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();

        DogProfile dogProfile1 = mock(DogProfile.class);
        when(dogProfile1.getName()).thenReturn("멍멍이");

        EstimateRequestProfiles estimateRequestProfile = mock(EstimateRequestProfiles.class);
        when(estimateRequestProfile.getDogProfile()).thenReturn(dogProfile1);

        EstimateRequest estimateRequest = mock(EstimateRequest.class);
        when(estimateRequest.getId()).thenReturn(1L);
        when(estimateRequest.getRequestDate()).thenReturn(now);
        when(estimateRequest.getRequestStatus()).thenReturn(RequestStatus.PENDING);
        when(estimateRequest.getEstimateRequestProfiles()).thenReturn(Collections.singletonList(estimateRequestProfile));

        given(estimateRequestRepository.findByUserId(userId))
                .willReturn(Optional.of(Collections.singletonList(estimateRequest)));

        // when
        List<MyEstimateRequestResponseDto> result = estimateRequestServices.getMyEstimateRequest(userId);

        // then
        assertThat(result).hasSize(1);
        MyEstimateRequestResponseDto responseDto = result.get(0);
        assertThat(responseDto.getRequestId()).isEqualTo(1L);
        assertThat(responseDto.getDate()).isEqualTo(now);
        assertThat(responseDto.getStatus()).isEqualTo(RequestStatus.PENDING);
        assertThat(responseDto.getDogList()).hasSize(1);
        assertThat(responseDto.getDogList().get(0).getDogName()).isEqualTo("멍멍이");

        verify(estimateRequestRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("내 견적 요청 조회 시 견적 요청 없음")
    void testGetMyEstimateRequest_NoRequests() {
        // given
        Long userId = 1L;
        given(estimateRequestRepository.findByUserId(userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> estimateRequestServices.getMyEstimateRequest(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원의 견적 요청을 찾을 수 없습니다: " + userId);

        verify(estimateRequestRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("견적 요청 그만 받기 성공")
    void testStopEstimate_Success() {
        // given
        Long requestId = 1L;
        EstimateRequest estimateRequest = mock(EstimateRequest.class);

        given(estimateRequestRepository.findById(requestId))
                .willReturn(Optional.of(estimateRequest));

        // when
        estimateRequestServices.stopEstimate(requestId);

        // then
        verify(estimateRequestRepository, times(1)).findById(requestId);
        verify(estimateRequest, times(1)).updateRequestStatus(RequestStatus.CANCEL);
    }

    @Test
    @DisplayName("견적 요청 그만 받기 견적 요청 없음")
    void testStopEstimate_RequestNotFound() {
        // given
        Long requestId = 1L;
        given(estimateRequestRepository.findById(requestId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> estimateRequestServices.stopEstimate(requestId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("견적 요청을 찾을 수 없습니다: " + requestId);

        verify(estimateRequestRepository, times(1)).findById(requestId);
    }

    @Test
    @DisplayName("견적 요청 등록 성공 - GroomerProfileId 없음")
    void testInsertEstimateRequest_SuccessWithoutGroomerProfileId() {
        // given
        Long userId = 1L;
        Long districtId = 1L;
        User mockUser = mock(User.class);
        District mockDistrict = mock(District.class);
        EstimateRequest mockEstimateRequest = mock(EstimateRequest.class);

        EstimateRequestDto estimateRequestDto = new EstimateRequestDto(
                null,
                districtId,
                LocalDateTime.now(),
                "ANY",
                Collections.emptyList()
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(districtRepository.findById(districtId)).willReturn(Optional.of(mockDistrict));
        given(estimateRequestInsertService.insertEstimateRequest(estimateRequestDto, mockUser, mockDistrict))
                .willReturn(mockEstimateRequest);

        // when
        estimateRequestServices.insertEstimateRequest(estimateRequestDto, userId);

        // then
        verify(userRepository).findById(userId);
        verify(districtRepository).findById(districtId);
        verify(estimateRequestInsertService).insertEstimateRequest(estimateRequestDto, mockUser, mockDistrict);
        verify(groomerEstimateRequestService).insertGroomerEstimateRequests(mockEstimateRequest, mockDistrict, estimateRequestDto);

        verifyNoInteractions(groomerProfileRepository);
    }
}
