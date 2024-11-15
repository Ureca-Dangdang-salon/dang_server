package com.dangdangsalon.domain.dogprofile.entity;
import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dog_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DogProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long id;

    private String name;

    private String species;

    @Column(name = "age_yaer")
    private int ageYear;

    @Column(name = "age_month")
    private int ageMonth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Neutering neutering;

    private int weight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public DogProfile(String name, String species, int ageYear, int ageMonth, Gender gender, Neutering neutering, int weight, User user) {
        this.name = name;
        this.species = species;
        this.ageYear = ageYear;
        this.ageMonth = ageMonth;
        this.gender = gender;
        this.neutering = neutering;
        this.weight = weight;
        this.user = user;
    }
}
