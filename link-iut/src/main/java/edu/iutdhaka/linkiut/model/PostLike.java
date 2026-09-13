package edu.iutdhaka.linkiut.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_like", uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
public class PostLike {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_dislike", nullable = false, columnDefinition = "boolean default false")
    private boolean isDislike = false;

    public PostLike() {}

    public PostLike(Post post, AppUser user) {
        this.post = post;
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.isDislike = false;
    }

    public PostLike(Post post, AppUser user, boolean isDislike) {
        this.post = post;
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.isDislike = isDislike;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public boolean isDislike() { return isDislike; }
    public void setDislike(boolean isDislike) { this.isDislike = isDislike; }
}
