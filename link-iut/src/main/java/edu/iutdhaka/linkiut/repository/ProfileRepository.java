package edu.iutdhaka.linkiut.repository;

import edu.iutdhaka.linkiut.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUser_Id(Long userId);

    @Query("SELECT p FROM UserProfile p " +
           "LEFT JOIN FETCH p.experiences " +
           "LEFT JOIN FETCH p.projects " +
           "JOIN FETCH p.user " +
           "WHERE p.user.id = :userId")
    Optional<UserProfile> findByUserIdWithDetails(@Param("userId") Long userId);
}
