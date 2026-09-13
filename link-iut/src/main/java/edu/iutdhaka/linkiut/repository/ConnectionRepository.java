package edu.iutdhaka.linkiut.repository;

import edu.iutdhaka.linkiut.model.Connection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    Optional<Connection> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId);

    @Query("SELECT c FROM Connection c WHERE c.receiver.id = :userId AND c.status = 'PENDING'")
    List<Connection> findPendingForUser(Long userId);

    @Query("SELECT c FROM Connection c WHERE (c.requester.id = :userId OR c.receiver.id = :userId) AND c.status = 'ACCEPTED'")
    List<Connection> findAcceptedConnections(Long userId);

    @Query("SELECT COUNT(c) FROM Connection c WHERE (c.requester.id = :userId OR c.receiver.id = :userId) AND c.status = 'ACCEPTED'")
    long countAcceptedConnections(Long userId);
    
    @Query("SELECT c FROM Connection c WHERE (c.requester.id = :user1Id AND c.receiver.id = :user2Id) OR (c.requester.id = :user2Id AND c.receiver.id = :user1Id)")
    Optional<Connection> findConnectionBetween(Long user1Id, Long user2Id);
}
