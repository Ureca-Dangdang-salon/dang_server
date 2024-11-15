package com.dangdangsalon.domain.estimate.entity;

import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "estimate_service_price")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EstimateServicePrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_id")
    private Long id;

    private int price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estimate_id")
    private Estimate estimate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private GroomerService groomerService;

    @Builder
    public EstimateServicePrice(int price, Estimate estimate, GroomerService groomerService) {
        this.price = price;
        this.estimate = estimate;
        this.groomerService = groomerService;
    }
}
