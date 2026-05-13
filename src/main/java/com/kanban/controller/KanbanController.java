package com.kanban.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kanban.model.Comment;
import com.kanban.model.Project;
import com.kanban.model.Task;
import com.kanban.model.User;
import com.kanban.model.UserProfile;
import com.kanban.service.CommentService;
import com.kanban.service.ProjectService;
import com.kanban.service.TaskService;
import com.kanban.service.UserProfileService;
import com.kanban.service.UserService;

@Controller
public class KanbanController {
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private UserProfileService userProfileService;
    
    @GetMapping("/")
    public String kanbanBoard(Model model, Principal principal) {
        Map<Task.DayOfWeek, List<Task>> tasksByDay;
        List<User> users = userService.getAllRegularUsers();
        
        if (principal != null) {
            User currentUser = userService.getUserByLogin(principal.getName()).orElse(null);
            model.addAttribute("currentUser", currentUser);
            if (currentUser != null && currentUser.getRole() != User.Role.ADMIN) {
                tasksByDay = taskService.getTasksByDayOfWeekForUser(currentUser.getId());
            } else {
                tasksByDay = taskService.getTasksByDayOfWeek();
            }
        } else {
            tasksByDay = new java.util.LinkedHashMap<>();
            for (Task.DayOfWeek day : Task.DayOfWeek.values()) {
                tasksByDay.put(day, java.util.Collections.emptyList());
            }
        }
        
        // Debug output
        System.out.println("=== DEBUG: Tasks by day ===");
        tasksByDay.forEach((day, tasks) -> {
            System.out.println(day + ": " + tasks.size() + " tasks");
            tasks.forEach(task -> System.out.println("  - " + task.getTitle()));
        });
        
        model.addAttribute("tasksByDay", tasksByDay);
        model.addAttribute("users", users);
        model.addAttribute("days", Task.DayOfWeek.values());
        
        return "kanban-board";
    }
    
    @GetMapping("/api/tasks")
    @ResponseBody
    public Map<String, List<Task>> getAllTasks(Principal principal) {
        Map<String, List<Task>> tasksByDay;
        if (principal != null) {
            User currentUser = userService.getUserByLogin(principal.getName()).orElse(null);
            if (currentUser != null && currentUser.getRole() != User.Role.ADMIN) {
                tasksByDay = taskService.getTasksByDayOfWeekForUserAsStringKeys(currentUser.getId());
            } else {
                tasksByDay = taskService.getTasksByDayOfWeekAsStringKeys();
            }
        } else {
            tasksByDay = new java.util.LinkedHashMap<>();
            for (Task.DayOfWeek day : Task.DayOfWeek.values()) {
                tasksByDay.put(day.name(), java.util.Collections.emptyList());
            }
        }
        
        System.out.println("=== API GET /api/tasks ===");
        tasksByDay.forEach((day, tasks) -> {
            System.out.println(day + ": " + tasks.size() + " tasks");
        });
        return tasksByDay;
    }
    
