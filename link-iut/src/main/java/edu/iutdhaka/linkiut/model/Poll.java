package edu.iutdhaka.linkiut.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "polls")
public class Poll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "poll")
    private Post post;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PollOption> options = new ArrayList<>();

    public Poll() {}

    public Poll(String question) {
        this.question = question;
    }

    public void addOption(PollOption option) {
        options.add(option);
        option.setPoll(this);
    }

    public void removeOption(PollOption option) {
        options.remove(option);
        option.setPoll(null);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public List<PollOption> getOptions() { return options; }
    public void setOptions(List<PollOption> options) { this.options = options; }

    public int getTotalVotes() {
        return options.stream().mapToInt(PollOption::getVoteCount).sum();
    }

    public boolean hasVotedBy(Long userId) {
        if (options == null) return false;
        return options.stream().anyMatch(opt -> opt.getVotes().stream().anyMatch(v -> v.getUser().getId().equals(userId)));
    }
}
