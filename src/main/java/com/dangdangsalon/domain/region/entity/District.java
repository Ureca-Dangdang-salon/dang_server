package com.dangdangsalon.domain.region.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "district")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    @Builder
    public District(String name, City city) {
        this.name = name;
        this.city = city;
    }
}