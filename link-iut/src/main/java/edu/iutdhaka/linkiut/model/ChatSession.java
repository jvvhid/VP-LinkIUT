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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sentAt ASC")
    private List<Message> messages = new ArrayList<>();

    // ── Constructors ─────────────────────────────────────────
    public ChatSession() {}

    public ChatSession(AppUser initiator, AppUser responder) {
        this.initiator = initiator;
        this.responder = responder;
    }

    // ── Getters & Setters ────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AppUser getInitiator() { return initiator; }
    public void setInitiator(AppUser initiator) { this.initiator = initiator; }

    public AppUser getResponder() { return responder; }
    public void setResponder(AppUser responder) { this.responder = responder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    /** Returns the other participant from the perspective of the given user */
    public AppUser getOtherParticipant(Long userId) {
        return initiator.getId().equals(userId) ? responder : initiator;
    }
}
