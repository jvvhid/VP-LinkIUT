package edu.iutdhaka.linkiut.repository;

import edu.iutdhaka.linkiut.model.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByActiveTrueOrderByCreatedAtDesc();
    List<JobPosting> findByPostedBy_IdOrderByCreatedAtDesc(Long userId);
    List<JobPosting> findByTitleContainingIgnoreCaseOrCompanyContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String t, String c, String d);
}
