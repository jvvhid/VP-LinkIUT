package edu.iutdhaka.linkiut.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications")
@Getter
@Setter
@NoArgsConstructor
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private AppUser applicant;

    @Column(columnDefinition = "TEXT")
    private String coverNote;

    private String resumeUrl; // Uploaded resume path or profile link

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    private LocalDateTime appliedAt;

    public enum ApplicationStatus {
        APPLIED,
        SHORTLISTED,
        INTERVIEWING,
        HIRED,
        REJECTED
    }

    @PrePersist
    public void prePersist() {
        this.appliedAt = LocalDateTime.now();
    }
}
