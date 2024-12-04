package com.dangdangsalon.domain.estimate.service;

import com.dangdangsalon.domain.dogprofile.entity.*;
import com.dangdangsalon.domain.dogprofile.feature.entity.Feature;
import com.dangdangsalon.domain.dogprofile.repository.DogProfileFeatureRepository;
import com.dangdangsalon.domain.estimate.dto.*;
import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.estimate.request.dto.FeatureResponseDto;
import com.dangdangsalon.domain.estimate.request.dto.ServicePriceResponseDto;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestService;
import com.dangdangsalon.domain.estimate.request.entity.RequestStatus;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestProfilesRepository;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestRepository;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestServiceRepository;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerDetails;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerProfileRepository;
import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import com.dangdangsalon.domain.groomerservice.repository.GroomerServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@DisplayName("EstimateService 테스트")
class EstimateServiceTest {

    @InjectMocks
    private EstimateService estimateService;

    @Mock
    private EstimateRequestRepository estimateRequestRepository;

    @Mock
    private GroomerProfileRepository groomerProfileRepository;

    @Mock
    private EstimateRepository estimateRepository;

    @Mock
    private GroomerServiceRepository groomerServiceRepository;

    @Mock
    private EstimateNotificationService estimateNotificationService;

    @Mock
    private EstimateRequestProfilesRepository estimateRequestProfilesRepository;

    @Mock
    private DogProfileFeatureRepository dogProfileFeatureRepository;

    @Mock
    private EstimateRequestServiceRepository estimateRequestServiceRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("견적서 등록 성공")
    void testInsertEstimate_Success() {
        // Given
        EstimateWriteRequestDto requestDto = mock(EstimateWriteRequestDto.class);
        EstimateRequest estimateRequest = mock(EstimateRequest.class);
        GroomerProfile groomerProfile = mock(GroomerProfile.class);
        Estimate estimate = mock(Estimate.class);

        given(requestDto.getRequestId()).willReturn(1L);
        given(requestDto.getGroomerProfileId()).willReturn(2L);
        given(requestDto.getDescription()).willReturn("Test description");
        given(requestDto.getImageKey()).willReturn("test-image-key");
        given(requestDto.getTotalAmount()).willReturn(10000);

        given(estimateRequestRepository.findById(1L)).willReturn(Optional.of(estimateRequest));
        given(estimateRequest.getRequestStatus()).willReturn(RequestStatus.COMPLETED);

        given(groomerProfileRepository.findById(2L)).willReturn(Optional.of(groomerProfile));
        given(estimateRepository.save(any(Estimate.class))).willReturn(estimate);

        // When
        estimateService.insertEstimate(requestDto);

        // Then
        verify(estimateRepository, times(1)).save(any(Estimate.class));
        verify(estimateNotificationService, times(1))
                .sendNotificationToUser(eq(estimateRequest), any(Estimate.class), eq(groomerProfile));
    }

