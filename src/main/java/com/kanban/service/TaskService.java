package com.kanban.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kanban.model.Task;
import com.kanban.model.User;
import com.kanban.repository.CommentRepository;
import com.kanban.repository.TaskRepository;

@Service
@Transactional
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentRepository commentRepository;
    
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }
    
    public Map<Task.DayOfWeek, List<Task>> getTasksByDayOfWeek() {
        Map<Task.DayOfWeek, List<Task>> tasksByDay = new LinkedHashMap<>();
        
        for (Task.DayOfWeek day : Task.DayOfWeek.values()) {
            tasksByDay.put(day, taskRepository.findByDayOfWeekOrderByCreatedAtAsc(day));
        }
        
        return tasksByDay;
    }
    
    public Map<Task.DayOfWeek, List<Task>> getTasksByDayOfWeekForUser(Long userId) {
        Map<Task.DayOfWeek, List<Task>> tasksByDay = new LinkedHashMap<>();
        
        for (Task.DayOfWeek day : Task.DayOfWeek.values()) {
            tasksByDay.put(day, taskRepository.findByAssignedUserIdOrderByCreatedAtAsc(userId).stream()
                    .filter(task -> task.getDayOfWeek() == day)
                    .toList());
        }
        
        return tasksByDay;
    }
    
    public Map<String, List<Task>> getTasksByDayOfWeekAsStringKeys() {
        Map<String, List<Task>> tasksByDay = new LinkedHashMap<>();
        
        for (Task.DayOfWeek day : Task.DayOfWeek.values()) {
            tasksByDay.put(day.name(), taskRepository.findByDayOfWeekOrderByCreatedAtAsc(day));
        }
        
        return tasksByDay;
    }
    
    public Map<String, List<Task>> getTasksByDayOfWeekForUserAsStringKeys(Long userId) {
        Map<String, List<Task>> tasksByDay = new LinkedHashMap<>();
        
        for (Task.DayOfWeek day : Task.DayOfWeek.values()) {
            List<Task> tasksForDay = taskRepository.findByAssignedUserIdOrderByCreatedAtAsc(userId).stream()
                    .filter(task -> task.getDayOfWeek() == day)
                    .toList();
            tasksByDay.put(day.name(), tasksForDay);
        }
        
        return tasksByDay;
    }
    
    public Task createTask(Task task) {
        task.setCreatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }
    
    public Task updateTask(Long id, Task taskDetails) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setDayOfWeek(taskDetails.getDayOfWeek());
        task.setDeadline(taskDetails.getDeadline());
        task.setAssignedUser(taskDetails.getAssignedUser());
        
        return taskRepository.save(task);
    }
    
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        
        // Delete all comments associated with this task first
        commentRepository.deleteByTaskId(id);
        
        taskRepository.delete(task);
    }
    
    public Task changeTaskStatus(Long id, Task.TaskStatus newStatus) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        
        task.setStatus(newStatus);
        return taskRepository.save(task);
    }
    
    public Task assignTaskToUser(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        task.setAssignedUser(user);
        return taskRepository.save(task);
    }
    
    public Task toggleTaskStatus(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        
        task.setStatus(switch (task.getStatus()) {
            case ACTIVE -> Task.TaskStatus.IN_PROGRESS;
            case IN_PROGRESS -> Task.TaskStatus.ACTIVE;
            case COMPLETED -> Task.TaskStatus.ACTIVE;
        });
        
        return taskRepository.save(task);
    }
    
    public Task completeTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        
        task.setStatus(Task.TaskStatus.COMPLETED);
        return taskRepository.save(task);
    }
    
    public List<Task> getOverdueTasks() {
        return taskRepository.findOverdueTasks(LocalDateTime.now());
    }
    
    public List<Task> getCloseToDeadlineTasks() {
        return taskRepository.findCloseToDeadlineTasks(LocalDateTime.now(), LocalDateTime.now().plusHours(24));
    }
    
    public List<Task> getTasksByUser(Long userId) {
        return taskRepository.findByAssignedUserIdOrderByCreatedAtAsc(userId);
    }
    
    public List<Task> searchTasks(String keyword) {
        return taskRepository.searchTasks(keyword);
    }
    
    public Map<String, Long> getTaskStatistics() {
        Map<String, Long> stats = new HashMap<>();
        
        List<Object[]> results = taskRepository.getTaskStatisticsByDay();
        for (Object[] result : results) {
            stats.put("day_" + result[0], (Long) result[1]);
        }
        
        stats.put("total_tasks", (long) taskRepository.findAll().size());
        stats.put("overdue_tasks", (long) getOverdueTasks().size());
        stats.put("close_to_deadline", (long) getCloseToDeadlineTasks().size());
        
        return stats;
    }
}
