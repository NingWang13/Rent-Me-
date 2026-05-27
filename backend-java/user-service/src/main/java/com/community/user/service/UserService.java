package com.community.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.exception.BusinessException;
import com.community.common.exception.ErrorCode;
import com.community.common.response.PageResponse;
import com.community.user.entity.User;
import com.community.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return user;
    }

    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    public User getUserByPhone(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return userMapper.selectOne(wrapper);
    }

    public User getUserByOpenid(String openid) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        return userMapper.selectOne(wrapper);
    }

    public PageResponse<User> getUserList(int page, int size, String keyword) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getNickname, keyword)
                    .or()
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getPhone, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(pageParam, wrapper);
        return PageResponse.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Transactional(rollbackFor = Exception.class)
    public User createUser(User user) {
        User existing = getUserByUsername(user.getUsername());
        if (existing != null) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        userMapper.insert(user);
        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    public User updateUser(Long id, User user) {
        getUserById(id);
        user.setId(id);
        userMapper.updateById(user);
        return getUserById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        getUserById(id);
        userMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long id, Integer status) {
        User user = getUserById(id);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    public List<User> searchUsers(String keyword, int limit) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(User::getNickname, keyword)
                .or()
                .like(User::getUsername, keyword)
                .last("LIMIT " + Math.min(limit, 100));
        return userMapper.selectList(wrapper);
    }
}
