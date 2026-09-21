package edu.iutdhaka.linkiut.repository;

import edu.iutdhaka.linkiut.model.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT r FROM Recommendation r JOIN FETCH r.author JOIN FETCH r.receiver WHERE r.receiver.id = :receiverId AND r.approved = true ORDER BY r.createdAt DESC")
    List<Recommendation> findByReceiver_IdAndApprovedTrueOrderByCreatedAtDesc(Long receiverId);
    
    @org.springframework.data.jpa.repository.Query("SELECT r FROM Recommendation r JOIN FETCH r.author JOIN FETCH r.receiver WHERE r.receiver.id = :receiverId AND r.approved = false ORDER BY r.createdAt DESC")
    List<Recommendation> findByReceiver_IdAndApprovedFalseOrderByCreatedAtDesc(Long receiverId);
    
    @org.springframework.data.jpa.repository.Query("SELECT r FROM Recommendation r JOIN FETCH r.author JOIN FETCH r.receiver WHERE r.author.id = :authorId ORDER BY r.createdAt DESC")
    List<Recommendation> findByAuthor_IdOrderByCreatedAtDesc(Long authorId);
}
