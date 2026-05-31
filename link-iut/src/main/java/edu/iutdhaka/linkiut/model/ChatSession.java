package edu.iutdhaka.linkiut.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_session")
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private AppUser initiator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responder_id", nullable = false)
    private AppUser responder;

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

    public ChatSession(AppUser initiator, AppUser responder) {
        this.initiator = initiator;
        this.responder = responder;
        this.slaDeadline = LocalDateTime.now().plusHours(48);
    }

    // ── Getters & Setters ────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AppUser getInitiator() { return initiator; }
    public void setInitiator(AppUser initiator) { this.initiator = initiator; }

    public AppUser getResponder() { return responder; }
    public void setResponder(AppUser responder) { this.responder = responder; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getSlaDeadline() { return slaDeadline; }
    public void setSlaDeadline(LocalDateTime slaDeadline) { this.slaDeadline = slaDeadline; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    /** Returns the other participant from the perspective of the given user */
    public AppUser getOtherParticipant(Long userId) {
        return initiator.getId().equals(userId) ? responder : initiator;
    }

    /** Returns remaining seconds until SLA deadline (negative if expired) */
    public long getRemainingSeconds() {
        return java.time.Duration.between(LocalDateTime.now(), slaDeadline).getSeconds();
    }

    public boolean isExpired() {
        return status == Status.EXPIRED || LocalDateTime.now().isAfter(slaDeadline);
    }
}
