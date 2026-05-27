-- 三代互助社区平台 - 数据库初始化脚本
-- 版本: 1.0.1
-- 日期: 2026-05-18
-- 更新: 统一数据库名称为 mutual_help

CREATE DATABASE IF NOT EXISTS mutual_help
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE mutual_help;

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `openid` VARCHAR(100) DEFAULT NULL COMMENT '微信OpenID',
    `user_type` TINYINT NOT NULL DEFAULT 1 COMMENT '用户类型: 1-年轻人 2-上班族 3-老年人',
    `id_card` VARCHAR(255) DEFAULT NULL COMMENT '身份证号(加密)',
    `id_card_verified` TINYINT DEFAULT 0 COMMENT '身份证认证状态: 0-未认证 1-已认证',
    `face_verified` TINYINT DEFAULT 0 COMMENT '人脸认证状态: 0-未认证 1-已认证',
    `payment_password` VARCHAR(255) DEFAULT NULL COMMENT '支付密码(加密)',
    `credit_score` INT DEFAULT 100 COMMENT '信用积分',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `password_history` TEXT DEFAULT NULL COMMENT '密码历史(JSON格式,存储最近5次密码哈希)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 师徒关系表
CREATE TABLE IF NOT EXISTS `mentor_relation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关系ID',
    `elder_id` BIGINT NOT NULL COMMENT '老年人ID',
    `young_id` BIGINT NOT NULL COMMENT '年轻人ID',
    `relation_type` TINYINT NOT NULL DEFAULT 1 COMMENT '关系类型: 1-师徒 2-互助',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-解除 1-生效',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_elder_id` (`elder_id`),
    KEY `idx_young_id` (`young_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='师徒关系表';

-- 互助心愿表
CREATE TABLE IF NOT EXISTS `wish` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '心愿ID',
    `user_id` BIGINT NOT NULL COMMENT '发布者ID',
    `title` VARCHAR(200) NOT NULL COMMENT '心愿标题',
    `content` TEXT NOT NULL COMMENT '心愿内容',
    `category` TINYINT NOT NULL COMMENT '分类: 1-学习类 2-陪伴类 3-协助类 4-圆梦类 5-技能类',
    `reward_credit` INT DEFAULT 0 COMMENT '奖励积分',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-待认领 2-进行中 3-已完成 4-已取消',
    `claimed_by` BIGINT DEFAULT NULL COMMENT '认领者ID',
    `claim_time` DATETIME DEFAULT NULL COMMENT '认领时间',
    `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='互助心愿表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '下单用户ID',
    `provider_id` BIGINT NOT NULL COMMENT '服务提供者ID',
    `service_type` VARCHAR(50) NOT NULL COMMENT '服务类型',
    `title` VARCHAR(200) NOT NULL COMMENT '服务标题',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    `actual_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '实际支付金额',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-待支付 2-已支付 3-服务中 4-已完成 5-已取消 6-退款中 7-已退款',
    `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `accept_time` DATETIME DEFAULT NULL COMMENT '接单时间',
    `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `is_delay_pay` TINYINT DEFAULT 0 COMMENT '是否延迟到账: 0-否 1-是',
    `delay_end_time` DATETIME DEFAULT NULL COMMENT '延迟到账结束时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_provider_id` (`provider_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 积分变动表
CREATE TABLE IF NOT EXISTS `credit_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `change_amount` INT NOT NULL COMMENT '变动积分(正负)',
    `balance` INT NOT NULL COMMENT '变动后余额',
    `type` TINYINT NOT NULL COMMENT '类型: 1-互助获得 2-积分兑换 3-活动奖励 4-扣减',
    `source` VARCHAR(50) DEFAULT NULL COMMENT '来源: order/wish/activity',
    `source_id` VARCHAR(50) DEFAULT NULL COMMENT '来源ID',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分变动表';

-- 消息通知表
CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
    `type` TINYINT NOT NULL COMMENT '类型: 1-订单通知 2-活动通知 3-系统通知 4-私信',
    `title` VARCHAR(200) DEFAULT NULL COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '内容',
    `related_type` VARCHAR(50) DEFAULT NULL COMMENT '关联类型',
    `related_id` VARCHAR(50) DEFAULT NULL COMMENT '关联ID',
    `is_read` TINYINT DEFAULT 0 COMMENT '已读: 0-未读 1-已读',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

-- 用户画像表
CREATE TABLE IF NOT EXISTS `user_profile` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `age` INT DEFAULT NULL COMMENT '年龄',
    `gender` TINYINT DEFAULT NULL COMMENT '性别: 1-男 2-女 3-未知',
    `profession` VARCHAR(100) DEFAULT NULL COMMENT '职业',
    `skills` TEXT DEFAULT NULL COMMENT '技能JSON',
    `interests` TEXT DEFAULT NULL COMMENT '兴趣JSON',
    `address` VARCHAR(500) DEFAULT NULL COMMENT '地址',
    `latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
    `longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
    `bio` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
    `tags` VARCHAR(500) DEFAULT NULL COMMENT '标签',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户画像表';

-- 适老化设置表
CREATE TABLE IF NOT EXISTS `accessibility_setting` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `elder_mode` TINYINT DEFAULT 0 COMMENT '适老模式: 0-关闭 1-开启',
    `font_size` INT DEFAULT 14 COMMENT '字体大小',
    `voice_broadcast` TINYINT DEFAULT 0 COMMENT '语音播报: 0-关闭 1-开启',
    `high_contrast` TINYINT DEFAULT 0 COMMENT '高对比度: 0-关闭 1-开启',
    `simplified_ui` TINYINT DEFAULT 0 COMMENT '简化界面: 0-关闭 1-开启',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='适老化设置表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '操作用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    `operation` VARCHAR(100) NOT NULL COMMENT '操作',
    `method` VARCHAR(200) DEFAULT NULL COMMENT '方法',
    `params` TEXT DEFAULT NULL COMMENT '参数',
    `result` TEXT DEFAULT NULL COMMENT '结果',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 好友关系表
CREATE TABLE IF NOT EXISTS `friend_relation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关系ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `friend_id` BIGINT NOT NULL COMMENT '好友ID',
    `remark` VARCHAR(100) DEFAULT NULL COMMENT '好友备注',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待验证 1-已同意 2-已拒绝 3-已删除',
    `apply_message` VARCHAR(500) DEFAULT NULL COMMENT '申请消息',
    `apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `agree_time` DATETIME DEFAULT NULL COMMENT '同意时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_friend_id` (`friend_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';

-- 群聊表
CREATE TABLE IF NOT EXISTS `group_chat` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '群聊ID',
    `group_no` VARCHAR(50) NOT NULL COMMENT '群聊编号',
    `name` VARCHAR(100) NOT NULL COMMENT '群聊名称',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '群头像URL',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '群聊描述',
    `owner_id` BIGINT NOT NULL COMMENT '群主ID',
    `member_count` INT DEFAULT 1 COMMENT '成员数量',
    `max_members` INT DEFAULT 500 COMMENT '最大成员数',
    `join_type` TINYINT DEFAULT 1 COMMENT '加入方式: 1-自由加入 2-需要验证 3-禁止加入',
    `chat_type` TINYINT DEFAULT 1 COMMENT '群聊类型: 1-普通群 2-工作群 3-兴趣群',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-已解散 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_no` (`group_no`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群聊表';

-- 群聊成员表
CREATE TABLE IF NOT EXISTS `group_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '成员ID',
    `group_id` BIGINT NOT NULL COMMENT '群聊ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` TINYINT DEFAULT 3 COMMENT '角色: 1-群主 2-管理员 3-普通成员',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT '群昵称',
    `join_type` TINYINT DEFAULT 1 COMMENT '加入方式: 1-创建 2-邀请 3-申请加入',
    `inviter_id` BIGINT DEFAULT NULL COMMENT '邀请人ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-已退出 1-正常 2-已踢出',
    `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `quit_time` DATETIME DEFAULT NULL COMMENT '退出时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_user` (`group_id`, `user_id`),
    KEY `idx_group_id` (`group_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群聊成员表';

-- 群聊消息表
CREATE TABLE IF NOT EXISTS `group_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `group_id` BIGINT NOT NULL COMMENT '群聊ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `message_type` TINYINT DEFAULT 1 COMMENT '消息类型: 1-文本 2-图片 3-语音 4-视频 5-文件 6-系统通知',
    `content` TEXT DEFAULT NULL COMMENT '消息内容',
    `media_url` VARCHAR(500) DEFAULT NULL COMMENT '媒体文件URL',
    `is_read` TINYINT DEFAULT 0 COMMENT '已读状态: 0-未读 1-已读',
    `read_count` INT DEFAULT 0 COMMENT '已读人数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_group_id` (`group_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群聊消息表';

-- 聊天会话表
CREATE TABLE IF NOT EXISTS `chat_session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `session_type` TINYINT NOT NULL COMMENT '会话类型: 1-单聊 2-群聊',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_id` BIGINT NOT NULL COMMENT '目标ID(好友ID或群聊ID)',
    `last_message` VARCHAR(500) DEFAULT NULL COMMENT '最后一条消息',
    `last_message_time` DATETIME DEFAULT NULL COMMENT '最后消息时间',
    `unread_count` INT DEFAULT 0 COMMENT '未读消息数',
    `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶: 0-否 1-是',
    `is_mute` TINYINT DEFAULT 0 COMMENT '是否免打扰: 0-否 1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_id`, `session_type`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_last_message_time` (`last_message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- 管理员用户表
CREATE TABLE IF NOT EXISTS `admin_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(100) DEFAULT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `role` TINYINT NOT NULL DEFAULT 1 COMMENT '角色: 1-普通管理员 2-超级管理员',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员用户表';

-- 插入默认管理员账号 (密码: admin123)
INSERT INTO `admin_user` (`username`, `password`, `real_name`, `phone`, `role`, `status`) 
VALUES ('admin', '$2a$10$dLZaFMdRl2Gv9p7ZLhv5NeXc/2JGxJ1tYH2fqGjp.j5VZVZZKuqO', '系统管理员', '13800138000', 2, 1);

-- 创建索引优化
CREATE INDEX `idx_operation_log_create_time` ON `operation_log` (`create_time`);
CREATE INDEX `idx_credit_log_create_time` ON `credit_log` (`create_time`);
CREATE INDEX `idx_message_create_time` ON `message` (`create_time`);
CREATE INDEX `idx_order_create_time` ON `order` (`create_time`);
CREATE INDEX `idx_wish_create_time` ON `wish` (`create_time`);
