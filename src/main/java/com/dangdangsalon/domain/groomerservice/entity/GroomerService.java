package com.dangdangsalon.domain.groomerservice.entity;


import com.dangdangsalon.config.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "service")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroomerService extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Column(name = "is_custom")
    private Boolean isCustom;

    @Builder
    public GroomerService(String description, Boolean isCustom) {
        this.description = description;
        this.isCustom = isCustom;
    }
}
