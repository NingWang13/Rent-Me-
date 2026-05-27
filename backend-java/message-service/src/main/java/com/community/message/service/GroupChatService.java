package com.community.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BusinessException;
import com.community.message.entity.GroupChat;
import com.community.message.entity.GroupMember;
import com.community.message.mapper.GroupChatMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GroupChatService extends ServiceImpl<GroupChatMapper, GroupChat> {

    private final GroupMemberService groupMemberService;

    public GroupChatService(GroupMemberService groupMemberService) {
        this.groupMemberService = groupMemberService;
    }

    @Transactional(rollbackFor = Exception.class)
    public GroupChat createGroup(Long userId, String name, String description, Integer joinType, Integer chatType) {
        GroupChat group = GroupChat.builder()
                .groupNo("GRP" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .name(name)
                .description(description)
                .ownerId(userId)
                .memberCount(1)
                .maxMembers(500)
                .joinType(joinType != null ? joinType : 1)
                .chatType(chatType != null ? chatType : 1)
                .status(1)
                .build();
        save(group);

        GroupMember owner = GroupMember.builder()
                .groupId(group.getId())
                .userId(userId)
                .role(1)
                .joinType(1)
                .status(1)
                .joinTime(LocalDateTime.now())
                .build();
        groupMemberService.save(owner);

        return group;
    }

    @Transactional(rollbackFor = Exception.class)
    public void joinGroup(Long userId, Long groupId, String applyMessage) {
        GroupChat group = getById(groupId);
        if (group == null || group.getStatus() != 1) {
            throw new BusinessException(400, "群聊不存在或已解散");
        }

        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId)
               .eq(GroupMember::getUserId, userId);
        GroupMember existing = groupMemberService.getOne(wrapper);
        if (existing != null && existing.getStatus() == 1) {
            throw new BusinessException(400, "已经是群成员了");
        }

        if (group.getJoinType() == 1) {
            GroupMember member = GroupMember.builder()
                    .groupId(groupId)
                    .userId(userId)
                    .role(3)
                    .joinType(2)
                    .status(1)
                    .joinTime(LocalDateTime.now())
                    .build();
            groupMemberService.save(member);

            group.setMemberCount(group.getMemberCount() + 1);
            updateById(group);
        } else if (group.getJoinType() == 2) {
            GroupMember member = GroupMember.builder()
                    .groupId(groupId)
                    .userId(userId)
                    .role(3)
                    .joinType(3)
                    .status(0)
                    .joinTime(LocalDateTime.now())
                    .build();
            groupMemberService.save(member);
        } else {
            throw new BusinessException(400, "该群禁止加入");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void quitGroup(Long userId, Long groupId) {
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId)
               .eq(GroupMember::getUserId, userId);
        GroupMember member = groupMemberService.getOne(wrapper);
        if (member == null) {
            throw new BusinessException(400, "不是群成员");
        }

        if (member.getRole() == 1) {
            throw new BusinessException(400, "群主不能退出群聊");
        }

        member.setStatus(0);
        member.setQuitTime(LocalDateTime.now());
        groupMemberService.updateById(member);

        GroupChat group = getById(groupId);
        if (group != null) {
            group.setMemberCount(group.getMemberCount() - 1);
            updateById(group);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void dismissGroup(Long userId, Long groupId) {
        GroupChat group = getById(groupId);
        if (group == null) {
            throw new BusinessException(400, "群聊不存在");
        }
        if (!group.getOwnerId().equals(userId)) {
            throw new BusinessException(403, "只有群主可以解散群聊");
        }

        group.setStatus(0);
        updateById(group);

        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId);
        List<GroupMember> members = groupMemberService.list(wrapper);
        for (GroupMember member : members) {
            member.setStatus(0);
            member.setQuitTime(LocalDateTime.now());
            groupMemberService.updateById(member);
        }
    }

    public Map<String, Object> getGroupList(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupMember::getUserId, userId)
                     .eq(GroupMember::getStatus, 1)
                     .orderByDesc(GroupMember::getJoinTime);

        Page<GroupMember> memberPage = groupMemberService.page(new Page<>(page, size), memberWrapper);

        List<Map<String, Object>> groupList = new java.util.ArrayList<>();
        for (GroupMember member : memberPage.getRecords()) {
            GroupChat group = getById(member.getGroupId());
            if (group != null) {
                Map<String, Object> groupInfo = new HashMap<>();
                groupInfo.put("id", group.getId());
                groupInfo.put("groupNo", group.getGroupNo());
                groupInfo.put("name", group.getName());
                groupInfo.put("avatarUrl", group.getAvatarUrl());
                groupInfo.put("memberCount", group.getMemberCount());
                groupInfo.put("role", member.getRole());
                groupInfo.put("joinTime", member.getJoinTime());
                groupList.add(groupInfo);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", groupList);
        result.put("total", memberPage.getTotal());
        result.put("page", page);
        result.put("size", size);
        result.put("hasMore", memberPage.getCurrent() < memberPage.getPages());
        return result;
    }

    public Map<String, Object> getGroupMembers(Long groupId, Integer page, Integer size) {
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId)
               .eq(GroupMember::getStatus, 1)
               .orderByAsc(GroupMember::getRole)
               .orderByDesc(GroupMember::getJoinTime);

        Page<GroupMember> memberPage = groupMemberService.page(new Page<>(page, size), wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", memberPage.getRecords());
        result.put("total", memberPage.getTotal());
        result.put("page", page);
        result.put("size", size);
        result.put("hasMore", memberPage.getCurrent() < memberPage.getPages());
        return result;
    }
}
