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
                        GroomerProfileDetailsResponseDto.builder()
                                .profileId(groomerProfile.getId())
                                .serviceName(groomerProfile.getName())
                                .imageKey(groomerProfile.getImageKey())
                                .businessNumber(groomerProfile.getDetails().getBusinessNumber())
                                .contact(groomerProfile.getPhone())
                                .contactHours(groomerProfile.getContactHours())
                                .serviceType(groomerProfile.getServiceType())
                                .servicesDistricts(serviceDistricts) // 서비스 지역
                                .starScore(totalScore)
                                .estimateRequestCount(estimateRequestCount)
                                .reviewCount(reviewCount)
                                .address(groomerProfile.getDetails().getAddress())
                                .experience(groomerProfile.getDetails().getExperience())
                                .certifications(certifications) // 자격증
                                .servicesOffered(servicesOffered) // 서비스 제공
                                .description(groomerProfile.getDetails().getDescription())
                                .startMessage(groomerProfile.getDetails().getStartChat())
                                .badges(badges) // 뱃지
                                .faq(groomerProfile.getDetails().getFaq())
                                .build()
                        : null;
        // 응답 DTO 생성
        return GroomerProfileResponseDto.builder()
                .role("SALON")
                .name(groomerProfile.getUser().getName())
                .email(groomerProfile.getUser().getEmail())
                .profileImage(groomerProfile.getUser().getImageKey())
                .city(groomerProfile.getUser().getDistrict().getCity().getName())
                .district(groomerProfile.getUser().getDistrict().getName())
                .groomerProfile(groomerProfileDetailsResponseDto)
                .build();
    }

    // 미용사 프로필 상세 조회
    @Transactional(readOnly = true)
    public GroomerProfileDetailsResponseDto getGroomerProfile(Long userId, Long profileId) {

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

        // GroomerDetails가 null일 경우 null로 설정

        // 응답 DTO 생성
        return GroomerProfileDetailsResponseDto.builder()
                .profileId(groomerProfile.getId())
                .serviceName(groomerProfile.getName())
                .imageKey(groomerProfile.getImageKey())
                .businessNumber(groomerProfile.getDetails().getBusinessNumber())
                .contact(groomerProfile.getPhone())
                .contactHours(groomerProfile.getContactHours())
                .serviceType(groomerProfile.getServiceType())
                .servicesDistricts(serviceDistricts) // 서비스 지역
                .starScore(totalScore)
                .estimateRequestCount(estimateRequestCount)
                .reviewCount(reviewCount)
                .address(groomerProfile.getDetails().getAddress())
                .experience(groomerProfile.getDetails().getExperience())
                .certifications(certifications) // 자격증
                .servicesOffered(servicesOffered) // 서비스 제공
                .description(groomerProfile.getDetails().getDescription())
                .startMessage(groomerProfile.getDetails().getStartChat())
                .badges(badges) // 뱃지
                .faq(groomerProfile.getDetails().getFaq())
                .build();
    }

    @Transactional
    public void saveGroomerProfile(GroomerProfileRequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("유저 아이디를 찾을 수 없습니다. userId : " + userId));


        GroomerProfile groomerProfile = GroomerProfile.builder()
                .name(requestDto.getServiceName())
                .phone(requestDto.getContact())
                .contactHours(requestDto.getContactHours())
                .details(null)
                .user(user)
                .build();
        groomerProfileRepository.save(groomerProfile);

        user.updateRole(Role.ROLE_SALON);

        // 요청에 포함된 서비스 ID로 GroomerService 리스트 조회
        if (requestDto.getServicesOfferedId() != null && !requestDto.getServiceName().isEmpty()) {
            List<GroomerService> services = groomerServiceRepository.findAllById(requestDto.getServicesOfferedId());

            // 유효하지 않은 서비스 ID 확인
            if (services.size() != requestDto.getServicesOfferedId().size()) {
                throw new IllegalArgumentException("유효하지 않은 서비스 ID가 포함되어 있습니다.");
            }

            for (GroomerService service : services) {
                GroomerCanService groomerCanService = GroomerCanService.builder()
                        .groomerProfile(groomerProfile)
                        .groomerService(service)
                        .build();
                groomerProfile.getGroomerCanServices().add(groomerCanService);
            }
        }
    }


    @Transactional
    public void saveGroomerProfileDetails(GroomerProfileDetailsRequestDto requestDto, Long userId) {
        GroomerProfile groomerProfile = groomerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 미용사를 찾을 수 없습니다."));

        groomerProfile.updateProfileDetail(
                requestDto.getServiceType(),
                requestDto.getImageKey(),
                GroomerDetails.builder()
                        .address(requestDto.getAddress())
                        .description(requestDto.getDescription())
                        .faq(requestDto.getFaq())
                        .businessNumber(requestDto.getBusinessNumber())
                        .experience(requestDto.getExperience())
                        .startChat(requestDto.getStartMessage())
                        .build()
        );

        if (requestDto.getCertifications() != null && !requestDto.getCertifications().isEmpty()) {
            for (String certification : requestDto.getCertifications()) {
                GroomerCertification groomerCertification = GroomerCertification.builder()
                        .groomerProfile(groomerProfile)
                        .certification(certification)
                        .build();
                groomerProfile.getGroomerCertifications().add(groomerCertification);
            }
        }
        // 요청에 포함된 서비스 ID로 GroomerService 리스트 조회
        if (requestDto.getServicesDistrictIds() != null && !requestDto.getServicesDistrictIds().isEmpty()) {
            List<District> districts = districtRepository.findAllById(requestDto.getServicesDistrictIds());

            // 유효하지 않은 서비스 ID 확인
            if (districts.size() != requestDto.getServicesDistrictIds().size()) {
                throw new IllegalArgumentException("유효하지 않은 서비스 ID가 포함되어 있습니다.");
            }

            for (District district : districts) {
                GroomerServiceArea groomerServiceArea = GroomerServiceArea.builder()
                        .groomerProfile(groomerProfile)
                        .district(district)
                        .build();
                groomerProfile.getGroomerServiceAreas().add(groomerServiceArea);

            }
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
                requestDto.getServiceName(),
                requestDto.getContactHours(),
                requestDto.getServiceType(),
                requestDto.getContact(),
                requestDto.getImageKey(),
                GroomerDetails.builder()
                        .address(requestDto.getAddress())
                        .description(requestDto.getDescription())
                        .faq(requestDto.getFaq())
                        .businessNumber(requestDto.getBusinessNumber())
                        .experience(requestDto.getExperience())
                        .startChat(requestDto.getStartMessage())
                        .build()
        );

        groomerProfile.getGroomerCertifications().clear();
        groomerProfile.getGroomerServiceAreas().clear();
        groomerProfile.getGroomerCanServices().clear();

        if (requestDto.getCertifications() != null && !requestDto.getCertifications().isEmpty()) {
            for (String certification : requestDto.getCertifications()) {
                GroomerCertification groomerCertification = GroomerCertification.builder()
                        .groomerProfile(groomerProfile)
                        .certification(certification)
                        .build();
                groomerProfile.getGroomerCertifications().add(groomerCertification);
            }
        }
        // 요청에 포함된 서비스 ID로 GroomerService 리스트 조회
        if (requestDto.getServicesDistrictIds() != null && !requestDto.getServicesDistrictIds().isEmpty()) {
            List<District> districts = districtRepository.findAllById(requestDto.getServicesDistrictIds());

            // 유효하지 않은 서비스 ID 확인
            if (districts.size() != requestDto.getServicesDistrictIds().size()) {
                throw new IllegalArgumentException("유효하지 않은 서비스 ID가 포함되어 있습니다.");
            }

            for (District district : districts) {
                GroomerServiceArea groomerServiceArea = GroomerServiceArea.builder()
                        .groomerProfile(groomerProfile)
                        .district(district)
                        .build();
                groomerProfile.getGroomerServiceAreas().add(groomerServiceArea);

            }
        }

        // 요청에 포함된 서비스 ID로 GroomerService 리스트 조회
        if (requestDto.getServicesOfferedId() != null && !requestDto.getServiceName().isEmpty()) {
            List<GroomerService> services = groomerServiceRepository.findAllById(requestDto.getServicesOfferedId());

            // 유효하지 않은 서비스 ID 확인
            if (services.size() != requestDto.getServicesOfferedId().size()) {
                throw new IllegalArgumentException("유효하지 않은 서비스 ID가 포함되어 있습니다.");
            }

            for (GroomerService service : services) {
                GroomerCanService groomerCanService = GroomerCanService.builder()
                        .groomerProfile(groomerProfile)
                        .groomerService(service)
                        .build();
                groomerProfile.getGroomerCanServices().add(groomerCanService);

            }
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
}