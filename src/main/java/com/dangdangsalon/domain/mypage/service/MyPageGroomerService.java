package com.dangdangsalon.domain.mypage.service;

import com.dangdangsalon.domain.groomerprofile.entity.*;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerProfileRepository;
import com.dangdangsalon.domain.groomerprofile.request.entity.GroomerRequestStatus;
import com.dangdangsalon.domain.groomerprofile.review.entity.Review;
import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import com.dangdangsalon.domain.groomerservice.repository.GroomerServiceRepository;
import com.dangdangsalon.domain.mypage.dto.req.*;
import com.dangdangsalon.domain.mypage.dto.res.*;
import com.dangdangsalon.domain.region.entity.District;
import com.dangdangsalon.domain.region.repository.DistrictRepository;
import com.dangdangsalon.domain.user.entity.Role;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageGroomerService {
    private final UserRepository userRepository;
    private final GroomerProfileRepository groomerProfileRepository;

    private final GroomerServiceRepository groomerServiceRepository;
    private final DistrictRepository districtRepository;

    // 미용사 마이페이지 조회
    @Transactional(readOnly = true)
    public GroomerProfileResponseDto getGroomerProfilePage(Long userId) {
        // GroomerProfile을 User와 연결된 정보로 조회
        GroomerProfile groomerProfile = groomerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 미용사를 찾을 수 없습니다."));

        Long profileId = groomerProfile.getId();

        // 서비스 지역 정보 매핑
        List<DistrictResponseDto> serviceDistricts =
                groomerProfileRepository.findServiceAreasWithDistricts(profileId);

        List<GroomerServicesResponseDto> servicesOffered =
                groomerProfileRepository.findGroomerServices(profileId);

        // 뱃지
        List<BadgeResponseDto> badges = groomerProfileRepository.findBadgesByProfileId(profileId);

        // 자격증
        List<String> certifications = groomerProfile.getGroomerCertifications().stream()
                .map(GroomerCertification::getCertification)
                .toList();

        // 견적 요청 대기 개수
        long estimateRequestCount = groomerProfile.getGroomerEstimateRequests().stream()
                .filter(request -> request.getGroomerRequestStatus() == GroomerRequestStatus.PENDING)
                .count();

        // 리뷰 개수
        long reviewCount = groomerProfile.getReviews().size();

        // 리뷰 총 점수
        double totalScore = reviewCount != 0 ? groomerProfile.getReviews().stream()
                .mapToDouble(Review::getStarScore)
                .sum() / reviewCount : 0;

        GroomerProfileDetailsResponseDto groomerProfileDetailsResponseDto =
                groomerProfile.getDetails() != null ?
                        GroomerProfileDetailsResponseDto.create(
                                groomerProfile,
                                GroomerProfileDetailsInfoResponseDto.create(
                                        totalScore, reviewCount, estimateRequestCount, badges,
                                        servicesOffered, serviceDistricts, certifications
                                )
                        )
                        : null;
        // 응답 DTO 생성
        return GroomerProfileResponseDto.createGroomerProfileResponseDto(
                groomerProfile,
                groomerProfileDetailsResponseDto
        );
    }

    // 미용사 프로필 상세 조회
    @Transactional(readOnly = true)
    public GroomerProfileDetailsResponseDto getGroomerProfile(Long profileId) {

        GroomerProfile groomerProfile = groomerProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("해당 미용사를 찾을 수 없습니다. profileId : " + profileId));

        if (groomerProfile.getDetails() == null) {
            throw new IllegalArgumentException("해당 미용사의 프로필이 없습니다.");
        }
        // 서비스 지역 정보 매핑
        List<DistrictResponseDto> serviceDistricts =
                groomerProfileRepository.findServiceAreasWithDistricts(profileId);

        List<GroomerServicesResponseDto> servicesOffered =
                groomerProfileRepository.findGroomerServices(profileId);

        // 뱃지
        List<BadgeResponseDto> badges = groomerProfileRepository.findBadgesByProfileId(profileId);

        // 자격증
        List<String> certifications = groomerProfile.getGroomerCertifications().stream()
                .map(GroomerCertification::getCertification)
                .toList();

        // 견적 요청 완료 개수
        long estimateRequestCount = groomerProfile.getGroomerEstimateRequests().stream()
                .filter(request -> request.getGroomerRequestStatus() == GroomerRequestStatus.COMPLETED)
                .count();

        // 리뷰 개수
        long reviewCount = groomerProfile.getReviews().size();

        // 리뷰 총 점수
        double totalScore = groomerProfile.getReviews().stream()
                .mapToDouble(Review::getStarScore)
                .sum() / reviewCount;

        // 응답 DTO 생성
        return GroomerProfileDetailsResponseDto.create(
                groomerProfile,
                GroomerProfileDetailsInfoResponseDto.create(
                        totalScore, reviewCount, estimateRequestCount, badges,
                        servicesOffered, serviceDistricts, certifications
                )
        );
    }

    @Transactional
    public void saveGroomerProfile(GroomerProfileRequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("유저 아이디를 찾을 수 없습니다. userId : " + userId));


        GroomerProfile groomerProfile = GroomerProfile.createGroomerProfile(requestDto, user);
        groomerProfileRepository.save(groomerProfile);

        user.updateRole(Role.ROLE_SALON);

        // 요청에 포함된 서비스 ID로 GroomerService 리스트 조회
        if (requestDto.getServicesOfferedId() != null && !requestDto.getServicesOfferedId().isEmpty()) {
            List<GroomerService> services = groomerServiceRepository.findAllById(requestDto.getServicesOfferedId());

            // 유효하지 않은 서비스 ID 확인
            if (services.size() != requestDto.getServicesOfferedId().size()) {
                throw new IllegalArgumentException("유효하지 않은 서비스 ID가 포함되어 있습니다.");
            }
            addCanService(services, groomerProfile);
        }
    }


    @Transactional
    public void saveGroomerProfileDetails(GroomerProfileDetailsRequestDto requestDto, Long userId) {
        GroomerProfile groomerProfile = groomerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 미용사를 찾을 수 없습니다."));

        groomerProfile.updateProfileDetail(
                requestDto.getServiceType(),
                requestDto.getImageKey(),
                GroomerDetails.createGroomerDetails(requestDto)
        );

        if (requestDto.getCertifications() != null && !requestDto.getCertifications().isEmpty()) {
            addCertification(requestDto.getCertifications(), groomerProfile);
        }
        // 요청에 포함된 서비스 ID로 GroomerService 리스트 조회
        if (requestDto.getServicesDistrictIds() != null && !requestDto.getServicesDistrictIds().isEmpty()) {
            List<District> districts = districtRepository.findAllById(requestDto.getServicesDistrictIds());

            // 유효하지 않은 서비스 ID 확인
            if (districts.size() != requestDto.getServicesDistrictIds().size()) {
                throw new IllegalArgumentException("유효하지 않은 서비스 ID가 포함되어 있습니다.");
            }
            addDistrict(districts, groomerProfile);
        }
    }


    @Transactional
    public void updateGroomerProfile(GroomerDetailsUpdateRequestDto requestDto, Long userId, Long profileId) {
        GroomerProfile groomerProfile = groomerProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("해당 미용사를 찾을 수 없습니다. profileId : " + profileId));

        if (!groomerProfile.isValidUser(userId)) {
            throw new IllegalArgumentException("프로필을 수정할 권한이 없습니다. userId : " + userId);
        }
        groomerProfile.updateProfile(
                requestDto.getName(),
                requestDto.getContactHours(),
                requestDto.getServiceType(),
                requestDto.getPhone(),
                requestDto.getImageKey(),
                GroomerDetails.updateGroomerDetails(requestDto)
        );

        groomerProfile.getGroomerCertifications().clear();
        groomerProfile.getGroomerServiceAreas().clear();
        groomerProfile.getGroomerCanServices().clear();

        if (requestDto.getCertifications() != null && !requestDto.getCertifications().isEmpty()) {
            addCertification(requestDto.getCertifications(), groomerProfile);
        }
        // 요청에 포함된 서비스 ID로 GroomerService 리스트 조회
        if (requestDto.getServicesDistrictIds() != null && !requestDto.getServicesDistrictIds().isEmpty()) {
            List<District> districts = districtRepository.findAllById(requestDto.getServicesDistrictIds());

            // 유효하지 않은 서비스 ID 확인
            if (districts.size() != requestDto.getServicesDistrictIds().size()) {
                throw new IllegalArgumentException("유효하지 않은 서비스 ID가 포함되어 있습니다.");
            }
            addDistrict(districts, groomerProfile);
        }

        // 요청에 포함된 서비스 ID로 GroomerService 리스트 조회
        if (requestDto.getServicesOfferedId() != null && !requestDto.getServicesOfferedId().isEmpty()) {
            List<GroomerService> services = groomerServiceRepository.findAllById(requestDto.getServicesOfferedId());

            // 유효하지 않은 서비스 ID 확인
            if (services.size() != requestDto.getServicesOfferedId().size()) {
                throw new IllegalArgumentException("유효하지 않은 서비스 ID가 포함되어 있습니다.");
            }
            addCanService(services, groomerProfile);
        }
    }

    @Transactional
    public void deleteGroomerProfile(Long userId, Long profileId) {
        GroomerProfile groomerProfile = groomerProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("해당 미용사를 찾을 수 없습니다. profileId : " + profileId));

        if (!groomerProfile.isValidUser(userId)) {
            throw new IllegalArgumentException("프로필을 삭제할 권한이 없습니다. userId : " + userId);
        }

        groomerProfileRepository.delete(groomerProfile);
    }

    private void addCertification(List<String> certifications,
                                    GroomerProfile groomerProfile) {
        for (String certification : certifications) {
            GroomerCertification groomerCertification = GroomerCertification.builder()
                    .groomerProfile(groomerProfile)
                    .certification(certification)
                    .build();
            groomerProfile.getGroomerCertifications().add(groomerCertification);
        }
    }

    private void addDistrict(List<District> districts,
                                  GroomerProfile groomerProfile) {
        for (District district : districts) {
            GroomerServiceArea groomerServiceArea = GroomerServiceArea.builder()
                    .groomerProfile(groomerProfile)
                    .district(district)
                    .build();
            groomerProfile.getGroomerServiceAreas().add(groomerServiceArea);
        }
    }

    private void addCanService(List<GroomerService> services,
                             GroomerProfile groomerProfile) {
        for (GroomerService service : services) {
            GroomerCanService groomerCanService = GroomerCanService.builder()
                    .groomerProfile(groomerProfile)
                    .groomerService(service)
                    .build();
            groomerProfile.getGroomerCanServices().add(groomerCanService);
        }
    }
}