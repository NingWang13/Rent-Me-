-- 三代互助社区平台 - 数据库初始化脚本（简化版）
-- 使用方法：
-- 1. 登录 MySQL：mysql -u root -p
-- 2. 执行此脚本：source C:\Users\Administrator\Desktop\小程序\scripts\init_database_simple.sql

-- 创建数据库
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
    `credit_score` INT DEFAULT 100 COMMENT '信用积分',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 互助心愿表
CREATE TABLE IF NOT EXISTS `wish` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '心愿ID',
    `user_id` BIGINT NOT NULL COMMENT '发布者ID',
    `title` VARCHAR(200) NOT NULL COMMENT '心愿标题',
    `content` TEXT NOT NULL COMMENT '心愿内容',
    `category` TINYINT NOT NULL COMMENT '分类: 1-学习类 2-陪伴类 3-协助类 4-圆梦类 5-技能类',
    `reward_credit` INT DEFAULT 0 COMMENT '奖励积分',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-待认领 2-进行中 3-已完成 4-已取消',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='互助心愿表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '下单用户ID',
    `provider_id` BIGINT NOT NULL COMMENT '服务提供者ID',
    `title` VARCHAR(200) NOT NULL COMMENT '服务标题',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-待支付 2-已支付 3-服务中 4-已完成 5-已取消',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 支付交易表
CREATE TABLE IF NOT EXISTS `payment_transaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '交易ID',
    `transaction_no` VARCHAR(50) NOT NULL COMMENT '交易号',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '金额',
    `payment_method` TINYINT NOT NULL COMMENT '支付方式: 1-微信支付',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待支付 1-已支付 2-支付失败',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_transaction_no` (`transaction_no`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付交易表';

-- 消息通知表
CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
    `type` TINYINT NOT NULL COMMENT '类型: 1-订单通知 2-活动通知 3-系统通知',
    `title` VARCHAR(200) DEFAULT NULL COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '内容',
    `is_read` TINYINT DEFAULT 0 COMMENT '已读: 0-未读 1-已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

-- 插入测试用户
INSERT IGNORE INTO `sys_user` (`username`, `password`, `phone`, `nickname`, `openid`, `user_type`, `credit_score`, `status`)
VALUES
('test_user1', '$2a$10$dLZaFMdRl2Gv9p7ZLhv5NeXc/2JGxJ1tYH2fqGjp.j5VZVZZKuqO', '13800138001', '测试用户1', 'test_openid_001', 1, 100, 1),
('test_user2', '$2a$10$dLZaFMdRl2Gv9p7ZLhv5NeXc/2JGxJ1tYH2fqGjp.j5VZVZZKuqO', '13800138002', '测试用户2', 'test_openid_002', 3, 120, 1);

-- 插入测试心愿
INSERT IGNORE INTO `wish` (`user_id`, `title`, `content`, `category`, `reward_credit`, `status`)
VALUES
(1, '教我使用智能手机', '希望年轻人能教我如何使用智能手机的基本功能', 2, 10, 1),
(2, '帮忙买菜', '行动不便，希望有人能帮忙买菜', 3, 15, 1);

-- 提交
COMMIT;
