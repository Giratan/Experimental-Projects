package com.kanban.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kanban.model.Task;
import com.kanban.model.Task.DayOfWeek;
import com.kanban.model.Task.TaskStatus;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    @Query("SELECT t FROM Task t WHERE t.dayOfWeek = :dayOfWeek ORDER BY t.createdAt ASC")
    List<Task> findByDayOfWeekOrderByCreatedAtAsc(@Param("dayOfWeek") DayOfWeek dayOfWeek);
    
    List<Task> findByAssignedUserIdOrderByCreatedAtAsc(Long userId);
    
    List<Task> findByStatusOrderByCreatedAtAsc(TaskStatus status);
    
    List<Task> findByDayOfWeekAndStatusOrderByCreatedAtAsc(DayOfWeek dayOfWeek, TaskStatus status);
    
    @Query("SELECT t FROM Task t WHERE t.deadline < :now AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasks(@Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM Task t WHERE t.deadline BETWEEN :now AND :deadline AND t.status != 'COMPLETED'")
    List<Task> findCloseToDeadlineTasks(@Param("now") LocalDateTime now, @Param("deadline") LocalDateTime deadline);
    
    @Query("SELECT t FROM Task t WHERE t.assignedUser.id = :userId AND t.deadline < :now AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasksByUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.dayOfWeek = :dayOfWeek AND t.status != 'COMPLETED'")
    long countActiveTasksByDay(@Param("dayOfWeek") DayOfWeek dayOfWeek);
    
    @Query("SELECT t FROM Task t WHERE t.title LIKE %:keyword% OR t.description LIKE %:keyword%")
    List<Task> searchTasks(@Param("keyword") String keyword);
    
    // Native SQL query example
    @Query(value = "SELECT DAYOFWEEK(created_at) as day, COUNT(*) as count FROM tasks " +
                   "WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                   "GROUP BY DAYOFWEEK(created_at) ORDER BY day", nativeQuery = true)
    List<Object[]> getTaskStatisticsByDay();
}
