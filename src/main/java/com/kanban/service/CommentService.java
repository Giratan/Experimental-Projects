package com.kanban.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kanban.model.Comment;
import com.kanban.repository.CommentRepository;

@Service
@Transactional
public class CommentService {
    
    @Autowired
    private CommentRepository commentRepository;
    
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }
    
    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }
    
    public List<Comment> getCommentsByTask(Long taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
    }
    
    public List<Comment> getCommentsByAuthor(Long authorId) {
        return commentRepository.findByAuthorIdOrderByCreatedAtAsc(authorId);
    }
    
    public Comment createComment(Comment comment) {
        if (comment.getTask() == null) {
            throw new RuntimeException("Task is required for comment");
        }
        if (comment.getAuthor() == null) {
            throw new RuntimeException("Author is required for comment");
        }
        return commentRepository.save(comment);
    }
    
    public Comment updateComment(Long id, Comment commentDetails) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        
        comment.setText(commentDetails.getText());
        
        return commentRepository.save(comment);
    }
    
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        commentRepository.delete(comment);
    }
    
    public long countCommentsByTask(Long taskId) {
        return commentRepository.countCommentsByTask(taskId);
    }
}
