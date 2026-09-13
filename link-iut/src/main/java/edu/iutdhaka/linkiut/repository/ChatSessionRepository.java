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
           "WHERE c.status = 'ACTIVE' AND c.slaDeadline < :now AND c.isGroup = false " +
           "AND NOT EXISTS (SELECT m FROM Message m WHERE m.chatSession = c)")
    int expireOverdueSessions(@Param("now") LocalDateTime now);

    /**
     * Find existing session between two users (either direction).
     */
    @Query("SELECT c FROM ChatSession c JOIN c.participants p1 JOIN c.participants p2 " +
           "WHERE c.status = 'ACTIVE' AND c.isGroup = false " +
           "AND p1.id = :user1 AND p2.id = :user2")
    Optional<ChatSession> findActiveSessionBetween(
            @Param("user1") Long user1, @Param("user2") Long user2);

    /**
     * All sessions for a given user (as initiator or responder).
     */
    @Query("SELECT DISTINCT c FROM ChatSession c " +
           "LEFT JOIN FETCH c.participants " +
           "WHERE c IN (SELECT c2 FROM ChatSession c2 JOIN c2.participants p WHERE p.id = :userId) " +
           "ORDER BY c.createdAt DESC")
    List<ChatSession> findAllByUser(@Param("userId") Long userId);

    /**
     * Find session by ID with eagerly loaded participants.
     */
    @Query("SELECT c FROM ChatSession c " +
           "LEFT JOIN FETCH c.participants " +
           "WHERE c.id = :id")
    Optional<ChatSession> findByIdWithParticipants(@Param("id") Long id);
}
