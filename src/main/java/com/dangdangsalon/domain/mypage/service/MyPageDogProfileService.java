package com.dangdangsalon.domain.mypage.service;

import com.dangdangsalon.domain.coupon.entity.CouponStatus;
import com.dangdangsalon.domain.dogprofile.entity.DogAge;
import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.dogprofile.entity.DogProfileFeature;
import com.dangdangsalon.domain.dogprofile.feature.entity.Feature;
import com.dangdangsalon.domain.dogprofile.repository.DogProfileRepository;
import com.dangdangsalon.domain.dogprofile.repository.FeatureRepository;
import com.dangdangsalon.domain.estimate.request.dto.FeatureResponseDto;
import com.dangdangsalon.domain.mypage.dto.req.*;
import com.dangdangsalon.domain.mypage.dto.res.*;
import com.dangdangsalon.domain.orders.entity.OrderStatus;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageDogProfileService {
    private final UserRepository userRepository;
    private final DogProfileRepository dogProfileRepository;
    private final FeatureRepository featureRepository;

    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("유저 아이디를 찾을 수 없습니다. userId : " + userId));

        // Coupon 중 상태가 NOT_USED인 것만 필터링하여 개수를 계산
        long notUsedCouponCount = user.getCoupons().stream()
                .filter(coupon -> coupon.getStatus() != null && coupon.getStatus().equals(CouponStatus.NOT_USED))
                .count();

        // PaymentStatus가 'APPROVED'인 결제 수 계산
        long paymentCount = user.getOrders().stream()
                .filter(order -> order.getStatus() != null && order.getStatus().equals(OrderStatus.ACCEPTED))
                .count();

        return UserProfileResponseDto.createUserProfileResponseDto(user, notUsedCouponCount, paymentCount);
    }

    @Transactional(readOnly = true)
    public MyDogProfileResponseDto getDogProfile(Long userId, Long dogProfileId) {

        // 해당 유저의 특정 반려견 프로필을 dogProfileId로 조회
        DogProfile dogProfile = dogProfileRepository.findByIdAndUserIdWithFeatures(dogProfileId, userId)
                .orElseThrow(() -> new IllegalArgumentException("반려견 프로필을 찾을 수 없습니다. dogProfileId : " + dogProfileId));

        // DogProfileFeature를 통해 Feature 목록을 가져와서 FeatureDto로 변환
        List<FeatureResponseDto> featureDtos = dogProfile.getDogProfileFeatures().stream()
                .map(dogProfileFeature -> FeatureResponseDto.builder()
                        .description(dogProfileFeature.getFeature().getDescription()) // Feature의 description 가져오기
                        .build())
                .toList();

        return MyDogProfileResponseDto.createMyDogProfileResponseDto(dogProfile, featureDtos);
    }

    @Transactional
    public void saveDogProfile(DogProfileRequestDto request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("유저 아이디를 찾을 수 없습니다. userId : " + userId));

        // 사용자 ID를 기반으로 반려견 프로필 생성
        DogProfile dogProfile = DogProfile.createDogProfile(request, user);

        dogProfileRepository.save(dogProfile);

        // Feature IDs로 Feature 엔티티 가져오기
        List<Feature> features = featureRepository.findAllById(request.getFeatureIds());
        if (features.size() != request.getFeatureIds().size()) {
            throw new IllegalArgumentException("유효하지 않은 Feature ID가 포함되어 있습니다.");
        }

        List<DogProfileFeature> dogProfileFeatures = addFeature(features, dogProfile);

        // Additional Feature 저장
        if (request.getAdditionalFeature() != null && !request.getAdditionalFeature().isEmpty()) {
            addAdditionFeature(dogProfileFeatures, dogProfile, request.getAdditionalFeature());
        }
        dogProfile.getDogProfileFeatures().addAll(dogProfileFeatures);
    }

    @Transactional
    public void updateDogProfile(DogProfileRequestDto request, Long userId, Long dogProfileId) {
        // 해당 유저의 특정 반려견 프로필을 dogProfileId로 조회
        DogProfile dogProfile = dogProfileRepository.findById(dogProfileId)
                .orElseThrow(() -> new IllegalArgumentException("반려견 프로필을 찾을 수 없습니다. dogProfileId : " + dogProfileId));

        // 유저가 해당 반려견 프로필을 수정할 권한이 있는지 확인
        if (!dogProfile.isValidUser(userId)) {
            throw new IllegalArgumentException("이 반려견 프로필을 수정할 권한이 없습니다.");
        }

        dogProfile.updateProfile(
                request.getName(),
                request.getProfileImage() == null ? "default.jpg" : request.getProfileImage(),
                request.getSpecies(),
                DogAge.builder()
                        .year(request.getAgeYear())
                        .month(request.getAgeMonth())
                        .build(),
                request.getGender(),
                request.getNeutering(),
                request.getWeight()
        );

        // 기존 특징 삭제
        dogProfile.getDogProfileFeatures().clear();

        // Feature IDs로 Feature 엔티티 가져오기
        List<Feature> features = featureRepository.findAllById(request.getFeatureIds());
        if (features.size() != request.getFeatureIds().size()) {
            throw new IllegalArgumentException("유효하지 않은 Feature ID가 포함되어 있습니다.");
        }

        List<DogProfileFeature> dogProfileFeatures = addFeature(features, dogProfile);

        // Additional Feature 저장
        if (request.getAdditionalFeature() != null && !request.getAdditionalFeature().isEmpty()) {
            addAdditionFeature(dogProfileFeatures, dogProfile, request.getAdditionalFeature());
        }

        // DogProfileFeature 저장
        dogProfile.getDogProfileFeatures().addAll(dogProfileFeatures);
    }

    @Transactional
    public void deleteDogProfile(Long userId, Long dogProfileId) {
        // 해당 유저의 특정 반려견 프로필을 dogProfileId로 조회
        DogProfile dogProfile = dogProfileRepository.findById(dogProfileId)
                .orElseThrow(() -> new IllegalArgumentException("반려견 프로필을 찾을 수 없습니다. dogProfileId : " + dogProfileId));

        // 유저가 해당 반려견 프로필을 수정할 권한이 있는지 확인
        if (!dogProfile.isValidUser(userId)) {
            throw new IllegalArgumentException("이 반려견 프로필을 삭제할 권한이 없습니다.");
        }

        dogProfileRepository.delete(dogProfile);
    }

    private List<DogProfileFeature> addFeature(List<Feature> features, DogProfile dogProfile) {
        List<DogProfileFeature> dogProfileFeatures = new ArrayList<>();

        // Feature 추가
        for (Feature feature : features) {
            dogProfileFeatures.add(DogProfileFeature.builder()
                    .dogProfile(dogProfile)
                    .feature(feature)
                    .build());
        }
        return dogProfileFeatures;
    }

    private void addAdditionFeature(List<DogProfileFeature> dogProfileFeatures,
                                    DogProfile dogProfile,
                                    String requestFeature) {

        Feature additionalFeature = featureRepository.save(
                Feature.builder()
                        .description(requestFeature)
                        .isCustom(true)
                        .build()
        );
        dogProfileFeatures.add(DogProfileFeature.builder()
                .feature(additionalFeature)
                .dogProfile(dogProfile)
                .build());
    }
}