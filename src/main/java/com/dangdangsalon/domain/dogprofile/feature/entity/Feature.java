package com.dangdangsalon.domain.dogprofile.feature.entity;

import com.dangdangsalon.config.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "feature")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feature extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feature_id")
    private Long id;

    private String description;

    @Column(name = "is_custom")
    private boolean isCustom;

    @Builder
    public Feature(String description, boolean isCustom) {
        this.description = description;
        this.isCustom = isCustom;
    }
}

