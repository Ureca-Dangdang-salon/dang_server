package com.dangdangsalon.domain.estimate.request.service;

import com.dangdangsalon.domain.estimate.request.dto.DogEstimateRequestDto;
import com.dangdangsalon.domain.estimate.request.dto.EstimateRequestDto;
import com.dangdangsalon.domain.estimate.request.dto.EstimateRequestResponseDto;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.RequestStatus;
import com.dangdangsalon.domain.groomerprofile.entity.*;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerCanServiceRepository;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerServiceAreaRepository;
import com.dangdangsalon.domain.groomerprofile.request.entity.GroomerEstimateRequest;
import com.dangdangsalon.domain.groomerprofile.request.entity.GroomerRequestStatus;
import com.dangdangsalon.domain.groomerprofile.request.repository.GroomerEstimateRequestRepository;
import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import com.dangdangsalon.domain.region.entity.City;
import com.dangdangsalon.domain.region.entity.District;
import com.dangdangsalon.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class GroomerEstimateRequestServiceTest {

    @InjectMocks
    private GroomerEstimateRequestService groomerEstimateRequestService;

    @Mock
    private GroomerServiceAreaRepository groomerServiceAreaRepository;

    @Mock
    private GroomerCanServiceRepository groomerCanServiceRepository;

    @Mock
    private GroomerEstimateRequestRepository groomerEstimateRequestRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("미용사 견적 요청 저장 성공")
    void insertGroomerEstimateRequests_Success() {
        // given
        District district = mock(District.class);
        EstimateRequest estimateRequest = mock(EstimateRequest.class);
        GroomerServiceArea serviceArea = mock(GroomerServiceArea.class);
        GroomerProfile groomerProfile = mock(GroomerProfile.class);
        GroomerCanService canService = mock(GroomerCanService.class);
        GroomerService groomerService = mock(GroomerService.class);

        when(estimateRequest.getServiceType()).thenReturn(ServiceType.ANY);
        when(groomerServiceAreaRepository.findByDistrict(district))
                .thenReturn(Optional.of(List.of(serviceArea)));
        when(serviceArea.getGroomerProfile()).thenReturn(groomerProfile);
        when(groomerProfile.getServiceType()).thenReturn(ServiceType.ANY);
        when(groomerCanServiceRepository.findByGroomerProfile(groomerProfile))
                .thenReturn(List.of(canService));
        when(canService.getGroomerService()).thenReturn(groomerService);
        when(groomerService.getId()).thenReturn(1L);

        EstimateRequestDto estimateRequestDto = createEstimateRequestDto();

        // when
        groomerEstimateRequestService.insertGroomerEstimateRequests(estimateRequest, district, estimateRequestDto);

        // then
        verify(groomerEstimateRequestRepository, times(1)).save(any(GroomerEstimateRequest.class));
    }

    @Test
    @DisplayName("미용사 견적 요청 조회 성공")
    void getEstimateRequest_Success() {
        // given
        Long groomerProfileId = 1L;

        GroomerEstimateRequest groomerEstimateRequest = mock(GroomerEstimateRequest.class);
        EstimateRequest estimateRequest = mock(EstimateRequest.class);
        District district = mock(District.class);
        City city = mock(City.class);
        User user = mock(User.class);

        when(groomerEstimateRequestRepository.findByGroomerProfileId(groomerProfileId))
                .thenReturn(Optional.of(List.of(groomerEstimateRequest)));
        when(groomerEstimateRequest.getEstimateRequest()).thenReturn(estimateRequest);

        when(estimateRequest.getUser()).thenReturn(user);
        when(user.getName()).thenReturn("이민수");
        when(user.getImageKey()).thenReturn("이민수 사진");

        when(estimateRequest.getDistrict()).thenReturn(district);
        when(district.getCity()).thenReturn(city);
        when(district.getName()).thenReturn("강남구");
        when(city.getName()).thenReturn("서울특별시");

        when(estimateRequest.getRequestDate()).thenReturn(LocalDateTime.now());
        when(estimateRequest.getServiceType()).thenReturn(ServiceType.ANY);
        when(estimateRequest.getRequestStatus()).thenReturn(RequestStatus.PENDING);

        when(groomerEstimateRequest.getGroomerRequestStatus()).thenReturn(GroomerRequestStatus.PENDING);

        // when
        List<EstimateRequestResponseDto> result = groomerEstimateRequestService.getEstimateRequest(groomerProfileId);

        // then
        assertThat(result).isNotEmpty();
        verify(groomerEstimateRequestRepository, times(1)).findByGroomerProfileId(groomerProfileId);
    }


    @Test
    @DisplayName("미용사 견적 요청 조회 실패")
    void getEstimateRequest_Failure() {
        // given
        Long groomerProfileId = 1L;
        when(groomerEstimateRequestRepository.findByGroomerProfileId(groomerProfileId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groomerEstimateRequestService.getEstimateRequest(groomerProfileId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 지역에 대한 미용사 정보를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("미용사가 제공할 수 없는 서비스일 경우 요청 저장 실패")
    void insertGroomerEstimateRequests_Failure_ServiceMismatch() {
        // given
        District district = mock(District.class);
        EstimateRequest estimateRequest = mock(EstimateRequest.class);
        when(estimateRequest.getServiceType()).thenReturn(ServiceType.VISIT);
        EstimateRequestDto estimateRequestDto = createEstimateRequestDto();

        GroomerServiceArea serviceArea = mock(GroomerServiceArea.class);
        GroomerProfile groomerProfile = mock(GroomerProfile.class);

        when(groomerServiceAreaRepository.findByDistrict(district))
                .thenReturn(Optional.of(List.of(serviceArea)));
        when(serviceArea.getGroomerProfile()).thenReturn(groomerProfile);
        when(groomerProfile.getServiceType()).thenReturn(ServiceType.VISIT);

        // when
        groomerEstimateRequestService.insertGroomerEstimateRequests(estimateRequest, district, estimateRequestDto);

        // then
        verify(groomerEstimateRequestRepository, never()).save(any(GroomerEstimateRequest.class));
    }

    @Test
    @DisplayName("견적 요청 삭제 미용사")
    void deleteGroomerEstimateRequest_Success() {
        // given
        Long estimateRequestId = 1L;
        GroomerEstimateRequest groomerEstimateRequest = mock(GroomerEstimateRequest.class);

        when(groomerEstimateRequestRepository.findByEstimateRequestId(estimateRequestId))
                .thenReturn(Optional.of(groomerEstimateRequest));

        // when
        groomerEstimateRequestService.deleteGroomerEstimateRequest(estimateRequestId);

        // then
        verify(groomerEstimateRequest, times(1));
    }

    @Test
    @DisplayName("견적 요청 ID가 없을 때 예외 발생")
    void deleteGroomerEstimateRequest_NotFound() {
        // given
        Long estimateRequestId = 1L;

        when(groomerEstimateRequestRepository.findByEstimateRequestId(estimateRequestId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groomerEstimateRequestService.deleteGroomerEstimateRequest(estimateRequestId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("견적 요청을 찾을 수 없습니다: " + estimateRequestId);

        // verify that the repository method was called once
        verify(groomerEstimateRequestRepository, times(1)).findByEstimateRequestId(estimateRequestId);
    }


    private EstimateRequestDto createEstimateRequestDto() {
        DogEstimateRequestDto dogEstimateRequestDto = new DogEstimateRequestDto(
                1L, "currentImage.jpg", "styleRefImage.jpg", true, false, "견적 요청", List.of(1L));
        return new EstimateRequestDto("서울", "강남구", LocalDateTime.now(), "ANY", List.of(dogEstimateRequestDto));
    }

    private List<GroomerCanService> createGroomerCanServices() {
        GroomerCanService canService = mock(GroomerCanService.class);
        GroomerService groomerService = mock(GroomerService.class);

        when(canService.getGroomerService()).thenReturn(groomerService);
        when(groomerService.getId()).thenReturn(1L);

        return List.of(canService);
    }
}
