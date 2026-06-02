package edu.iutdhaka.linkiut.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    private String headline;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String department;

    private String batch;

    @Column(name = "current_company")
    private String currentCompany;

    private String location;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("startDate DESC")
    private java.util.Set<Experience> experiences = new java.util.LinkedHashSet<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private java.util.Set<Project> projects = new java.util.LinkedHashSet<>();

    // ── Constructors ─────────────────────────────────────────
    public UserProfile() {}

    // ── Getters & Setters ────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }

    public String getCurrentCompany() { return currentCompany; }
    public void setCurrentCompany(String currentCompany) { this.currentCompany = currentCompany; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public java.util.Set<Experience> getExperiences() { return experiences; }
    public void setExperiences(java.util.Set<Experience> experiences) { this.experiences = experiences; }

    public java.util.Set<Project> getProjects() { return projects; }
    public void setProjects(java.util.Set<Project> projects) { this.projects = projects; }
}
