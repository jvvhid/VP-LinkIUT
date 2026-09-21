package edu.iutdhaka.linkiut.repository;

import edu.iutdhaka.linkiut.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByApplicant_IdOrderByAppliedAtDesc(Long applicantId);
    List<JobApplication> findByJobPosting_IdOrderByAppliedAtDesc(Long jobPostingId);
    Optional<JobApplication> findByJobPosting_IdAndApplicant_Id(Long jobPostingId, Long applicantId);
    boolean existsByJobPosting_IdAndApplicant_Id(Long jobPostingId, Long applicantId);
}
