package edu.iutdhaka.linkiut.repository;

import edu.iutdhaka.linkiut.model.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    /**
     * Fetches all opportunities WITH the poster (AppUser) eagerly joined.
     * This prevents the N+1 SELECT problem when rendering the feed.
     * Without JOIN FETCH, Hibernate would fire one extra SELECT per opportunity
     * to load postedBy lazily.
     */
    @Query("SELECT o FROM Opportunity o JOIN FETCH o.postedBy ORDER BY o.createdAt DESC")
    List<Opportunity> findAllWithPoster();

    /**
     * Filter by type with JOIN FETCH on poster.
     */
    @Query("SELECT o FROM Opportunity o JOIN FETCH o.postedBy WHERE o.type = :type ORDER BY o.createdAt DESC")
    List<Opportunity> findByTypeWithPoster(@org.springframework.data.repository.query.Param("type") Opportunity.OpportunityType type);

    @Query("SELECT o FROM Opportunity o JOIN FETCH o.postedBy WHERE LOWER(o.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(o.description) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(o.postedBy.displayName) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY o.createdAt DESC")
    List<Opportunity> searchByTitleOrDescription(@org.springframework.data.repository.query.Param("query") String query);

    @Query("SELECT o FROM Opportunity o JOIN FETCH o.postedBy WHERE o.postedBy.id = :posterId AND o.expiresAt > CURRENT_TIMESTAMP ORDER BY o.createdAt DESC")
    List<Opportunity> findByPosterId(@org.springframework.data.repository.query.Param("posterId") Long posterId);
}
