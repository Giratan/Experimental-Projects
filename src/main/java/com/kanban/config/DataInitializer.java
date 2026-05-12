package com.kanban.config;

import com.kanban.model.Task;
import com.kanban.model.User;
import com.kanban.service.TaskService;
import com.kanban.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = Logger.getLogger(DataInitializer.class.getName());
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TaskService taskService;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("=== DataInitializer started ===");
        
        // Create demo users if they don't exist
        createDemoUsers();
        
        // Create demo tasks if they don't exist
        createDemoTasks();
        
        logger.info("=== DataInitializer completed ===");
    }
    
    private void createDemoUsers() {
        logger.info("Creating demo users...");
        
        if (!userService.existsByLogin("admin")) {
            User admin = new User();
            admin.setLogin("admin");
            admin.setPassword("admin123");
            admin.setFullName("Administrator");
            admin.setEmail("admin@kanban.com");
            admin.setRole(User.Role.ADMIN);
            userService.createUser(admin);
            logger.info("Admin user created");
        } else {
            logger.info("Admin user already exists");
        }
        
        if (!userService.existsByLogin("user")) {
            User user = new User();
            user.setLogin("user");
            user.setPassword("user123");
            user.setFullName("Regular User");
            user.setEmail("user@kanban.com");
            user.setRole(User.Role.USER);
            userService.createUser(user);
            logger.info("Regular user created");
        } else {
            logger.info("Regular user already exists");
        }
    }
    
    private void createDemoTasks() {
        logger.info("Checking for existing tasks...");
        long totalTasks = taskService.getAllTasks().size();
        logger.info("Total tasks in database: " + totalTasks);
        
        if (totalTasks == 0) {
            logger.info("Creating demo tasks...");
            
            User admin = userService.getUserByLogin("admin").orElse(null);
            User user = userService.getUserByLogin("user").orElse(null);
            
            // Monday tasks
            createTask("Планерка с командой", "Ежедневная планерка для обсуждения задач", 
                      Task.DayOfWeek.MONDAY, LocalDateTime.now().plusHours(3), admin);
            
            createTask("Code Review", "Проверить pull requests", 
                      Task.DayOfWeek.MONDAY, LocalDateTime.now().plusHours(5), user);
            
            // Tuesday tasks
            createTask("Встреча с клиентом", "Обсуждение требований к проекту", 
                      Task.DayOfWeek.TUESDAY, LocalDateTime.now().plusDays(1).plusHours(10), admin);
            
            // Wednesday tasks
            createTask("Разработка нового модуля", "Реализовать авторизацию", 
                      Task.DayOfWeek.WEDNESDAY, LocalDateTime.now().plusDays(2).plusHours(18), user);
            
            createTask("Тестирование", "Написать unit тесты", 
                      Task.DayOfWeek.WEDNESDAY, LocalDateTime.now().plusDays(2).plusHours(20), user);
            
            // Thursday tasks
            createTask("Деплой на тестовый сервер", "Развернуть новую версию", 
                      Task.DayOfWeek.THURSDAY, LocalDateTime.now().plusDays(3).plusHours(14), admin);
            
            // Friday tasks
            createTask("Подготовка отчета", "Сформировать недельный отчет", 
                      Task.DayOfWeek.FRIDAY, LocalDateTime.now().plusDays(4).plusHours(16), admin);
            
            // Create some overdue tasks
            createTask("Просроченная задача", "Эта задача должна была быть выполнена вчера", 
                      Task.DayOfWeek.MONDAY, LocalDateTime.now().minusDays(1), user);
            
            logger.info("Demo tasks created successfully");
        } else {
            logger.info("Tasks already exist in database, skipping demo data creation");
        }
    }
    
    private void createTask(String title, String description, Task.DayOfWeek dayOfWeek, 
                         LocalDateTime deadline, User assignedUser) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setDayOfWeek(dayOfWeek);
        task.setDeadline(deadline);
        task.setAssignedUser(assignedUser);
        task.setStatus(Task.TaskStatus.ACTIVE);
        
        taskService.createTask(task);
        logger.info("Task created: " + title);
    }
}
