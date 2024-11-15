package com.dangdangsalon.domain.groomerprofile.entity;

import com.dangdangsalon.config.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "badge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Badge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String imageKey;

    @Builder
    public Badge(String name, String imageKey) {
        this.name = name;
        this.imageKey = imageKey;
    }
}
