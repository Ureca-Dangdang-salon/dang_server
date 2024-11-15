package com.dangdangsalon.domain.orders.entity;

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
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(name = "order_name")
    private String orderName;

    @Column(name = "amount_value")
    private int amountValue;

    private OrderStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "estimate_id")
    private Estimate estimate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Orders(String orderName, int amountValue, OrderStatus status,
                  LocalDateTime createdAt, Estimate estimate, User user) {
        this.orderName = orderName;
        this.amountValue = amountValue;
        this.status = status;
        this.createdAt = createdAt;
        this.estimate = estimate;
        this.user = user;
    }
}
