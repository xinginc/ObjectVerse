package com.example.objectverse.service.impl;

import com.example.objectverse.entity.UserAccount;
import com.example.objectverse.mapper.UserAccountMapper;
import com.example.objectverse.service.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserAccountMapper userAccountMapper;

    public AuthServiceImpl(UserAccountMapper userAccountMapper) {
        this.userAccountMapper = userAccountMapper;
    }

    @Override
    public UserAccount register(String username, String password, String nickname) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        if (userAccountMapper.findByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        LocalDateTime now = LocalDateTime.now();
        UserAccount user = new UserAccount();
        user.setUsername(username.trim());
        // TODO: 后续可改为 BCrypt 加密存储
        user.setPassword(password);
        user.setNickname(StringUtils.hasText(nickname) ? nickname.trim() : username.trim());
        user.setRole("STUDENT");
        user.setStatus("ACTIVE");
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setDeleted(0);
        userAccountMapper.insert(user);
        return user;
    }

    @Override
    public UserAccount login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        UserAccount user = userAccountMapper.findByUsername(username.trim());
        if (user == null || !password.equals(user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        return user;
    }
}
