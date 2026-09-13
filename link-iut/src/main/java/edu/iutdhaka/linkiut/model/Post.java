package edu.iutdhaka.linkiut.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private AppUser author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;  // Phase 4 — Instagram image support

    public List<String> getMediaUrls() {
        if (imageUrl == null || imageUrl.isBlank()) return new ArrayList<>();
        return java.util.Arrays.asList(imageUrl.split(","));
    }

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_opportunity", nullable = false)
    private boolean isOpportunity = false;

    @Column(name = "opportunity_title")
    private String opportunityTitle;

    @Column(name = "opportunity_type")
    private String opportunityType; // JOB, INTERNSHIP, MENTORSHIP, THESIS_MATCHMAKING

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PostLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<Comment> comments = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "poll_id")
    private Poll poll;

    public Post() {}

    public Post(AppUser author, String content) {
        this.author = author;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public AppUser getAuthor() { return author; }
    public void setAuthor(AppUser author) { this.author = author; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isOpportunity() { return isOpportunity; }
    public void setOpportunity(boolean opportunity) { isOpportunity = opportunity; }

    public String getOpportunityTitle() { return opportunityTitle; }
    public void setOpportunityTitle(String opportunityTitle) { this.opportunityTitle = opportunityTitle; }

    public String getOpportunityType() { return opportunityType; }
    public void setOpportunityType(String opportunityType) { this.opportunityType = opportunityType; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<PostLike> getLikes() { return likes; }
    public void setLikes(List<PostLike> likes) { this.likes = likes; }
    
    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }

    public int getLikeCount() {
        if (likes == null) return 0;
        return (int) likes.stream().filter(l -> !l.isDislike()).count();
    }
    
    public int getDislikeCount() {
        if (likes == null) return 0;
        return (int) likes.stream().filter(PostLike::isDislike).count();
    }

    public int getCommentCount() {
        return comments != null ? comments.size() : 0;
    }

    public boolean isLikedBy(Long userId) {
        if (likes == null) return false;
        return likes.stream().anyMatch(like -> like.getUser().getId().equals(userId) && !like.isDislike());
    }
    
    public boolean isDislikedBy(Long userId) {
        if (likes == null) return false;
        return likes.stream().anyMatch(like -> like.getUser().getId().equals(userId) && like.isDislike());
    }

    public String getTimeAgo() {
        if (createdAt == null) return "";
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(createdAt, now);
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m ago";
        long hours = ChronoUnit.HOURS.between(createdAt, now);
        if (hours < 24) return hours + "h ago";
        long days = ChronoUnit.DAYS.between(createdAt, now);
        return days + "d ago";
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }
}
