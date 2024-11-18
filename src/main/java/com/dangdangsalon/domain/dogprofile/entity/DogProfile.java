package com.dangdangsalon.domain.dogprofile.entity;
import com.dangdangsalon.config.base.BaseEntity;
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
}
