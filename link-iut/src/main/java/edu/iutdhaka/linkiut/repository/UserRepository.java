package edu.iutdhaka.linkiut.repository;

import edu.iutdhaka.linkiut.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    java.util.List<AppUser> findByEmailStartingWith(String prefix);
}
