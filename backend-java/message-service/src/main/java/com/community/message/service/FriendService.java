package com.community.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.exception.BusinessException;
import com.community.common.exception.ErrorCode;
import com.community.message.entity.FriendRelation;
import com.community.message.mapper.FriendRelationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FriendService {

    private final FriendRelationMapper friendRelationMapper;

    public FriendService(FriendRelationMapper friendRelationMapper) {
        this.friendRelationMapper = friendRelationMapper;
    }

    public FriendRelation getFriendRelation(Long userId, Long friendId) {
        LambdaQueryWrapper<FriendRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRelation::getUserId, userId);
        wrapper.eq(FriendRelation::getFriendId, friendId);
        return friendRelationMapper.selectOne(wrapper);
    }

    public List<FriendRelation> getFriends(Long userId) {
        LambdaQueryWrapper<FriendRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRelation::getUserId, userId);
        wrapper.eq(FriendRelation::getStatus, 1);
        return friendRelationMapper.selectList(wrapper);
    }

    public List<FriendRelation> getFriendApplications(Long userId) {
        LambdaQueryWrapper<FriendRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRelation::getFriendId, userId);
        wrapper.eq(FriendRelation::getStatus, 0);
        wrapper.orderByDesc(FriendRelation::getCreateTime);
        return friendRelationMapper.selectList(wrapper);
    }

    public long getPendingApplicationCount(Long userId) {
        LambdaQueryWrapper<FriendRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRelation::getFriendId, userId);
        wrapper.eq(FriendRelation::getStatus, 0);
        return friendRelationMapper.selectCount(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public FriendRelation applyFriend(FriendRelation relation) {
        FriendRelation existing = getFriendRelation(relation.getUserId(), relation.getFriendId());
        if (existing != null && existing.getStatus() == 1) {
            throw new BusinessException(ErrorCode.ALREADY_FRIENDS);
        }
        if (existing != null && existing.getStatus() == 0) {
            throw new BusinessException(ErrorCode.FRIEND_APPLY_EXISTS);
        }
        relation.setStatus(0);
        relation.setApplyTime(LocalDateTime.now());
        friendRelationMapper.insert(relation);
        return relation;
    }

    @Transactional(rollbackFor = Exception.class)
    public FriendRelation acceptFriend(Long relationId, Long userId) {
        FriendRelation relation = friendRelationMapper.selectById(relationId);
        if (relation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (!relation.getFriendId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        relation.setStatus(1);
        relation.setAgreeTime(LocalDateTime.now());
        friendRelationMapper.updateById(relation);

        FriendRelation reverse = new FriendRelation();
        reverse.setUserId(userId);
        reverse.setFriendId(relation.getUserId());
        reverse.setStatus(1);
        reverse.setAgreeTime(LocalDateTime.now());
        friendRelationMapper.insert(reverse);

        return relation;
    }

    @Transactional(rollbackFor = Exception.class)
    public void rejectFriend(Long relationId, Long userId) {
        FriendRelation relation = friendRelationMapper.selectById(relationId);
        if (relation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (!relation.getFriendId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        relation.setStatus(2);
        friendRelationMapper.updateById(relation);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteFriend(Long userId, Long friendId) {
        LambdaQueryWrapper<FriendRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRelation::getUserId, userId);
        wrapper.eq(FriendRelation::getFriendId, friendId);
        friendRelationMapper.delete(wrapper);

        LambdaQueryWrapper<FriendRelation> reverseWrapper = new LambdaQueryWrapper<>();
        reverseWrapper.eq(FriendRelation::getUserId, friendId);
        reverseWrapper.eq(FriendRelation::getFriendId, userId);
        friendRelationMapper.delete(reverseWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRemark(Long userId, Long friendId, String remark) {
        FriendRelation relation = getFriendRelation(userId, friendId);
        if (relation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        relation.setRemark(remark);
        friendRelationMapper.updateById(relation);
    }
}
