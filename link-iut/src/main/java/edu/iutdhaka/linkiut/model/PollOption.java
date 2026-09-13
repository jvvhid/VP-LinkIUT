package edu.iutdhaka.linkiut.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "poll_options")
public class PollOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private int voteCount = 0;

    @OneToMany(mappedBy = "pollOption", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PollVote> votes = new ArrayList<>();

    public PollOption() {}

    public PollOption(String text) {
        this.text = text;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Poll getPoll() { return poll; }
    public void setPoll(Poll poll) { this.poll = poll; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public int getVoteCount() { return voteCount; }
    public void setVoteCount(int voteCount) { this.voteCount = voteCount; }
    public List<PollVote> getVotes() { return votes; }
    public void setVotes(List<PollVote> votes) { this.votes = votes; }

    public void incrementVote() { this.voteCount++; }
    public void decrementVote() { this.voteCount--; }
}
