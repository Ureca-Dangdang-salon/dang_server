package com.dangdangsalon.domain.chat.repository;

import com.dangdangsalon.domain.chat.entity.ChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.w3c.dom.stylesheets.LinkStyle;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT cr FROM ChatRoom cr JOIN FETCH cr.groomerProfile gp JOIN FETCH cr.user cu WHERE gp.id = :groomerProfileId OR cu.id = :customerId")
    List<ChatRoom> findByGroomerProfileIdOrCustomerId(Long groomerProfileId, Long customerId);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.estimate.id = :estimateId")
    Optional<ChatRoom> findByEstimateId(Long estimateId);
}
