package com.dangdangsalon.domain.groomerprofile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class GroomerDetails {

    private String businessNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String startChat;

    private String address;

    @Column(columnDefinition = "TEXT")
    private String faq;

    @Builder
    public GroomerDetails(String businessNumber, String description, String startChat,
                          String address,
                          String faq) {
        this.businessNumber = businessNumber;
        this.description = description;
        this.startChat = startChat;
        this.address = address;
        this.faq = faq;
    }
}
