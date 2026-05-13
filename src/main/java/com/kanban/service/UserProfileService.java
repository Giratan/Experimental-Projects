package com.kanban.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kanban.model.User;
import com.kanban.model.UserProfile;
import com.kanban.repository.UserProfileRepository;

@Service
@Transactional
public class UserProfileService {
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private UserService userService;
    
    public List<UserProfile> getAllUserProfiles() {
        return userProfileRepository.findAll();
    }
    
    public Optional<UserProfile> getUserProfileById(Long id) {
        return userProfileRepository.findById(id);
    }
    
    public Optional<UserProfile> getUserProfileByUserId(Long userId) {
        return userProfileRepository.findByUserId(userId);
    }
    
    public UserProfile createUserProfile(UserProfile userProfile) {
        if (userProfile.getUser() == null || userProfile.getUser().getId() == null) {
            throw new RuntimeException("User is required for profile");
        }
        
        // Fetch the actual User entity from the database to avoid detached entity error
        User user = userService.getUserById(userProfile.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userProfile.getUser().getId()));
        
        if (userProfileRepository.existsByUserId(user.getId())) {
            throw new RuntimeException("User profile already exists for user: " + user.getId());
        }
        
        userProfile.setUser(user);
        return userProfileRepository.save(userProfile);
    }
    
    public UserProfile updateUserProfile(Long id, UserProfile profileDetails) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("UserProfile not found with id: " + id));
        
        profile.setPhone(profileDetails.getPhone());
        profile.setBirthDate(profileDetails.getBirthDate());
        profile.setBio(profileDetails.getBio());
        profile.setAvatarUrl(profileDetails.getAvatarUrl());
        profile.setLocation(profileDetails.getLocation());
        
        return userProfileRepository.save(profile);
    }
    
    public void deleteUserProfile(Long id) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("UserProfile not found with id: " + id));
        userProfileRepository.delete(profile);
    }
    
    public UserProfile getOrCreateUserProfile(Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        return userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserProfile profile = new UserProfile(user);
                    return userProfileRepository.save(profile);
                });
    }
}
