package edu.iutdhaka.linkiut.repository;

import edu.iutdhaka.linkiut.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUser_Id(Long userId);

    @Query("SELECT p FROM UserProfile p " +
           "JOIN FETCH p.user " +
           "WHERE p.user.id = :userId")
    Optional<UserProfile> findByUserIdWithDetails(@Param("userId") Long userId);

    @Query("SELECT p FROM UserProfile p " +
           "JOIN FETCH p.user u " +
           "WHERE (:department IS NULL OR :department = '' OR p.department = :department) " +
           "AND (:batch IS NULL OR :batch = '' OR p.batch = :batch) " +
           "AND (:hostel IS NULL OR :hostel = '' OR p.hostel = :hostel) " +
           "AND (:name IS NULL OR :name = '' OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<UserProfile> searchNetwork(@Param("department") String department,
                                    @Param("batch") String batch,
                                    @Param("hostel") String hostel,
                                    @Param("name") String name);

    List<UserProfile> findByUser_DisplayNameContainingIgnoreCaseOrHeadlineContainingIgnoreCaseOrDepartmentContainingIgnoreCase(String name, String headline, String dept);

    List<UserProfile> findByDepartmentIgnoreCase(String department);
}
