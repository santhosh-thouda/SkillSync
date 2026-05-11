package com.capgemini.group.repository;

import com.capgemini.group.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByGroupIdOrderByTimestampAsc(Long groupId);

    @Query("SELECT m FROM Message m WHERE m.groupId = :groupId ORDER BY m.timestamp DESC")
    List<Message> findTopNByGroupIdOrderByTimestampDesc(
            @Param("groupId") Long groupId,
            Pageable pageable
    );
}
