package com.kanban.repository;

import com.kanban.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    List<Project> findByOwnerIdOrderByCreatedAtAsc(Long ownerId);
    
    List<Project> findByStatusOrderByCreatedAtAsc(Project.ProjectStatus status);
    
    @Query("SELECT p FROM Project p WHERE p.owner.id = :ownerId AND p.status = :status")
    List<Project> findByOwnerIdAndStatus(Long ownerId, Project.ProjectStatus status);
    
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = 'ACTIVE'")
    long countActiveProjects();
}
