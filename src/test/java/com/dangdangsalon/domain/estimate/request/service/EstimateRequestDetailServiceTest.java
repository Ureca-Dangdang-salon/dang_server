package com.dangdangsalon.domain.estimate.request.service;

import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.dogprofile.entity.DogProfileFeature;
import com.dangdangsalon.domain.dogprofile.feature.entity.Feature;
import com.dangdangsalon.domain.dogprofile.repository.DogProfileFeatureRepository;
import com.dangdangsalon.domain.estimate.request.dto.EstimateDetailResponseDto;
import com.dangdangsalon.domain.estimate.request.dto.MyEstimateRequestDetailResponseDto;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestService;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestProfilesRepository;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestRepository;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestServiceRepository;
import com.dangdangsalon.domain.estimate.request.service.EstimateRequestDetailService;
import com.dangdangsalon.domain.groomerprofile.entity.ServiceType;
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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class EstimateRequestDetailServiceTest {

    @InjectMocks
    private EstimateRequestDetailService estimateRequestDetailService;

    @Mock
    private EstimateRequestRepository estimateRequestRepository;

    @Mock
    private EstimateRequestProfilesRepository estimateRequestProfilesRepository;

    @Mock
    private EstimateRequestServiceRepository estimateRequestServiceRepository;

    @Mock
    private DogProfileFeatureRepository dogProfileFeatureRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("견적 요청 상세 조회 성공")
    void getEstimateRequestDetail_Success() {
        // given
        Long requestId = 1L;

        EstimateRequest estimateRequest = mock(EstimateRequest.class);
        DogProfile dogProfile = mock(DogProfile.class);
        EstimateRequestProfiles profile = mock(EstimateRequestProfiles.class);
        EstimateRequestService service = mock(EstimateRequestService.class);
        DogProfileFeature feature = mock(DogProfileFeature.class);
        Feature mockFeature = mock(Feature.class);
        GroomerService groomerService = mock(GroomerService.class);
        District district = mock(District.class);
        City city = mock(City.class);
        ServiceType serviceType = mock(ServiceType.class);

        when(estimateRequestRepository.findById(requestId)).thenReturn(Optional.of(estimateRequest));
        when(estimateRequestProfilesRepository.findByEstimateRequest(estimateRequest))
                .thenReturn(Optional.of(List.of(profile)));

        // Mock 지역 정보
        when(estimateRequest.getDistrict()).thenReturn(district);
        when(district.getCity()).thenReturn(city);
        when(city.getName()).thenReturn("서울특별시");
        when(district.getName()).thenReturn("강남구");

        when(estimateRequest.getServiceType()).thenReturn(serviceType);
        when(serviceType.name()).thenReturn("ANY");

        // Mock 사용자 정보
        User user = mock(User.class);
        when(estimateRequest.getUser()).thenReturn(user);
        when(user.getName()).thenReturn("김철수");
        when(user.getImageKey()).thenReturn("user-image-key");

        when(estimateRequest.getRequestDate()).thenReturn(LocalDateTime.of(2024, 1, 1, 0, 0));

        // Mock 프로필과 서비스 정보
        when(profile.getDogProfile()).thenReturn(dogProfile);
        when(dogProfile.getId()).thenReturn(2L);
        when(dogProfile.getImageKey()).thenReturn("image-key");
        when(dogProfile.getName()).thenReturn("구름이");

        when(estimateRequestServiceRepository.findByEstimateRequestProfiles(profile))
                .thenReturn(Optional.of(List.of(service)));

        when(service.getGroomerService()).thenReturn(groomerService);
        when(groomerService.getId()).thenReturn(3L);
        when(groomerService.getDescription()).thenReturn("미용");

        when(feature.getFeature()).thenReturn(mockFeature);
        when(mockFeature.getDescription()).thenReturn("말을 하는 강아지");

        when(dogProfileFeatureRepository.findByDogProfile(dogProfile))
                .thenReturn(Optional.of(List.of(feature)));

        // when
        List<EstimateDetailResponseDto> response = estimateRequestDetailService.getEstimateRequestDetail(requestId);

        // then
        assertThat(response).hasSize(1);
        EstimateDetailResponseDto dto = response.get(0);

        assertThat(dto.getDogProfileResponseDto().getDogProfileId()).isEqualTo(2L);
        assertThat(dto.getDogProfileResponseDto().getProfileImage()).isEqualTo("image-key");
        assertThat(dto.getDogProfileResponseDto().getName()).isEqualTo("구름이");

        assertThat(dto.getServiceList()).hasSize(1);
        assertThat(dto.getServiceList().get(0).getServiceId()).isEqualTo(3L);
        assertThat(dto.getServiceList().get(0).getDescription()).isEqualTo("미용");

        assertThat(dto.getFeatureList()).hasSize(1);
        assertThat(dto.getFeatureList().get(0).getDescription()).isEqualTo("말을 하는 강아지");

        assertThat(dto.getUserProfile().getName()).isEqualTo("김철수");
        assertThat(dto.getUserProfile().getRegion()).isEqualTo("서울특별시 강남구");

        verify(estimateRequestRepository, times(1)).findById(requestId);
        verify(estimateRequestProfilesRepository, times(1)).findByEstimateRequest(estimateRequest);
        verify(estimateRequestServiceRepository, times(1)).findByEstimateRequestProfiles(profile);
        verify(dogProfileFeatureRepository, times(1)).findByDogProfile(dogProfile);
    }


    @Test
    @DisplayName("견적 요청을 찾을 수 없을 때 예외 발생")
    void getEstimateRequestDetail_RequestNotFound() {
        // given
        Long requestId = 1L;

        when(estimateRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> estimateRequestDetailService.getEstimateRequestDetail(requestId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("견적 요청을 찾을 수 없습니다 : " + requestId);

        verify(estimateRequestRepository, times(1)).findById(requestId);
    }

    @Test
    @DisplayName("견적 요청 프로필 정보를 찾을 수 없을 때 예외 발생")
    void getEstimateRequestDetail_ProfileNotFound() {
        // given
        Long requestId = 1L;
        EstimateRequest estimateRequest = mock(EstimateRequest.class);

        when(estimateRequestRepository.findById(requestId)).thenReturn(Optional.of(estimateRequest));
        when(estimateRequestProfilesRepository.findByEstimateRequest(estimateRequest)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> estimateRequestDetailService.getEstimateRequestDetail(requestId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("견적 요청 프로필 정보를 찾을 수 없습니다");

        verify(estimateRequestRepository, times(1)).findById(requestId);
        verify(estimateRequestProfilesRepository, times(1)).findByEstimateRequest(estimateRequest);
    }

    @Test
    @DisplayName("내 견적 요청 상세 조회 성공")
    void getMyEstimateDetailRequest_Success() {
        // given
        Long requestId = 1L;

        EstimateRequest estimateRequest = mock(EstimateRequest.class);
        EstimateRequestProfiles profile = mock(EstimateRequestProfiles.class);
        DogProfile dogProfile = mock(DogProfile.class);
        EstimateRequestService service = mock(EstimateRequestService.class);
        GroomerService groomerService = mock(GroomerService.class);

        when(estimateRequestRepository.findById(requestId)).thenReturn(Optional.of(estimateRequest));
        when(estimateRequestProfilesRepository.findByEstimateRequest(estimateRequest))
                .thenReturn(Optional.of(List.of(profile)));

        when(profile.getDogProfile()).thenReturn(dogProfile);
        when(dogProfile.getImageKey()).thenReturn("dog-image-key");
        when(dogProfile.getName()).thenReturn("멍멍이");
        when(profile.isAggression()).thenReturn(false);
        when(profile.isHealthIssue()).thenReturn(true);
        when(profile.getDescription()).thenReturn("특별 관리 필요");

        when(estimateRequestServiceRepository.findByEstimateRequestProfiles(profile))
                .thenReturn(Optional.of(List.of(service)));

        when(service.getGroomerService()).thenReturn(groomerService);
        when(groomerService.getId()).thenReturn(3L);
        when(groomerService.getDescription()).thenReturn("서비스 설명");

        // when
        List<MyEstimateRequestDetailResponseDto> response =
                estimateRequestDetailService.getMyEstimateDetailRequest(requestId);

        // then
        assertThat(response).hasSize(1);
        MyEstimateRequestDetailResponseDto dto = response.get(0);

        assertThat(dto.getImageKey()).isEqualTo("dog-image-key");
        assertThat(dto.getDogName()).isEqualTo("멍멍이");
        assertThat(dto.isAggression()).isFalse();
        assertThat(dto.isHealthIssue()).isTrue();
        assertThat(dto.getDescription()).isEqualTo("특별 관리 필요");
        assertThat(dto.getServiceList()).hasSize(1);
        assertThat(dto.getServiceList().get(0).getServiceId()).isEqualTo(3L);
        assertThat(dto.getServiceList().get(0).getDescription()).isEqualTo("서비스 설명");

        verify(estimateRequestRepository, times(1)).findById(requestId);
        verify(estimateRequestProfilesRepository, times(1)).findByEstimateRequest(estimateRequest);
    }

    @Test
    @DisplayName("내 견적 요청 아이디로 요청을 찾을 수 없을 때 예외 발생")
    void getMyEstimateDetailRequest_RequestNotFound() {
        // given
        Long requestId = 1L;

        when(estimateRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> estimateRequestDetailService.getMyEstimateDetailRequest(requestId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("견적 요청을 찾을 수 없습니다 : " + requestId);
    }

    @Test
    @DisplayName("내 견적 요청 프로필을 찾을 수 없을 때 예외 발생")
    void getMyEstimateDetailRequest_ProfileNotFound() {
        // given
        Long requestId = 1L;
        EstimateRequest estimateRequest = mock(EstimateRequest.class);

        when(estimateRequestRepository.findById(requestId)).thenReturn(Optional.of(estimateRequest));
        when(estimateRequestProfilesRepository.findByEstimateRequest(estimateRequest))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> estimateRequestDetailService.getMyEstimateDetailRequest(requestId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("견적 요청 프로필 정보를 찾을 수 없습니다");
    }
}
