package com.kanban.repository;

import com.kanban.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByLogin(String login);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByLogin(String login);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.role = 'USER'")
    java.util.List<User> findAllUsers();
    
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN'")
    java.util.List<User> findAllAdmins();
}
