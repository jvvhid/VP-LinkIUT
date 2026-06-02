package edu.iutdhaka.linkiut.service;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.model.Opportunity;
import edu.iutdhaka.linkiut.repository.OpportunityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;

    public OpportunityService(OpportunityRepository opportunityRepository) {
        this.opportunityRepository = opportunityRepository;
    }

    /**
     * Fetches all opportunities with the poster eagerly loaded (JOIN FETCH).
     * This avoids the N+1 SELECT problem when rendering the feed.
     */
    public List<Opportunity> getAllOpportunities() {
        return opportunityRepository.findAllWithPoster();
    }

    public List<Opportunity> getOpportunitiesByType(Opportunity.OpportunityType type) {
        return opportunityRepository.findByTypeWithPoster(type);
    }

    @Transactional
    public Opportunity createOpportunity(String title, String description,
                                          Opportunity.OpportunityType type,
                                          AppUser postedBy,
                                          Integer archiveInHours) {
        Opportunity opp = new Opportunity();
        opp.setTitle(title);
        opp.setDescription(description);
        opp.setType(type);
        opp.setPostedBy(postedBy);
        opp.setCreatedAt(LocalDateTime.now());
        opp.setExpiresAt(LocalDateTime.now().plusDays(30));
        
        if (archiveInHours != null && archiveInHours > 0) {
            opp.setArchiveAt(LocalDateTime.now().plusHours(archiveInHours));
        }
        
        return opportunityRepository.save(opp);
    }
}
