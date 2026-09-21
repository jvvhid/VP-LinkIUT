package edu.iutdhaka.linkiut.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mentorship_slots")
@Getter
@Setter
@NoArgsConstructor
public class MentorshipSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private AppUser mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id")
    private AppUser mentee;

    @Column(nullable = false)
    private String topic; // e.g., Resume Review, System Design, Career Advice

    private String description;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private int durationMinutes = 30;

    private String meetingLink;

    @Enumerated(EnumType.STRING)
    private SlotStatus status = SlotStatus.AVAILABLE;

    @Column(columnDefinition = "TEXT")
    private String studentNote;

    public enum SlotStatus {
        AVAILABLE,
        REQUESTED,
        BOOKED,
        COMPLETED,
        CANCELLED
    }
}
