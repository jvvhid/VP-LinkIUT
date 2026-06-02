package edu.iutdhaka.linkiut.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "opportunity")
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpportunityType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by", nullable = false)
    private AppUser postedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public enum OpportunityType {
        JOB, INTERNSHIP, MENTORSHIP, JOURNEY, PROJECT_INSIGHT, APP_BUILD
    }

    // ── Constructors ─────────────────────────────────────────
    public Opportunity() {}

    // ── Getters & Setters ────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public OpportunityType getType() { return type; }
    public void setType(OpportunityType type) { this.type = type; }

    public AppUser getPostedBy() { return postedBy; }
    public void setPostedBy(AppUser postedBy) { this.postedBy = postedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    @Column(name = "archive_at")
    private LocalDateTime archiveAt;

    public LocalDateTime getArchiveAt() { return archiveAt; }
    public void setArchiveAt(LocalDateTime archiveAt) { this.archiveAt = archiveAt; }

    public boolean isArchived() {
        return archiveAt != null && archiveAt.isBefore(LocalDateTime.now());
    }

    /** CSS class helper based on type */
    public String getTypeBadgeClass() {
        return switch (type) {
            case JOB             -> "badge-job";
            case INTERNSHIP      -> "badge-internship";
            case MENTORSHIP      -> "badge-mentorship";
            case JOURNEY         -> "badge-journey";
            case PROJECT_INSIGHT -> "badge-insight";
            case APP_BUILD       -> "badge-app";
        };
    }

    /** Human-readable time-ago string */
    public String getTimeAgo() {
        if (createdAt == null) return "";
        var now = LocalDateTime.now();
        var diff = java.time.Duration.between(createdAt, now);
        if (diff.toMinutes() < 1) return "just now";
        if (diff.toHours() < 1) return diff.toMinutes() + "m ago";
        if (diff.toDays() < 1) return diff.toHours() + "h ago";
        return diff.toDays() + "d ago";
    }
}
