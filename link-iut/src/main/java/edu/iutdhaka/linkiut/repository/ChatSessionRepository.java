package edu.iutdhaka.linkiut.repository;

import edu.iutdhaka.linkiut.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    /**
     * Finds ACTIVE sessions whose SLA deadline has passed.
     * Used by ChatTimerScheduler every 60 seconds.
     */
    List<ChatSession> findByStatusAndSlaDeadlineBefore(
            ChatSession.Status status, LocalDateTime deadline);

    /**
     * Bulk-expire overdue sessions in a single UPDATE (more efficient than loading entities).
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ChatSession c SET c.status = 'EXPIRED' " +
           "WHERE c.status = 'ACTIVE' AND c.slaDeadline < :now")
    int expireOverdueSessions(@Param("now") LocalDateTime now);

    /**
     * Find existing session between two users (either direction).
     */
    @Query("SELECT c FROM ChatSession c " +
           "WHERE c.status = 'ACTIVE' " +
           "AND ((c.initiator.id = :user1 AND c.responder.id = :user2) " +
           "  OR (c.initiator.id = :user2 AND c.responder.id = :user1))")
    Optional<ChatSession> findActiveSessionBetween(
            @Param("user1") Long user1, @Param("user2") Long user2);

    /**
     * All sessions for a given user (as initiator or responder).
     */
    @Query("SELECT c FROM ChatSession c " +
           "JOIN FETCH c.initiator JOIN FETCH c.responder " +
           "WHERE c.initiator.id = :userId OR c.responder.id = :userId " +
           "ORDER BY c.createdAt DESC")
    List<ChatSession> findAllByUser(@Param("userId") Long userId);
}
