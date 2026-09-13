package edu.iutdhaka.linkiut.repository;

import edu.iutdhaka.linkiut.model.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollOptionRepository extends JpaRepository<PollOption, Long> {
}
