package edu.iutdhaka.linkiut.repository;

import edu.iutdhaka.linkiut.model.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {
}
