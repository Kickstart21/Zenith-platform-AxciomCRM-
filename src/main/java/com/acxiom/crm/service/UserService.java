package com.acxiom.crm.service;

import com.acxiom.crm.model.Role;
import com.acxiom.crm.model.User;
import com.acxiom.crm.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User registerUser(String email, String password, String fullName, Role role) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("User with email '" + email + "' already exists.");
        }
        validatePasswordPolicy(password);

        Role userRole = role != null ? role : Role.SALES_EXECUTIVE;
        User user = new User(email.toLowerCase().trim(), passwordEncoder.encode(password), fullName, userRole);
        User saved = userRepository.save(user);

        auditService.logEvent(saved.getEmail(), "REGISTER", "User", saved.getId().toString(),
                "Registered new account with role " + userRole.name(), null);
        return saved;
    }

    public void validatePasswordPolicy(String password) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character (@$!%*?&#).");
        }
    }

    @Transactional
    public void recordFailedLogin(String email, String ip) {
        Optional<User> opt = userRepository.findByEmailIgnoreCase(email);
        if (opt.isPresent()) {
            User user = opt.get();
            int newAttempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(newAttempts);

            if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                user.setLockoutEndTime(LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES));
                userRepository.save(user);
                auditService.logEvent(email, "LOCKOUT", "User", user.getId().toString(),
                        "Account locked for " + LOCKOUT_MINUTES + " minutes due to " + newAttempts + " consecutive failed login attempts.", ip);
            } else {
                userRepository.save(user);
                auditService.logEvent(email, "FAILED_LOGIN", "User", user.getId().toString(),
                        "Failed login attempt (" + newAttempts + "/" + MAX_FAILED_ATTEMPTS + ")", ip);
            }
        } else {
            auditService.logEvent(email, "FAILED_LOGIN", "User", null,
                    "Failed login attempt for non-existent account", ip);
        }
    }

    @Transactional
    public void resetFailedLoginAttempts(String email, String ip) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            user.setLockoutEndTime(null);
            userRepository.save(user);
            auditService.logEvent(email, "LOGIN", "Session", user.getId().toString(), "User logged in successfully", ip);
        });
    }

    @Transactional
    public void unlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setFailedLoginAttempts(0);
        user.setLockoutEndTime(null);
        userRepository.save(user);
        auditService.logEvent(auditService.getCurrentUsername(), "SECURITY", "User", user.getId().toString(),
                "Admin unlocked account for " + user.getEmail(), null);
    }

    @Transactional
    public void toggleUserActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setActive(!user.isActive());
        userRepository.save(user);
        auditService.logEvent(auditService.getCurrentUsername(), "UPDATE", "User", user.getId().toString(),
                "Admin toggled active status to " + user.isActive() + " for " + user.getEmail(), null);
    }

    @Transactional
    public void changeUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Role oldRole = user.getRole();
        user.setRole(newRole);
        userRepository.save(user);
        auditService.logEvent(auditService.getCurrentUsername(), "ROLE_CHANGE", "User", user.getId().toString(),
                "Role changed from " + oldRole.name() + " to " + newRole.name() + " for " + user.getEmail(), null);
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        validatePasswordPolicy(newPassword);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        auditService.logEvent(auditService.getCurrentUsername(), "SECURITY", "User", user.getId().toString(),
                "Admin reset password for " + user.getEmail(), null);
    }
}
