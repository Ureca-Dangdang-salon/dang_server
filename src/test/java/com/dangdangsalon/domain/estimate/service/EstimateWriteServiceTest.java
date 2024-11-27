package com.dangdangsalon.domain.estimate.service;

import com.dangdangsalon.domain.dogprofile.entity.*;
import com.dangdangsalon.domain.dogprofile.feature.entity.Feature;
import com.dangdangsalon.domain.dogprofile.repository.DogProfileFeatureRepository;
import com.dangdangsalon.domain.estimate.dto.EstimateWriteDetailResponseDto;
import com.dangdangsalon.domain.estimate.dto.EstimateWriteResponseDto;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestService;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestProfilesRepository;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestRepository;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestServiceRepository;
import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class EstimateWriteServiceTest {

    @InjectMocks
    private EstimateWriteService estimateWriteService;

    @Mock
    private EstimateRequestRepository estimateRequestRepository;

    @Mock
    private EstimateRequestProfilesRepository estimateRequestProfilesRepository;

    @Mock
    private EstimateRequestServiceRepository estimateRequestServiceRepository;

    @Mock
    private DogProfileFeatureRepository dogProfileFeatureRepository;

    private DogProfile dogProfile;
    private EstimateRequest estimateRequest;
    private EstimateRequestProfiles estimateRequestProfiles;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        estimateRequest = mock(EstimateRequest.class);
        dogProfile = createMockDogProfile("구름이", 2, 6, 10, Gender.MALE, Neutering.Y, "default-image-key", "리트리버");
        estimateRequestProfiles = createMockProfile(dogProfile, "current-image-key", "style-ref-image-key", false, false, "확 물어버린당");
    }

    @Test
    @DisplayName("견적서 작성 반려견 요청 목록 조회 성공")
    void getEstimateRequestDog_Success() {
        // given
        Long requestId = 1L;

        when(estimateRequestRepository.findById(requestId)).thenReturn(Optional.of(estimateRequest));
        when(estimateRequestProfilesRepository.findByEstimateRequest(estimateRequest))
                .thenReturn(Optional.of(List.of(estimateRequestProfiles)));
        when(estimateRequestServiceRepository.findByEstimateRequestProfiles(estimateRequestProfiles))
                .thenReturn(Optional.of(List.of()));

        // when
        List<EstimateWriteResponseDto> result = estimateWriteService.getEstimateRequestDog(requestId);

        // then
        assertThat(result).isNotEmpty();
        verify(estimateRequestRepository, times(1)).findById(requestId);
        verify(estimateRequestProfilesRepository, times(1)).findByEstimateRequest(estimateRequest);
    }

    @Test
    @DisplayName("견적 요청 ID가 없을 때 예외 발생")
    void getEstimateRequestDog_NotFound() {
        // given
        Long requestId = 1L;
        when(estimateRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> estimateWriteService.getEstimateRequestDog(requestId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("견적 요청을 찾을 수 없습니다 : " + requestId);
    }

    @Test
    @DisplayName("견적서 작성 반려견 요청 상세 보기 성공")
    void getEstimateRequestDogDetail_Success() {
        // given
        Long requestId = 1L;
        Long dogProfileId = 2L;

        Feature featureEntity = createMockFeature("사람하고 잘 지내요");
        GroomerService groomerService = createMockGroomerService(1L, "목욕");

        DogProfileFeature feature = mock(DogProfileFeature.class);
        EstimateRequestService estimateRequestService = mock(EstimateRequestService.class);

        when(estimateRequestRepository.findById(requestId)).thenReturn(Optional.of(estimateRequest));
        when(estimateRequestProfilesRepository.findByEstimateRequestAndDogProfileId(estimateRequest, dogProfileId))
                .thenReturn(Optional.of(estimateRequestProfiles));
        when(dogProfileFeatureRepository.findByDogProfile(dogProfile)).thenReturn(Optional.of(List.of(feature)));
        when(feature.getFeature()).thenReturn(featureEntity);

        when(estimateRequestServiceRepository.findByEstimateRequestProfiles(estimateRequestProfiles))
                .thenReturn(Optional.of(List.of(estimateRequestService)));
        when(estimateRequestService.getGroomerService()).thenReturn(groomerService);

        // when
        EstimateWriteDetailResponseDto result = estimateWriteService.getEstimateRequestDogDetail(requestId, dogProfileId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDogName()).isEqualTo("구름이");
        assertThat(result.getYear()).isEqualTo(2);
        assertThat(result.getMonth()).isEqualTo(6);
        assertThat(result.getDogWeight()).isEqualTo(10);
        assertThat(result.getGender()).isEqualTo(Gender.MALE);
        assertThat(result.getNeutering()).isEqualTo(Neutering.Y);
        assertThat(result.getImageKey()).isEqualTo("default-image-key");
        assertThat(result.getCurrentImageKey()).isEqualTo("current-image-key");
        assertThat(result.getStyleRefImageKey()).isEqualTo("style-ref-image-key");
        assertThat(result.getSpecies()).isEqualTo("리트리버");
        assertThat(result.getServiceList()).hasSize(1);
        assertThat(result.getServiceList().get(0).getDescription()).isEqualTo("목욕");
        assertThat(result.getFeatureList()).hasSize(1);
        assertThat(result.getFeatureList().get(0).getDescription()).isEqualTo("사람하고 잘 지내요");
        assertThat(result.isAggression()).isFalse();
        assertThat(result.isHealthIssue()).isFalse();
        assertThat(result.getDescription()).isEqualTo("확 물어버린당");

        verify(estimateRequestRepository, times(1)).findById(requestId);
        verify(estimateRequestProfilesRepository, times(1))
                .findByEstimateRequestAndDogProfileId(estimateRequest, dogProfileId);
        verify(dogProfileFeatureRepository, times(1)).findByDogProfile(dogProfile);
        verify(estimateRequestServiceRepository, times(1)).findByEstimateRequestProfiles(estimateRequestProfiles);
    }

    @Test
    @DisplayName("견적서 작성 반려견 요청 상세 보기 실패")
    void getEstimateRequestDogDetail_NotFound() {
        // given
        Long requestId = 1L;
        Long dogProfileId = 2L;

        when(estimateRequestRepository.findById(requestId)).thenReturn(Optional.of(estimateRequest));
        when(estimateRequestProfilesRepository.findByEstimateRequestAndDogProfileId(estimateRequest, dogProfileId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> estimateWriteService.getEstimateRequestDogDetail(requestId, dogProfileId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("견적 요청에 해당하는 강아지 프로필을 찾을 수 없습니다.");
    }

    private DogProfile createMockDogProfile(String name, int year, int month, int weight, Gender gender, Neutering neutering, String imageKey, String species) {
        DogProfile dogProfile = mock(DogProfile.class);
        when(dogProfile.getName()).thenReturn(name);
        when(dogProfile.getAge()).thenReturn(new DogAge(year, month));
        when(dogProfile.getWeight()).thenReturn(weight);
        when(dogProfile.getGender()).thenReturn(gender);
        when(dogProfile.getNeutering()).thenReturn(neutering);
        when(dogProfile.getImageKey()).thenReturn(imageKey);
        when(dogProfile.getSpecies()).thenReturn(species);
        return dogProfile;
    }

    private EstimateRequestProfiles createMockProfile(DogProfile dogProfile, String currentImageKey, String styleRefImageKey, boolean aggression, boolean healthIssue, String description) {
        EstimateRequestProfiles profile = mock(EstimateRequestProfiles.class);
        when(profile.getDogProfile()).thenReturn(dogProfile);
        when(profile.getCurrentImageKey()).thenReturn(currentImageKey);
        when(profile.getStyleRefImageKey()).thenReturn(styleRefImageKey);
        when(profile.isAggression()).thenReturn(aggression);
        when(profile.isHealthIssue()).thenReturn(healthIssue);
        when(profile.getDescription()).thenReturn(description);
        return profile;
    }

    private GroomerService createMockGroomerService(Long id, String description) {
        GroomerService groomerService = mock(GroomerService.class);
        when(groomerService.getId()).thenReturn(id);
        when(groomerService.getDescription()).thenReturn(description);
        return groomerService;
    }

    private Feature createMockFeature(String description) {
        Feature feature = mock(Feature.class);
        when(feature.getDescription()).thenReturn(description);
        return feature;
    }
}
