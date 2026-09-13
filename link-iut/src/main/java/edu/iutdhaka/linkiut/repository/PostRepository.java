package edu.iutdhaka.linkiut.repository;

import edu.iutdhaka.linkiut.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p JOIN FETCH p.author ORDER BY p.createdAt DESC")
    List<Post> findAllWithAuthor();

    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.author.id = :authorId ORDER BY p.createdAt DESC")
    List<Post> findByAuthorId(Long authorId);

    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.createdAt DESC")
    List<Post> searchByContent(String query);
}
