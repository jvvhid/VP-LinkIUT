package edu.iutdhaka.linkiut.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "endorsement", uniqueConstraints = @UniqueConstraint(columnNames = {"profile_id", "endorser_id", "skill"}))
public class Endorsement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private UserProfile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endorser_id", nullable = false)
    private AppUser endorser;

    @Column(nullable = false)
    private String skill;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Endorsement() {}

    public Endorsement(UserProfile profile, AppUser endorser, String skill) {
        this.profile = profile;
        this.endorser = endorser;
        this.skill = skill;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UserProfile getProfile() { return profile; }
    public void setProfile(UserProfile profile) { this.profile = profile; }
    
    public AppUser getEndorser() { return endorser; }
    public void setEndorser(AppUser endorser) { this.endorser = endorser; }
    
    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
