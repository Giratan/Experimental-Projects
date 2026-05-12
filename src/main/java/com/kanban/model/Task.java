package com.kanban.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.ACTIVE;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;
    
    @Column(name = "deadline")
    private LocalDateTime deadline;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;
    
    // Constructors
    public Task() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Task(String title, String description, DayOfWeek dayOfWeek, LocalDateTime deadline) {
        this();
        this.title = title;
        this.description = description;
        this.dayOfWeek = dayOfWeek;
        this.deadline = deadline;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { 
        this.title = title; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { 
        this.status = status; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { 
        this.dayOfWeek = dayOfWeek; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { 
        this.deadline = deadline; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public User getAssignedUser() { return assignedUser; }
    public void setAssignedUser(User assignedUser) { 
        this.assignedUser = assignedUser; 
        this.updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public boolean isOverdue() {
        return deadline != null && deadline.isBefore(LocalDateTime.now()) && status != TaskStatus.COMPLETED;
    }
    
    public boolean isCloseToDeadline() {
        return deadline != null && 
               deadline.isBefore(LocalDateTime.now().plusHours(24)) && 
               deadline.isAfter(LocalDateTime.now()) && 
               status != TaskStatus.COMPLETED;
    }
    
    public String getColorClass() {
        if (isOverdue()) return "overdue";
        if (isCloseToDeadline()) return "close-to-deadline";
        if (status == TaskStatus.IN_PROGRESS) return "in-progress";
        return "active";
    }
    
    public enum TaskStatus {
        ACTIVE, IN_PROGRESS, COMPLETED
    }
    
    public enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }
}
