package com.dangdangsalon.domain.chat.repository;

import com.dangdangsalon.domain.chat.entity.ChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.w3c.dom.stylesheets.LinkStyle;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT cr FROM ChatRoom cr JOIN FETCH cr.groomer gr JOIN FETCH cr.customer cu WHERE gr.id = :groomerId OR cu.id = :customerId")
    List<ChatRoom> findByGroomerIdOrCustomerId(Long groomerId, Long customerId);

    @Query("SELECT cr FROM ChatRoom cr JOIN FETCH cr.groomer gr JOIN FETCH cr.customer cu WHERE cu.id = :customerId AND cr.customerLeft = false")
    List<ChatRoom> findByCustomerId(Long customerId);

    @Query("SELECT cr FROM ChatRoom cr JOIN FETCH cr.groomer gr JOIN FETCH cr.customer cu WHERE gr.id = :groomerId AND cr.groomerLeft = false")
    List<ChatRoom> findByGroomerId(Long groomerId);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.estimate.id = :estimateId")
    Optional<ChatRoom> findByEstimateId(Long estimateId);
}
