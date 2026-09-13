package edu.iutdhaka.linkiut.repository;

import edu.iutdhaka.linkiut.model.Endorsement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EndorsementRepository extends JpaRepository<Endorsement, Long> {
    
    @Query("SELECT e.skill, COUNT(e) FROM Endorsement e WHERE e.profile.id = :profileId GROUP BY e.skill")
    List<Object[]> countBySkillForProfile(Long profileId);
    
    boolean existsByProfileIdAndEndorserIdAndSkill(Long profileId, Long endorserId, String skill);
    
    long countByProfileIdAndSkill(Long profileId, String skill);
}
