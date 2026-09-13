package edu.iutdhaka.linkiut.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user; // User receiving the notification

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private AppUser actor; // User who triggered the action

    @Column(nullable = false)
    private String type; // e.g. LIKE, COMMENT, CONNECTION_REQUEST, CONNECTION_ACCEPT

    @Column(nullable = false)
    private String message;

    private Long entityId; // The ID of the post/comment/connection related to this

    private String link; // URL to redirect when clicked

    private boolean isRead = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification() {}

    public Notification(AppUser user, AppUser actor, String type, String message, String link) {
        this.user = user;
        this.actor = actor;
        this.type = type;
        this.message = message;
        this.link = link;
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public AppUser getActor() { return actor; }
    public void setActor(AppUser actor) { this.actor = actor; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
