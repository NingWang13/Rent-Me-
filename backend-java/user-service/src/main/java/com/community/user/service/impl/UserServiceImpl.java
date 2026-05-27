package com.community.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.security.PasswordEncoder;
import com.community.user.entity.User;
import com.community.user.mapper.UserMapper;
import com.community.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        wrapper.eq(User::getDeleted, 0);
        return this.getOne(wrapper);
    }

    @Override
    public User getUserById(Long id) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId, id);
        wrapper.eq(User::getDeleted, 0);
        return this.getOne(wrapper);
    }

    @Override
    public User createUser(User user) {
        this.save(user);
        return user;
    }

    @Override
    public User updateUser(Long userId, User user) {
        user.setId(userId);
        this.updateById(user);
        return this.getUserById(userId);
    }

    @Override
    public List<User> getUserList(int page, int size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(User::getUsername, keyword);
        }
        wrapper.eq(User::getDeleted, 0);
        wrapper.last("limit " + (page - 1) * size + ", " + size);
        return this.list(wrapper);
    }

    @Override
    public long countUsers(String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(User::getUsername, keyword);
        }
        wrapper.eq(User::getDeleted, 0);
        return this.count(wrapper);
    }
}
