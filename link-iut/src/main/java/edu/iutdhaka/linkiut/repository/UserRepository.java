package edu.iutdhaka.linkiut.repository;

import edu.iutdhaka.linkiut.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Searches users by display name or email (case-insensitive partial match).
     */
    @Query("SELECT u FROM AppUser u WHERE " +
           "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<AppUser> searchByKeyword(@Param("keyword") String keyword);
}

