package edu.iutdhaka.linkiut.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_postings")
@Getter
@Setter
@NoArgsConstructor
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobType jobType; // FULL_TIME, INTERNSHIP, PART_TIME, CONTRACT, REMOTE

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    private String salaryRange;

    @Column(columnDefinition = "TEXT")
    private String requiredSkills; // Comma-separated skills e.g. "Java, Spring Boot, SQL"

    public String[] getRequiredSkillList() {
        if (requiredSkills == null || requiredSkills.isBlank()) return new String[0];
        return requiredSkills.split("\\s*,\\s*");
    }

    private String experienceLevel; // Entry-level, Mid-level, Senior

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by_id", nullable = false)
    private AppUser postedBy;

    private LocalDateTime createdAt;

    private boolean active = true;

    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobApplication> applications = new ArrayList<>();

    public enum JobType {
        FULL_TIME,
        INTERNSHIP,
        PART_TIME,
        CONTRACT,
        REMOTE
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
