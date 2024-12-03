package com.dangdangsalon.domain.chat.repository;

import com.dangdangsalon.domain.chat.entity.ChatMessage;
import com.dangdangsalon.domain.chat.entity.ChatMessageMongo;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ChatMessageRepository extends MongoRepository<ChatMessageMongo, String> {
    List<ChatMessageMongo> findByRoomIdOrderBySendAtDesc(Long roomId, Pageable pageable);

    void deleteByRoomId(Long roomId);

    List<ChatMessageMongo> findByRoomIdAndSequenceGreaterThanOrderBySequenceAsc(Long roomId, Long lastReadSequence);

    /*
        roomId가 ?0 (첫 파라미터)와 같은 값을 가진다.
        sequence가 ?1 (두번째 파라미터)보다 작은 값($lt)만 가져온다.
        sort=> -1: 내림차순 / 1: 오름차순
     */
//    @Query(value = "{ 'roomId': ?0, 'sequence': { $lt: ?1 } }", sort = "{ 'sequence': -1 }")
//    List<ChatMessageMongo> findByRoomIdAndSequenceLessThanOrderBySequenceAsc(Long roomId, Long sequence, int limit);

    List<ChatMessageMongo> findTopByRoomIdOrderBySequenceDesc(Long roomId, Pageable pageable);
}
