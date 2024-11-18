package com.dangdangsalon.domain.orders.entity;

import com.dangdangsalon.config.base.BaseEntity;
import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Orders extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_name")
    private String orderName;

    @Column(name = "amount_value")
    private int amountValue;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToOne
    @JoinColumn(name = "estimate_id")
    private Estimate estimate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Orders(String orderName, int amountValue, OrderStatus status, Estimate estimate, User user) {
        this.orderName = orderName;
        this.amountValue = amountValue;
        this.status = status;
        this.estimate = estimate;
        this.user = user;
    }
}