    @Test
    @DisplayName("견적서 등록 실패 - 견적 요청 없음")
    void testInsertEstimate_EstimateRequestNotFound() {
        // given
        EstimateWriteRequestDto requestDto = mock(EstimateWriteRequestDto.class);
        given(requestDto.getRequestId()).willReturn(1L);
        given(estimateRequestRepository.findById(requestDto.getRequestId())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> estimateService.insertEstimate(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("견적 요청을 찾을 수 없습니다 : " + requestDto.getRequestId());
    }

    @Test
    @DisplayName("견적서 수정 조회 성공")
    void testGetEstimateGroomer_Success() {
        // given
        Long estimateId = 1L;
        Estimate estimate = mock(Estimate.class);
        EstimateRequest estimateRequest = mock(EstimateRequest.class);

        given(estimate.getEstimateRequest()).willReturn(estimateRequest);
        given(estimateRequest.getId()).willReturn(1L);

        given(estimateRepository.findById(estimateId)).willReturn(Optional.of(estimate));
        given(estimateRequestProfilesRepository.findByEstimateRequestId(1L)).willReturn(Optional.of(Collections.emptyList()));

        // when
        EstimateResponseDto responseDto = estimateService.getEstimateGroomer(estimateId);

        // then
        assertThat(responseDto).isNotNull();
        verify(estimateRepository, times(1)).findById(estimateId);
    }

    @Test
    @DisplayName("견적서 수정 조회 실패 - 견적서 없음")
    void testGetEstimateGroomer_EstimateNotFound() {
        // given
        Long estimateId = 1L;
        given(estimateRepository.findById(estimateId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> estimateService.getEstimateGroomer(estimateId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("견적서를 찾을 수 없습니다: " + estimateId);
    }

    @Test
    @DisplayName("내 견적 조회 성공")
    void testGetMyEstimate_Success() {
        // given
        Long requestId = 1L;
        EstimateRequest estimateRequest = mock(EstimateRequest.class);
        Estimate estimate = mock(Estimate.class);

        given(estimateRequestRepository.findById(requestId)).willReturn(Optional.of(estimateRequest));
        given(estimateRepository.findByEstimateRequest(estimateRequest)).willReturn(Optional.of(List.of(estimate)));

        // when
        List<MyEstimateResponseDto> response = estimateService.getMyEstimate(requestId);

        // then
        assertThat(response).hasSize(1);
        verify(estimateRepository, times(1)).findByEstimateRequest(estimateRequest);
    }

    @Test
    @DisplayName("내 견적 조회 실패 - 견적 요청 없음")
    void testGetMyEstimate_EstimateRequestNotFound() {
        // given
        Long requestId = 1L;
        given(estimateRequestRepository.findById(requestId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> estimateService.getMyEstimate(requestId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("견적 요청을 찾을 수 없습니다: " + requestId);
    }

    @Test
    @DisplayName("내 견적 상세 조회 성공")
    void testGetEstimateDetail_Success() {
        // given
        Long estimateId = 1L;
        Estimate estimate = mock(Estimate.class);
        GroomerProfile groomerProfile = mock(GroomerProfile.class);
        GroomerDetails groomerDetails = mock(GroomerDetails.class);

        given(estimateRepository.findWithGroomerProfileById(estimateId)).willReturn(Optional.of(estimate));
        given(estimate.getGroomerProfile()).willReturn(groomerProfile);
        given(groomerProfile.getDetails()).willReturn(groomerDetails);
        given(groomerDetails.getStartChat()).willReturn("저 미용 잘해요");

        // when
        MyEstimateDetailResponseDto response = estimateService.getEstimateDetail(estimateId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStartChat()).isEqualTo("저 미용 잘해요"); // 반환된 startChat 값 검증
        verify(estimateRepository, times(1)).findWithGroomerProfileById(estimateId);
    }

    @Test
    @DisplayName("견적 반려견 상세 조회 성공")
    void testGetEstimateDogDetail_Success() {
        // given
        Long requestId = 1L;
        Long dogProfileId = 2L;

        EstimateRequest estimateRequest = mock(EstimateRequest.class);
        EstimateRequestProfiles profile = mock(EstimateRequestProfiles.class);
        DogProfile dogProfile = mock(DogProfile.class);

        // Age mock setup
        DogAge age = mock(DogAge.class);
        given(dogProfile.getAge()).willReturn(age);
        given(age.getYear()).willReturn(2);
        given(age.getMonth()).willReturn(3);

        // 목 객체 설정
        given(estimateRequestRepository.findById(requestId)).willReturn(Optional.of(estimateRequest));
        given(estimateRequestProfilesRepository.findByEstimateRequestAndDogProfileId(estimateRequest, dogProfileId))
                .willReturn(Optional.of(profile));
        given(profile.getDogProfile()).willReturn(dogProfile);

        // 서비스 및 특징 목 데이터 준비
        given(estimateRequestServiceRepository.findByEstimateRequestProfiles(profile))
                .willReturn(Optional.of(Collections.emptyList()));
        given(dogProfileFeatureRepository.findByDogProfile(dogProfile))
                .willReturn(Optional.of(Collections.emptyList()));
        
        given(dogProfile.getName()).willReturn("구름이");
        given(dogProfile.getWeight()).willReturn(5);
        given(dogProfile.getGender()).willReturn(Gender.MALE);
        given(dogProfile.getNeutering()).willReturn(Neutering.Y);
        given(dogProfile.getImageKey()).willReturn("imageKey");
        given(dogProfile.getSpecies()).willReturn("말티즈");

        given(profile.getCurrentImageKey()).willReturn("currentImageKey");
        given(profile.getStyleRefImageKey()).willReturn("styleRefImageKey");
        given(profile.isAggression()).willReturn(false);
        given(profile.isHealthIssue()).willReturn(false);
        given(profile.getAggressionCharge()).willReturn(0);
        given(profile.getHealthIssueCharge()).willReturn(0);
        given(profile.getDescription()).willReturn("주의 해주세요");

        // when
        EstimateDogDetailResponseDto result = estimateService.getEstimateDogDetail(requestId, dogProfileId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDogName()).isEqualTo("구름이");
    }
    @Test
    @DisplayName("견적 반려견 상세 조회 실패 - 견적 요청 없음")
    void testGetEstimateDogDetail_RequestNotFound() {
        // given
        Long requestId = 1L;
        Long dogProfileId = 2L;

        given(estimateRequestRepository.findById(requestId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> estimateService.getEstimateDogDetail(requestId, dogProfileId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("견적 요청을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("견적 반려견 상세 조회 실패 - 강아지 프로필 없음")
    void testGetEstimateDogDetail_DogProfileNotFound() {
        // given
        Long requestId = 1L;
        Long dogProfileId = 2L;

        EstimateRequest estimateRequest = mock(EstimateRequest.class);

        given(estimateRequestRepository.findById(requestId)).willReturn(Optional.of(estimateRequest));
        given(estimateRequestProfilesRepository.findByEstimateRequestAndDogProfileId(estimateRequest, dogProfileId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> estimateService.getEstimateDogDetail(requestId, dogProfileId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 강아지 프로필 정보를 찾을 수 없습니다");
    }


    @Test
    @DisplayName("강아지 특징 조회 - 특징 없는 경우")
    void testGetFeatureList_NoFeaturesFound() {
        // given
        DogProfile dogProfile = mock(DogProfile.class);

        given(dogProfileFeatureRepository.findByDogProfile(dogProfile))
                .willReturn(Optional.empty());

        // when
        List<FeatureResponseDto> features = invokePrivateGetFeatureList(dogProfile);

        // then
        assertThat(features).isEmpty();
    }

    @Test
    @DisplayName("견적 반려견 상세 조회 실패 - 서비스 정보 없음")
    void testGetEstimateDogDetail_NoServiceFound() {
        // given
        Long requestId = 1L;
        Long dogProfileId = 2L;

        EstimateRequest estimateRequest = mock(EstimateRequest.class);
        EstimateRequestProfiles profile = mock(EstimateRequestProfiles.class);
        DogProfile dogProfile = mock(DogProfile.class);

        given(estimateRequestRepository.findById(requestId)).willReturn(Optional.of(estimateRequest));
        given(estimateRequestProfilesRepository.findByEstimateRequestAndDogProfileId(estimateRequest, dogProfileId))
                .willReturn(Optional.of(profile));
        given(profile.getDogProfile()).willReturn(dogProfile);

        given(estimateRequestServiceRepository.findByEstimateRequestProfiles(profile))
                .willReturn(Optional.empty()); // 서비스가 없는 경우

        // when & then
        assertThatThrownBy(() -> estimateService.getEstimateDogDetail(requestId, dogProfileId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("견적 요청 서비스가 없습니다");
    }

    @Test
    @DisplayName("견적 반려견 특징 조회 - 특징 있는 경우")
    void testGetFeatureList_FeaturesFound() {
        // given
        DogProfile dogProfile = mock(DogProfile.class);
        DogProfileFeature feature = mock(DogProfileFeature.class);
        Feature featureEntity = mock(Feature.class);

        given(feature.getFeature()).willReturn(featureEntity);
        given(featureEntity.getDescription()).willReturn("사랑스러운");

        given(dogProfileFeatureRepository.findByDogProfile(dogProfile))
                .willReturn(Optional.of(List.of(feature)));

        // when
        List<FeatureResponseDto> features = invokePrivateGetFeatureList(dogProfile);

        // then
        assertThat(features).hasSize(1);
        assertThat(features.get(0).getDescription()).isEqualTo("사랑스러운");
    }

    @Test
    @DisplayName("견적서 등록 실패 - 견적 요청 프로필 없음")
    void testInsertEstimate_ProfileNotFound() {
        // given
        EstimateWriteRequestDto requestDto = mock(EstimateWriteRequestDto.class);
        EstimateRequest estimateRequest = mock(EstimateRequest.class);
        given(requestDto.getRequestId()).willReturn(1L);
        given(estimateRequestRepository.findById(requestDto.getRequestId())).willReturn(Optional.of(estimateRequest));
        given(estimateRequest.getRequestStatus()).willReturn(RequestStatus.COMPLETED);

        DogPriceRequestDto dogPriceDto = mock(DogPriceRequestDto.class);
        given(requestDto.getDogPriceList()).willReturn(List.of(dogPriceDto));
        given(dogPriceDto.getDogProfileId()).willReturn(1L);

        given(estimateRequestProfilesRepository.findByDogProfileIdAndEstimateRequestId(1L, 1L))
                .willReturn(Optional.empty()); // 프로필이 없는 경우

        // when & then
        assertThatThrownBy(() -> estimateService.insertEstimate(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("미용사 프로필을 찾을 수 없습니다 ");
    }

    @Test
    @DisplayName("견적 요청한 서비스 정보 가져오기 - 성공")
    void testGetServiceList_Success() {
        // given
        EstimateRequestProfiles profile = mock(EstimateRequestProfiles.class);
        GroomerService groomerService = mock(GroomerService.class);
        EstimateRequestService service = mock(EstimateRequestService.class);

        given(service.getGroomerService()).willReturn(groomerService);
        given(groomerService.getId()).willReturn(1L);
        given(groomerService.getDescription()).willReturn("기본 미용 서비스");
        given(service.getPrice()).willReturn(5000);

        given(estimateRequestServiceRepository.findByEstimateRequestProfiles(profile))
                .willReturn(Optional.of(List.of(service)));

        // when
        List<ServicePriceResponseDto> serviceList = invokePrivateGetServiceList(profile);

        // then
        assertThat(serviceList).hasSize(1);
        assertThat(serviceList.get(0).getDescription()).isEqualTo("기본 미용 서비스");
        assertThat(serviceList.get(0).getPrice()).isEqualTo(5000);
    }

    @Test
    @DisplayName("견적서 등록 - 강아지별 서비스 업데이트 성공")
    void testInsertEstimate_DogServiceUpdateSuccess() {
        // given
        EstimateWriteRequestDto requestDto = mock(EstimateWriteRequestDto.class);
        EstimateRequest estimateRequest = mock(EstimateRequest.class);
        GroomerProfile groomerProfile = mock(GroomerProfile.class);

        DogPriceRequestDto dogPriceDto = mock(DogPriceRequestDto.class);
        ServiceRequestDto serviceDto = mock(ServiceRequestDto.class);
        EstimateRequestProfiles estimateRequestProfiles = mock(EstimateRequestProfiles.class);
        GroomerService groomerService = mock(GroomerService.class);
        EstimateRequestService estimateRequestService = mock(EstimateRequestService.class);

        // Mock requestDto 설정
        given(requestDto.getRequestId()).willReturn(1L);
        given(requestDto.getGroomerProfileId()).willReturn(1L);
        given(requestDto.getDogPriceList()).willReturn(List.of(dogPriceDto));

        // Mock dogPriceDto 설정
        given(dogPriceDto.getDogProfileId()).willReturn(1L);
        given(dogPriceDto.getAggressionCharge()).willReturn(500);
        given(dogPriceDto.getHealthIssueCharge()).willReturn(200);
        given(dogPriceDto.getServiceList()).willReturn(List.of(serviceDto));

        // Mock serviceDto 설정
        given(serviceDto.getServiceId()).willReturn(1L);
        given(serviceDto.getPrice()).willReturn(10000);

        // Mock estimateRequest 설정
        given(estimateRequestRepository.findById(requestDto.getRequestId())).willReturn(Optional.of(estimateRequest));
        given(estimateRequest.getRequestStatus()).willReturn(RequestStatus.COMPLETED);

        // Mock groomerProfileRepository 설정
        given(groomerProfileRepository.findById(requestDto.getGroomerProfileId())).willReturn(Optional.of(groomerProfile));

        // Mock Profiles 및 Services 설정
        given(estimateRequestProfilesRepository.findByDogProfileIdAndEstimateRequestId(1L, 1L))
                .willReturn(Optional.of(estimateRequestProfiles));
        given(groomerServiceRepository.findById(1L)).willReturn(Optional.of(groomerService));
        given(estimateRequestServiceRepository.findByEstimateRequestProfilesAndGroomerService(estimateRequestProfiles, groomerService))
                .willReturn(Optional.of(estimateRequestService));

        // when
        estimateService.insertEstimate(requestDto);

        // then
        verify(estimateRequestProfiles, times(1)).updateCharges(500, 200);
        verify(estimateRequestService, times(1)).updatePrice(10000);
    }


    @Test
    @DisplayName("견적서 수정 조회 - 강아지별 정보와 서비스 리스트 성공")
    void testGetEstimateGroomerDogAndService_Success() {
        // given
        Long estimateId = 1L;
        Estimate estimate = mock(Estimate.class);
        EstimateRequest estimateRequest = mock(EstimateRequest.class);
        EstimateRequestProfiles profile = mock(EstimateRequestProfiles.class);
        DogProfile dogProfile = mock(DogProfile.class);
        EstimateRequestService estimateRequestService = mock(EstimateRequestService.class);
        GroomerService groomerService = mock(GroomerService.class);

        given(estimate.getEstimateRequest()).willReturn(estimateRequest);
        given(estimateRequest.getId()).willReturn(1L);
        given(estimate.getDescription()).willReturn("미용이 오래 걸릴 수 있을 거 같아요");
        given(estimate.getTotalAmount()).willReturn(20000);
        given(estimate.getDate()).willReturn(LocalDateTime.parse("2024-12-01T15:30:00"));
        given(estimateRepository.findById(estimateId)).willReturn(Optional.of(estimate));

        given(estimateRequestProfilesRepository.findByEstimateRequestId(1L))
                .willReturn(Optional.of(List.of(profile)));

        given(profile.getDogProfile()).willReturn(dogProfile);
        given(dogProfile.getId()).willReturn(1L);
        given(dogProfile.getImageKey()).willReturn("dog-image-key");
        given(dogProfile.getName()).willReturn("구름이");

        given(profile.getDescription()).willReturn("사랑");
        given(profile.isAggression()).willReturn(false);
        given(profile.isHealthIssue()).willReturn(true);

        // Set up services
        given(estimateRequestServiceRepository.findByEstimateRequestProfiles(profile))
                .willReturn(Optional.of(List.of(estimateRequestService)));
        given(estimateRequestService.getGroomerService()).willReturn(groomerService);
        given(estimateRequestService.getPrice()).willReturn(10000);
        given(groomerService.getId()).willReturn(1L);
        given(groomerService.getDescription()).willReturn("목욕");

        // when
        EstimateResponseDto response = estimateService.getEstimateGroomer(estimateId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getComment()).isEqualTo("미용이 오래 걸릴 수 있을 거 같아요");
        assertThat(response.getEstimateList()).hasSize(1);
        assertThat(response.getEstimateList().get(0).getDogProfileResponseDto().getName()).isEqualTo("구름이");
        assertThat(response.getEstimateList().get(0).getDogPrice()).isEqualTo(10000);
    }

    @Test
    @DisplayName("견적서 등록 실패 - 견적 요청 상태가 완료가 아닌 경우")
    void testInsertEstimate_RequestStatusNotCompleted() {
        // given
        EstimateWriteRequestDto requestDto = mock(EstimateWriteRequestDto.class);
        EstimateRequest estimateRequest = mock(EstimateRequest.class);

        given(requestDto.getRequestId()).willReturn(1L);
        given(estimateRequestRepository.findById(requestDto.getRequestId())).willReturn(Optional.of(estimateRequest));
        given(estimateRequest.getRequestStatus()).willReturn(RequestStatus.PENDING); // COMPLETED가 아닌 상태

        // when & then
        assertThatThrownBy(() -> estimateService.insertEstimate(requestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("견적 요청 상태가 완료가 아닙니다");
    }

    private List<ServicePriceResponseDto> invokePrivateGetServiceList(EstimateRequestProfiles profile) {
        try {
            Method method = EstimateService.class.getDeclaredMethod("getServiceList", EstimateRequestProfiles.class);
            method.setAccessible(true);
            return (List<ServicePriceResponseDto>) method.invoke(estimateService, profile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private List<FeatureResponseDto> invokePrivateGetFeatureList(DogProfile dogProfile) {
        try {
            Method method = EstimateService.class.getDeclaredMethod("getFeatureList", DogProfile.class);
            method.setAccessible(true);
            return (List<FeatureResponseDto>) method.invoke(estimateService, dogProfile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
