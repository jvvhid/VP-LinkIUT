package edu.iutdhaka.linkiut.repository;

import edu.iutdhaka.linkiut.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Fetches messages for a chat session ordered chronologically,
     * with sender eagerly loaded to prevent N+1 on chat rendering.
     */
    @Query("SELECT m FROM Message m JOIN FETCH m.sender " +
           "WHERE m.chatSession.id = :sessionId ORDER BY m.sentAt ASC")
    List<Message> findByChatSessionIdWithSender(@Param("sessionId") Long sessionId);

    List<Message> findByChatSession_IdOrderBySentAtAsc(Long sessionId);

    /**
     * For HTMX polling: fetch only messages after a given ID.
     */
    @Query("SELECT m FROM Message m JOIN FETCH m.sender " +
           "WHERE m.chatSession.id = :sessionId AND m.id > :afterId " +
           "ORDER BY m.sentAt ASC")
    List<Message> findNewMessages(@Param("sessionId") Long sessionId, @Param("afterId") Long afterId);
}
