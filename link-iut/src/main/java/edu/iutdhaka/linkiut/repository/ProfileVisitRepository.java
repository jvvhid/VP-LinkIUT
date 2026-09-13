package edu.iutdhaka.linkiut.repository;

import edu.iutdhaka.linkiut.model.ProfileVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileVisitRepository extends JpaRepository<ProfileVisit, Long> {
    long countByProfileId(Long profileId);
    boolean existsByProfileIdAndViewerId(Long profileId, Long viewerId);
}
