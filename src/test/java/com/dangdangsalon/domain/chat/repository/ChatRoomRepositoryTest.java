package com.dangdangsalon.domain.chat.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.dangdangsalon.domain.chat.entity.ChatRoom;
import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
class ChatRoomRepositoryTest {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("Customer Id로 ChatRoom 조회")
    void testFindByCustomerId() {
        User customer = User.builder().name("Customer").build();
        User groomer = User.builder().name("Groomer").build();

        em.persist(customer);
        em.persist(groomer);

        ChatRoom chatRoom = ChatRoom.builder()
                .customer(customer)
                .groomer(groomer)
                .customerLeft(false)
                .groomerLeft(false)
                .build();

        em.persist(chatRoom);

        List<ChatRoom> result = chatRoomRepository.findByCustomerId(customer.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomer().getName()).isEqualTo("Customer");
        assertThat(result.get(0).getGroomer().getName()).isEqualTo("Groomer");
    }

    @Test
    @DisplayName("Groomer ID로 ChatRoom 조회 테스트")
    void testFindByGroomerId() {
        User customer = User.builder().name("Customer").build();
        User groomer = User.builder().name("Groomer").build();

        em.persist(customer);
        em.persist(groomer);

        ChatRoom chatRoom = ChatRoom.builder()
                .customer(customer)
                .groomer(groomer)
                .groomerLeft(false)
                .customerLeft(false)
                .build();

        em.persist(chatRoom);

        List<ChatRoom> result = chatRoomRepository.findByGroomerId(groomer.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomer().getName()).isEqualTo("Customer");
        assertThat(result.get(0).getGroomer().getName()).isEqualTo("Groomer");
    }

    @Test
    @DisplayName("Estimate ID로 ChatRoom 조회 테스트")
    void testFindByEstimateId() {
        User customer = User.builder().name("Customer").build();
        User groomer = User.builder().name("Groomer").build();

        em.persist(customer);
        em.persist(groomer);

        Estimate estimate = Estimate.builder().imageKey("estimate.jpg").build();

        em.persist(estimate);

        ChatRoom chatRoom = ChatRoom.builder()
                .customer(customer)
                .groomer(groomer)
                .estimate(estimate)
                .customerLeft(false)
                .groomerLeft(false)
                .build();

        em.persist(chatRoom);

        Optional<ChatRoom> result = chatRoomRepository.findByEstimateId(estimate.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getCustomer().getName()).isEqualTo("Customer");
        assertThat(result.get().getGroomer().getName()).isEqualTo("Groomer");
    }
}