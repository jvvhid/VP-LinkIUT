package edu.iutdhaka.linkiut.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "profile_visit", uniqueConstraints = @UniqueConstraint(columnNames = {"profile_id", "viewer_id"}))
public class ProfileVisit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private UserProfile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viewer_id", nullable = false)
    private AppUser viewer;

    @Column(name = "visited_at", nullable = false)
    private LocalDateTime visitedAt = LocalDateTime.now();

    public ProfileVisit() {}

    public ProfileVisit(UserProfile profile, AppUser viewer) {
        this.profile = profile;
        this.viewer = viewer;
        this.visitedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UserProfile getProfile() { return profile; }
    public void setProfile(UserProfile profile) { this.profile = profile; }
    
    public AppUser getViewer() { return viewer; }
    public void setViewer(AppUser viewer) { this.viewer = viewer; }
    
    public LocalDateTime getVisitedAt() { return visitedAt; }
    public void setVisitedAt(LocalDateTime visitedAt) { this.visitedAt = visitedAt; }
}
