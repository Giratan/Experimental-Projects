# Kanban Board Application

A modern Kanban board application built with Spring Boot 3.5.0 and Java 25 LTS, featuring task management, user authentication, and role-based access control.

## Features

- **Task Management**: Create, edit, delete, and organize tasks in a Kanban board format
- **User Authentication**: Secure login system with Spring Security
- **Role-Based Access**: Admin users can manage all tasks and users, regular users see only their assigned tasks
- **Real-time Updates**: AJAX-powered task updates without page refresh
- **Responsive Design**: Bootstrap-based UI that works on all devices
- **MySQL Integration**: Persistent data storage with JPA/Hibernate
- **REST API**: Full REST API for task operations

## Technologies Used

- **Backend**: Spring Boot 3.5.0, Java 25 LTS
- **Database**: MySQL 8.0+
- **Security**: Spring Security with BCrypt password encoding
- **Frontend**: Thymeleaf templates, Bootstrap 5, JavaScript
- **Build Tool**: Maven
- **ORM**: Hibernate/JPA

## Prerequisites

- Java 25 LTS (Temurin distribution recommended)
- MySQL 8.0+
- Maven 3.6+

## Installation & Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Giratan/Experimental-Projects.git
   cd Experimental-Projects
   ```

2. **Database Setup**:
   - Create a MySQL database named `kanban_db`
   - Update database credentials in `src/main/resources/application.properties`:
     ```properties
     spring.datasource.username=your_username
     spring.datasource.password=your_password
     ```

3. **Build and Run**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **Access the application**:
   - Open http://localhost:8080 in your browser
   - Default admin credentials: `admin` / `admin123`
   - Default user credentials: `user` / `user123`

## Project Structure

```
src/
├── main/
│   ├── java/com/kanban/
│   │   ├── KanbanApplication.java          # Main application class
│   │   ├── config/                         # Configuration classes
│   │   │   ├── DataInitializer.java        # Demo data initialization
│   │   │   ├── PasswordEncoderConfig.java  # Password encoding config
│   │   │   └── SecurityConfig.java         # Security configuration
│   │   ├── controller/                     # REST controllers
│   │   │   └── KanbanController.java       # Main controller
│   │   ├── model/                          # JPA entities
│   │   │   ├── Task.java                   # Task entity
│   │   │   └── User.java                   # User entity
│   │   ├── repository/                     # JPA repositories
│   │   │   ├── TaskRepository.java         # Task data access
│   │   │   └── UserRepository.java         # User data access
│   │   └── service/                        # Business logic
│   │       ├── TaskService.java            # Task operations
│   │       └── UserService.java            # User operations
│   └── resources/
│       ├── application.properties          # Application configuration
│       ├── static/                         # Static resources
│       │   ├── css/kanban.css             # Custom styles
│       │   └── js/kanban.js               # Frontend JavaScript
│       └── templates/                     # Thymeleaf templates
│           ├── kanban-board.html          # Main board view
│           ├── admin-panel.html           # Admin management
│           └── login.html                 # Login page
```

## API Endpoints

### Tasks
- `GET /api/tasks` - Get all tasks (filtered by user role)
- `GET /api/tasks/{id}` - Get task by ID
- `POST /api/tasks` - Create new task
- `PUT /api/tasks/{id}` - Update task
- `DELETE /api/tasks/{id}` - Delete task
- `POST /api/tasks/{id}/toggle` - Toggle task status
- `POST /api/tasks/{id}/complete` - Mark task as completed

### Statistics
- `GET /api/stats` - Get task statistics
- `GET /api/tasks/overdue` - Get overdue tasks
- `GET /api/tasks/close-to-deadline` - Get tasks close to deadline
- `GET /api/tasks/search?keyword={keyword}` - Search tasks

## User Roles

- **ADMIN**: Full access to all tasks and user management
- **USER**: Can only view and manage their own assigned tasks

## Database Schema

### Users Table
- `id` (Primary Key)
- `login` (Unique)
- `password` (BCrypt encoded)
- `full_name`
- `email` (Unique)
- `role` (ADMIN/USER)

### Tasks Table
- `id` (Primary Key)
- `title`
- `description`
- `status` (ACTIVE/IN_PROGRESS/COMPLETED)
- `day_of_week` (MONDAY-SUNDAY)
- `deadline`
- `created_at`
- `updated_at`
- `assigned_user_id` (Foreign Key to Users)

## Development

### Running Tests
```bash
mvn test
```

### Building for Production
```bash
mvn clean package
java -jar target/kanban-board-1.0-SNAPSHOT.jar
```

### IDE Setup
- Import as Maven project
- Ensure Java 25 LTS is configured as the project SDK
- Run `DataInitializer` will create demo data on startup

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Built with Spring Boot and modern Java features
- Uses Bootstrap for responsive design
- Implements best practices for Spring Security and JPA