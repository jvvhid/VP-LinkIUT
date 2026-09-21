package edu.iutdhaka.linkiut.repository;

import edu.iutdhaka.linkiut.model.PostReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {
    List<PostReaction> findByPost_Id(Long postId);
    Optional<PostReaction> findByPost_IdAndUser_Id(Long postId, Long userId);
    long countByPost_IdAndReactionType(Long postId, PostReaction.ReactionType reactionType);
}
