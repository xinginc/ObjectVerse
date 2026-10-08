package com.example.objectverse.service;

import com.example.objectverse.entity.UserAccount;

public interface AuthService {

    UserAccount register(String username, String password, String nickname);

    UserAccount login(String username, String password);
}
