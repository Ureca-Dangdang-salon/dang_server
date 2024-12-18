package com.dangdangsalon.domain.notification.repository;

import com.dangdangsalon.domain.notification.entity.Topic;
import com.dangdangsalon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    Optional<Topic> findByTopicNameAndUser(String topicName, User user);
}
