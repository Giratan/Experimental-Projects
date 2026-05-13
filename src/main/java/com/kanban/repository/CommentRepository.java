package com.kanban.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kanban.model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByTaskIdOrderByCreatedAtAsc(Long taskId);

    List<Comment> findByAuthorIdOrderByCreatedAtAsc(Long authorId);

    List<Comment> findByTaskIdAndAuthorId(Long taskId, Long authorId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.task.id = :taskId")
    long countCommentsByTask(@Param("taskId") Long taskId);

    @Query("SELECT c FROM Comment c WHERE c.task.id = :taskId ORDER BY c.createdAt DESC")
    List<Comment> findLatestCommentsByTask(@Param("taskId") Long taskId);

    void deleteByTaskId(Long taskId);
}
