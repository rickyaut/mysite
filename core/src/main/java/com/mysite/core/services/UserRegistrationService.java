package com.mysite.core.services;

public interface UserRegistrationService {
    boolean registerUser(String email, String password, String fullName);
    boolean authenticateUser(String email, String password);
}
