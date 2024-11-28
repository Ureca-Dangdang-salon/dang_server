package com.dangdangsalon.domain.dogprofile.entity;
import com.dangdangsalon.config.base.BaseEntity;
import com.dangdangsalon.domain.mypage.dto.req.DogProfileRequestDto;
import com.dangdangsalon.domain.mypage.dto.res.CommonProfileResponseDto;
import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dog_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DogProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_key")
    private String imageKey;

    private String name;

    private String species; //UI 보고 Enum 고민.

    @Embedded
    private DogAge age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Neutering neutering;

    private int weight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "dogProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<DogProfileFeature> dogProfileFeatures = new ArrayList<>();

    @Builder
    public DogProfile(String imageKey, String name, String species, DogAge age, Gender gender, Neutering neutering,
                      int weight, User user) {
        this.imageKey = imageKey;
        this.name = name;
        this.species = species;
        this.age = age;
        this.gender = gender;
        this.neutering = neutering;
        this.weight = weight;
        this.user = user;
    }

    public boolean isValidUser(Long userId) {
        return this.getUser().getId().equals(userId);
    }

    public void updateProfile(String name, String profileImage, String species, DogAge dogAge,
                              Gender gender, Neutering neutering, int weight) {
        this.name = name;
        this.imageKey = profileImage;
        this.species = species;
        this.age = dogAge;
        this.gender = gender;
        this.neutering = neutering;
        this.weight = weight;
    }

    public static DogProfile createDogProfile(DogProfileRequestDto request, User user) {
        return DogProfile.builder()
                .name(request.getName())
                .imageKey(request.getProfileImage() == null ? "default.jpg" : request.getProfileImage())
                .species(request.getSpecies())
                .age(DogAge.builder()
                        .month(request.getAgeMonth())
                        .year(request.getAgeYear())
                        .build())
                .gender(request.getGender())
                .neutering(request.getNeutering())
                .weight(request.getWeight())
                .user(user)
                .build();
    }
}
