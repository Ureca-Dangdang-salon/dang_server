package com.dangdangsalon.domain.dogprofile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class DogAge {

    @Column(name = "dog_year")
    private int year;
    @Column(name = "dog_month")
    private int month;

    public DogAge(int year, int month) {
        this.year = year;
        this.month = month;
    }
}
