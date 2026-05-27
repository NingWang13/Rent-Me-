package com.community.message.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.message.entity.GroupMember;
import com.community.message.mapper.GroupMemberMapper;
import org.springframework.stereotype.Service;

@Service
public class GroupMemberService extends ServiceImpl<GroupMemberMapper, GroupMember> {
}
