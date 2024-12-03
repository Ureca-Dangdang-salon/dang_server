package com.dangdangsalon.domain.estimate.request.service;

import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.dogprofile.repository.DogProfileRepository;
import com.dangdangsalon.domain.estimate.request.dto.DogEstimateRequestDto;
import com.dangdangsalon.domain.estimate.request.dto.EstimateRequestDto;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestService;
import com.dangdangsalon.domain.estimate.request.entity.RequestStatus;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestProfilesRepository;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestRepository;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestServiceRepository;
import com.dangdangsalon.domain.groomerprofile.entity.ServiceType;
import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import com.dangdangsalon.domain.groomerservice.repository.GroomerServiceRepository;
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

class EstimateRequestInsertServiceTest {

    @InjectMocks
    private EstimateRequestInsertService estimateRequestInsertService;

    @Mock
    private EstimateRequestRepository estimateRequestRepository;

    @Mock
    private EstimateRequestProfilesRepository estimateRequestProfilesRepository;

    @Mock
    private EstimateRequestServiceRepository estimateRequestServiceRepository;

    @Mock
    private DogProfileRepository dogProfileRepository;

    @Mock
    private GroomerServiceRepository groomerServiceRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("견적 요청 등록 성공")
    void insertEstimateRequest_Success() {
        // given
        User user = mock(User.class);
        District district = mock(District.class);

        DogProfile dogProfile = mock(DogProfile.class);
        GroomerService groomerService = mock(GroomerService.class);

        DogEstimateRequestDto dogEstimateRequestDto = createDogEstimateRequestDto();

        EstimateRequestDto estimateRequestDto = createEstimateRequestDto(List.of(dogEstimateRequestDto));

        EstimateRequest estimateRequest = mock(EstimateRequest.class);
        EstimateRequestProfiles estimateRequestProfiles = mock(EstimateRequestProfiles.class);

        when(dogProfileRepository.findById(dogEstimateRequestDto.getDogProfileId())).thenReturn(Optional.of(dogProfile));
        when(groomerServiceRepository.findById(1L)).thenReturn(Optional.of(groomerService));
        when(estimateRequestRepository.save(any(EstimateRequest.class))).thenReturn(estimateRequest);
        when(estimateRequestProfilesRepository.save(any(EstimateRequestProfiles.class))).thenReturn(estimateRequestProfiles);

        // when
        EstimateRequest result = estimateRequestInsertService.insertEstimateRequest(estimateRequestDto, user, district);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRequestDate()).isEqualTo(estimateRequestDto.getDate());
        assertThat(result.getRequestStatus()).isEqualTo(RequestStatus.COMPLETED); // 변경된 부분
        assertThat(result.getServiceType()).isEqualTo(ServiceType.valueOf(estimateRequestDto.getServiceType()));
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getDistrict()).isEqualTo(district);

        verify(estimateRequestRepository, times(1)).save(any(EstimateRequest.class));
        verify(dogProfileRepository, times(1)).findById(dogEstimateRequestDto.getDogProfileId());
        verify(groomerServiceRepository, times(1)).findById(1L);
        verify(estimateRequestProfilesRepository, times(1)).save(any(EstimateRequestProfiles.class));
        verify(estimateRequestServiceRepository, times(1)).save(any(EstimateRequestService.class));
    }

    @Test
    @DisplayName("강아지 프로필이 없는 경우 예외 발생")
    void insertEstimateRequest_DogProfileNotFound() {
        // given
        User user = mock(User.class);
        District district = mock(District.class);

        DogEstimateRequestDto dogEstimateRequestDto = createDogEstimateRequestDto();

        EstimateRequestDto estimateRequestDto = createEstimateRequestDto(List.of(dogEstimateRequestDto));

        when(dogProfileRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> estimateRequestInsertService.insertEstimateRequest(estimateRequestDto, user, district))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("강아지 프로필을 찾을 수 없습니다: 1");

        verify(dogProfileRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("서비스가 없는 경우 예외 발생")
    void insertEstimateRequest_ServiceNotFound() {
        // given
        User user = mock(User.class);
        District district = mock(District.class);

        DogEstimateRequestDto dogEstimateRequestDto = createDogEstimateRequestDto();

        EstimateRequestDto estimateRequestDto = createEstimateRequestDto(List.of(dogEstimateRequestDto));

        DogProfile dogProfile = mock(DogProfile.class);

        when(dogProfileRepository.findById(1L)).thenReturn(Optional.of(dogProfile));
        when(groomerServiceRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> estimateRequestInsertService.insertEstimateRequest(estimateRequestDto, user, district))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("서비스를 찾을 수 없습니다: 1");

        verify(dogProfileRepository, times(1)).findById(1L);
        verify(groomerServiceRepository, times(1)).findById(1L);
    }

    private DogEstimateRequestDto createDogEstimateRequestDto() {
        return new DogEstimateRequestDto(
                1L,
                "currentImage.jpg",
                "styleRefImage.jpg",
                true,
                false,
                "잘 부탁드립니다",
                List.of(1L));
    }

    private EstimateRequestDto createEstimateRequestDto(List<DogEstimateRequestDto> dogEstimateRequestDtoList) {
        return new EstimateRequestDto(
                1L,
                1L,
                LocalDateTime.now(),
                "VISIT",
                dogEstimateRequestDtoList
        );
    }
}
