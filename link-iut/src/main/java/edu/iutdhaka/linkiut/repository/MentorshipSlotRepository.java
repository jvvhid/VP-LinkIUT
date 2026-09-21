package edu.iutdhaka.linkiut.repository;

import edu.iutdhaka.linkiut.model.MentorshipSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorshipSlotRepository extends JpaRepository<MentorshipSlot, Long> {
    List<MentorshipSlot> findByMentor_IdOrderByStartTimeAsc(Long mentorId);
    List<MentorshipSlot> findByMentee_IdOrderByStartTimeAsc(Long menteeId);
    List<MentorshipSlot> findByStatusOrderByStartTimeAsc(MentorshipSlot.SlotStatus status);
}