    @GetMapping("/api/tasks/{id}")
    @ResponseBody
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/api/tasks")
    @ResponseBody
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task createdTask = taskService.createTask(task);
        return ResponseEntity.ok(createdTask);
    }
    
    @PutMapping("/api/tasks/{id}")
    @ResponseBody
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        try {
            Task updatedTask = taskService.updateTask(id, task);
            return ResponseEntity.ok(updatedTask);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/api/tasks/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/api/tasks/{id}/toggle")
    @ResponseBody
    public ResponseEntity<Task> toggleTaskStatus(@PathVariable Long id) {
        try {
            Task task = taskService.toggleTaskStatus(id);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/api/tasks/{id}/complete")
    @ResponseBody
    public ResponseEntity<Task> completeTask(@PathVariable Long id) {
        try {
            Task task = taskService.completeTask(id);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/api/tasks/{id}/assign")
    @ResponseBody
    public ResponseEntity<Task> assignTask(@PathVariable Long id, @RequestParam Long userId) {
        try {
            Task task = taskService.assignTaskToUser(id, userId);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/api/tasks/overdue")
    @ResponseBody
    public List<Task> getOverdueTasks() {
        return taskService.getOverdueTasks();
    }
    
    @GetMapping("/api/tasks/close-to-deadline")
    @ResponseBody
    public List<Task> getCloseToDeadlineTasks() {
        return taskService.getCloseToDeadlineTasks();
    }
    
    @GetMapping("/api/tasks/search")
    @ResponseBody
    public List<Task> searchTasks(@RequestParam String keyword) {
        return taskService.searchTasks(keyword);
    }
    
    @GetMapping("/api/stats")
    @ResponseBody
    public Map<String, Long> getStatistics() {
        return taskService.getTaskStatistics();
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/admin")
    public String adminPanel(Model model) {
        List<User> users = userService.getAllUsers();
        List<Task> tasks = taskService.getAllTasks();
        
        model.addAttribute("users", users);
        model.addAttribute("tasks", tasks);
        model.addAttribute("overdueTasks", taskService.getOverdueTasks());
        
        return "admin-panel";
    }
    
    @GetMapping("/api/users")
    @ResponseBody
    public List<User> getAllUsersApi() {
        return userService.getAllUsers();
    }
    
    @GetMapping("/api/users/{id}")
    @ResponseBody
    public ResponseEntity<User> getUserByIdApi(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/api/users")
    @ResponseBody
    public ResponseEntity<?> createUserApi(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.ok(createdUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/api/users/{id}")
    @ResponseBody
    public ResponseEntity<User> updateUserApi(@PathVariable Long id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/api/users/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteUserApi(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Project endpoints
    @GetMapping("/api/projects")
    @ResponseBody
    public List<Project> getAllProjects() {
        return projectService.getAllProjects();
    }
    
    @GetMapping("/api/projects/{id}")
    @ResponseBody
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/api/projects")
    @ResponseBody
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        try {
            Project createdProject = projectService.createProject(project);
            return ResponseEntity.ok(createdProject);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/api/projects/{id}")
    @ResponseBody
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody Project project) {
        try {
            Project updatedProject = projectService.updateProject(id, project);
            return ResponseEntity.ok(updatedProject);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/api/projects/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        try {
            projectService.deleteProject(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Comment endpoints
    @GetMapping("/api/comments")
    @ResponseBody
    public List<Comment> getAllComments() {
        return commentService.getAllComments();
    }
    
    @GetMapping("/api/comments/{id}")
    @ResponseBody
    public ResponseEntity<Comment> getCommentById(@PathVariable Long id) {
        return commentService.getCommentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/api/comments/task/{taskId}")
    @ResponseBody
    public List<Comment> getCommentsByTask(@PathVariable Long taskId) {
        return commentService.getCommentsByTask(taskId);
    }
    
    @PostMapping("/api/comments")
    @ResponseBody
    public ResponseEntity<Comment> createComment(@RequestBody Comment comment) {
        try {
            Comment createdComment = commentService.createComment(comment);
            return ResponseEntity.ok(createdComment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/api/comments/{id}")
    @ResponseBody
    public ResponseEntity<Comment> updateComment(@PathVariable Long id, @RequestBody Comment comment) {
        try {
            Comment updatedComment = commentService.updateComment(id, comment);
            return ResponseEntity.ok(updatedComment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/api/comments/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        try {
            commentService.deleteComment(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // UserProfile endpoints
    @GetMapping("/api/user-profiles")
    @ResponseBody
    public java.util.List<UserProfile> getAllUserProfiles() {
        return userProfileService.getAllUserProfiles();
    }
    
    @GetMapping("/api/user-profiles/{id}")
    @ResponseBody
    public ResponseEntity<UserProfile> getUserProfileById(@PathVariable Long id) {
        return userProfileService.getUserProfileById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/api/user-profiles/user/{userId}")
    @ResponseBody
    public ResponseEntity<UserProfile> getUserProfileByUserId(@PathVariable Long userId) {
        return userProfileService.getUserProfileByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/api/user-profiles")
    @ResponseBody
    public ResponseEntity<?> createUserProfile(@RequestBody UserProfile userProfile) {
        try {
            UserProfile createdProfile = userProfileService.createUserProfile(userProfile);
            return ResponseEntity.ok(createdProfile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/api/user-profiles/{id}")
    @ResponseBody
    public ResponseEntity<UserProfile> updateUserProfile(@PathVariable Long id, @RequestBody UserProfile userProfile) {
        try {
            UserProfile updatedProfile = userProfileService.updateUserProfile(id, userProfile);
            return ResponseEntity.ok(updatedProfile);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/api/user-profiles/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteUserProfile(@PathVariable Long id) {
        try {
            userProfileService.deleteUserProfile(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
