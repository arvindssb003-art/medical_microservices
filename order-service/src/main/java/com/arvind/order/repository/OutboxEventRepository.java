package com.arvind.order.repository;

import com.arvind.order.entity.OutboxEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    @Query("""
            select event from OutboxEvent event
            where event.publishedAt is null
              and (event.nextAttemptAt is null or event.nextAttemptAt <= :now)
            order by event.createdAt
            """)
    List<OutboxEvent> findReadyToPublish(
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select event from OutboxEvent event where event.id = :id")
    Optional<OutboxEvent> findByIdForUpdate(@Param("id") String id);
}
