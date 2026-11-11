package com.acme.payroll.service;

import com.acme.payroll.model.Role;
import com.acme.payroll.model.User;

/**
 * Holds the user logged in to this desktop client.
 */
public final class Session {

    private static volatile User currentUser;

    private Session() {
    }

    public static User getUser() {
        return currentUser;
    }

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean hasRole(Role role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    /** Email used for audit records; "system" when no one is logged in (e.g. first start seeding). */
    public static String actor() {
        User u = currentUser;
        return u == null ? "system" : u.getEmail();
    }
}
