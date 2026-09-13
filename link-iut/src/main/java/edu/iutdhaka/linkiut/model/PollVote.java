package edu.iutdhaka.linkiut.model;

import jakarta.persistence.*;

@Entity
@Table(name = "poll_votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"poll_id", "user_id"})
})
public class PollVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_option_id", nullable = false)
    private PollOption pollOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    public PollVote() {}

    public PollVote(Poll poll, PollOption pollOption, AppUser user) {
        this.poll = poll;
        this.pollOption = pollOption;
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Poll getPoll() { return poll; }
    public void setPoll(Poll poll) { this.poll = poll; }
    public PollOption getPollOption() { return pollOption; }
    public void setPollOption(PollOption pollOption) { this.pollOption = pollOption; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
}
