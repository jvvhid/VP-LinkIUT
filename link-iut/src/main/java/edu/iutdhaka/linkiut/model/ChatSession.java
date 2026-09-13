package edu.iutdhaka.linkiut.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "chat_session")
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_group", nullable = false)
    private boolean isGroup = false;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "group_cover_photo")
    private String groupCoverPhoto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private AppUser admin;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "chat_session_participants",
        joinColumns = @JoinColumn(name = "chat_session_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<AppUser> participants = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "sla_deadline", nullable = false)
    private LocalDateTime slaDeadline;

    @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sentAt ASC")
    private List<Message> messages = new ArrayList<>();

    public enum Status {
        ACTIVE, EXPIRED, CLOSED
    }

    // ── Constructors ─────────────────────────────────────────
    public ChatSession() {}

    public ChatSession(AppUser user1, AppUser user2) {
        this.isGroup = false;
        this.participants.add(user1);
        this.participants.add(user2);
        this.slaDeadline = LocalDateTime.now().plusHours(48);
    }

    public ChatSession(String groupName, String groupCoverPhoto, AppUser admin, Set<AppUser> initialParticipants) {
        this.isGroup = true;
        this.groupName = groupName;
        this.groupCoverPhoto = groupCoverPhoto;
        this.admin = admin;
        this.participants.addAll(initialParticipants);
        this.participants.add(admin); // Ensure admin is in the group
        this.slaDeadline = LocalDateTime.now().plusHours(48);
    }

    // ── Getters & Setters ────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public boolean isGroup() { return isGroup; }
    public void setGroup(boolean group) { isGroup = group; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getGroupCoverPhoto() { return groupCoverPhoto; }
    public void setGroupCoverPhoto(String groupCoverPhoto) { this.groupCoverPhoto = groupCoverPhoto; }

    public AppUser getAdmin() { return admin; }
    public void setAdmin(AppUser admin) { this.admin = admin; }

    public Set<AppUser> getParticipants() { return participants; }
    public void setParticipants(Set<AppUser> participants) { this.participants = participants; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getSlaDeadline() { return slaDeadline; }
    public void setSlaDeadline(LocalDateTime slaDeadline) { this.slaDeadline = slaDeadline; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    /** Returns the other participant for 1-on-1 chats. If group, returns null or could return admin. */
    public AppUser getOtherParticipant(Long userId) {
        if (isGroup) return null;
        for (AppUser p : participants) {
            if (!p.getId().equals(userId)) {
                return p;
            }
        }
        // If chat with self
        return participants.stream().findFirst().orElse(null);
    }

    /** Returns remaining seconds until SLA deadline (negative if expired) */
    public long getRemainingSeconds() {
        return java.time.Duration.between(LocalDateTime.now(), slaDeadline).getSeconds();
    }

    public boolean isExpired() {
        return status == Status.EXPIRED || LocalDateTime.now().isAfter(slaDeadline);
    }
}
