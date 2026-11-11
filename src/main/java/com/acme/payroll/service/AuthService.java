package com.acme.payroll.service;

import com.acme.payroll.dao.AuditLogDao;
import com.acme.payroll.dao.UserDao;
import com.acme.payroll.model.User;

import java.time.LocalDateTime;
import java.util.Arrays;

public class AuthService {

    public static final String INVALID_CREDENTIALS = "Invalid email or password.";

    private final UserDao userDao;
    private final AuditLogDao auditLogDao;

    public AuthService(UserDao userDao, AuditLogDao auditLogDao) {
        this.userDao = userDao;
        this.auditLogDao = auditLogDao;
    }

    /**
     * Checks the credentials and opens the session.
     *
     * @throws ValidationException with a user-facing message if the input is incomplete or wrong
     */
    public User login(String email, char[] password) {
        try {
            if (email == null || email.trim().isEmpty()) {
                throw new ValidationException("Email is required.");
            }
            if (!EmployeeValidator.isValidEmail(email)) {
                throw new ValidationException("Enter a valid email address.");
            }
            if (password == null || password.length == 0) {
                throw new ValidationException("Password is required.");
            }
            User user = userDao.findByEmail(email.trim());
            if (user == null || !PasswordHasher.verify(password, user.getPasswordHash())) {
                auditLogDao.log(email.trim(), "LOGIN_FAILED", "User", user == null ? null : user.getId(), null);
                throw new ValidationException(INVALID_CREDENTIALS);
            }
            if (!user.isActive()) {
                throw new ValidationException("This account is disabled. Contact your administrator.");
            }
            LocalDateTime now = LocalDateTime.now();
            userDao.updateLastLogin(user.getId(), now);
            user.setLastLogin(now);
            Session.login(user);
            auditLogDao.log(user.getEmail(), "LOGIN", "User", user.getId(), null);
            return user;
        } finally {
            if (password != null) {
                Arrays.fill(password, '\0');
            }
        }
    }

    public void logout() {
        User u = Session.getUser();
        if (u != null) {
            auditLogDao.log(u.getEmail(), "LOGOUT", "User", u.getId(), null);
        }
        Session.logout();
    }
}
