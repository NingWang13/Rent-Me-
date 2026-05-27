package com.community.user.service;

import com.community.common.response.PageResponse;
import com.community.user.entity.User;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 根据ID获取用户
     */
    User getUserById(Long id);

    /**
     * 根据用户名获取用户
     */
    User getUserByUsername(String username);

    /**
     * 根据手机号获取用户
     */
    User getUserByPhone(String phone);

    /**
     * 根据openid获取用户
     */
    User getUserByOpenid(String openid);

    /**
     * 分页查询用户列表
     */
    PageResponse<User> getUserList(int page, int size, String keyword);

    /**
     * 创建用户
     */
    User createUser(User user);

    /**
     * 更新用户
     */
    User updateUser(Long id, User user);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 更新用户状态
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * 搜索用户
     */
    List<User> searchUsers(String keyword, int limit);
}
