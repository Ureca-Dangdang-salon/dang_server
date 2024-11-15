package com.dangdangsalon.domain.dogprofile.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class DogAge {

    private int year;
    private int month;

    public DogAge(int year, int month) {
        this.year = year;
        this.month = month;
    }
}
