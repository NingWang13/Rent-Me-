-- ============================================
-- 社区互助平台 - 数据库初始化脚本
-- 版本: 1.0.0
-- 数据库: MySQL 8.0+
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS community_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE community_platform;

-- ============================================
-- 用户模块 (user-service)
-- ============================================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(256) NOT NULL COMMENT '密码(加密)',
    phone VARCHAR(20) COMMENT '手机号',
    nickname VARCHAR(64) COMMENT '昵称',
    avatar_url VARCHAR(512) COMMENT '头像URL',
    user_type TINYINT DEFAULT 1 COMMENT '用户类型: 1-普通用户 2-服务提供者 3-管理员',
    id_card VARCHAR(64) COMMENT '身份证号(加密)',
    id_card_verified TINYINT DEFAULT 0 COMMENT '身份证是否认证: 0-未认证 1-已认证',
    face_verified TINYINT DEFAULT 0 COMMENT '人脸是否认证: 0-未认证 1-已认证',
    payment_password VARCHAR(256) COMMENT '支付密码(加密)',
    credit_score INT DEFAULT 100 COMMENT '信用分',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    password_history JSON COMMENT '密码历史',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    INDEX idx_username (username),
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户地址表
CREATE TABLE IF NOT EXISTS user_address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '地址ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    contact_name VARCHAR(64) NOT NULL COMMENT '联系人姓名',
    contact_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    province VARCHAR(64) COMMENT '省份',
    city VARCHAR(64) COMMENT '城市',
    district VARCHAR(64) COMMENT '区县',
    detail_address VARCHAR(256) NOT NULL COMMENT '详细地址',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认: 0-否 1-是',
    latitude DECIMAL(10, 7) COMMENT '纬度',
    longitude DECIMAL(10, 7) COMMENT '经度',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址表';

-- ============================================
-- 订单模块 (order-service)
-- ============================================

-- 订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '发起人ID',
    provider_id BIGINT COMMENT '服务提供者ID',
    service_type VARCHAR(64) NOT NULL COMMENT '服务类型',
    title VARCHAR(256) NOT NULL COMMENT '标题',
    description TEXT COMMENT '描述',
    amount DECIMAL(10, 2) NOT NULL COMMENT '金额',
    actual_amount DECIMAL(10, 2) COMMENT '实际支付金额',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待接单 1-进行中 2-已完成 3-已取消 4-已退款',
    payment_time DATETIME COMMENT '支付时间',
    accept_time DATETIME COMMENT '接单时间',
    complete_time DATETIME COMMENT '完成时间',
    is_delay_pay TINYINT DEFAULT 0 COMMENT '是否延迟支付',
    delay_end_time DATETIME COMMENT '延迟支付结束时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_provider_id (provider_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 心愿表
CREATE TABLE IF NOT EXISTS wish (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '心愿ID',
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    title VARCHAR(256) NOT NULL COMMENT '标题',
    description TEXT COMMENT '描述',
    category VARCHAR(64) COMMENT '分类',
    reward DECIMAL(10, 2) COMMENT '报酬',
    location VARCHAR(256) COMMENT '地点',
    latitude DECIMAL(10, 7) COMMENT '纬度',
    longitude DECIMAL(10, 7) COMMENT '经度',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待接 1-进行中 2-已完成 3-已取消',
    expire_time DATETIME COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_create_time (create_time),
    SPATIAL INDEX idx_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心愿表';

-- ============================================
-- 支付模块 (payment-service)
-- ============================================

-- 支付交易表
CREATE TABLE IF NOT EXISTS payment_transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '交易ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount DECIMAL(10, 2) NOT NULL COMMENT '金额',
    payment_method TINYINT COMMENT '支付方式: 1-微信 2-支付宝 3-余额',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待支付 1-支付中 2-成功 3-失败 4-已退款',
    transaction_no VARCHAR(64) NOT NULL UNIQUE COMMENT '交易流水号',
    channel_transaction_no VARCHAR(128) COMMENT '渠道交易号',
    error_message VARCHAR(512) COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_transaction_no (transaction_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付交易表';

-- 支付风控表
CREATE TABLE IF NOT EXISTS payment_risk_control (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    payment_password_fail_count INT DEFAULT 0 COMMENT '支付密码失败次数',
    payment_password_lock_time DATETIME COMMENT '支付密码锁定时间',
    daily_payment_amount INT DEFAULT 0 COMMENT '当日支付金额',
    last_payment_time DATETIME COMMENT '最后支付时间',
    face_verified TINYINT DEFAULT 0 COMMENT '人脸是否验证',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付风控表';

-- ============================================
-- 消息模块 (message-service)
-- ============================================

-- 消息表
CREATE TABLE IF NOT EXISTS message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(256) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    type TINYINT COMMENT '类型: 1-系统 2-订单 3-支付 4-积分 5-好友',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-无效 1-有效',
    related_id BIGINT COMMENT '关联ID',
    related_type VARCHAR(64) COMMENT '关联类型',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
    read_time DATETIME COMMENT '阅读时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 好友关系表
CREATE TABLE IF NOT EXISTS friend_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关系ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    friend_id BIGINT NOT NULL COMMENT '好友ID',
    remark VARCHAR(64) COMMENT '备注名',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待同意 1-已好友 2-已拒绝',
    apply_message VARCHAR(512) COMMENT '申请消息',
    apply_time DATETIME COMMENT '申请时间',
    agree_time DATETIME COMMENT '同意时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_friend_id (friend_id),
    INDEX idx_status (status),
    UNIQUE KEY uk_user_friend (user_id, friend_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';

-- 群聊表
CREATE TABLE IF NOT EXISTS group_chat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '群聊ID',
    name VARCHAR(128) NOT NULL COMMENT '群名称',
    avatar_url VARCHAR(512) COMMENT '群头像',
    owner_id BIGINT NOT NULL COMMENT '群主ID',
    member_count INT DEFAULT 1 COMMENT '成员数量',
    max_members INT DEFAULT 200 COMMENT '最大成员数',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-已解散 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_owner_id (owner_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群聊表';

-- 群成员表
CREATE TABLE IF NOT EXISTS group_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '成员ID',
    group_id BIGINT NOT NULL COMMENT '群聊ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role TINYINT DEFAULT 0 COMMENT '角色: 0-成员 1-管理员 2-群主',
    nickname VARCHAR(64) COMMENT '群昵称',
    join_type TINYINT DEFAULT 0 COMMENT '加入方式: 0-邀请 1-申请',
    inviter_id BIGINT COMMENT '邀请人ID',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-已退出 1-正常',
    join_time DATETIME COMMENT '加入时间',
    quit_time DATETIME COMMENT '退出时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_group_id (group_id),
    INDEX idx_user_id (user_id),
    UNIQUE KEY uk_group_user (group_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群成员表';

-- 聊天会话表
CREATE TABLE IF NOT EXISTS chat_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '会话ID',
    session_type TINYINT NOT NULL COMMENT '会话类型: 1-单聊 2-群聊',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_id BIGINT NOT NULL COMMENT '目标ID(好友ID或群ID)',
    last_message VARCHAR(512) COMMENT '最后一条消息',
    last_message_time DATETIME COMMENT '最后消息时间',
    unread_count INT DEFAULT 0 COMMENT '未读消息数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_target_id (target_id),
    UNIQUE KEY uk_session (user_id, target_id, session_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- ============================================
-- 积分模块 (credit-service)
-- ============================================

-- 积分日志表
CREATE TABLE IF NOT EXISTS credit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    change_amount INT NOT NULL COMMENT '变动金额(正负)',
    balance_after INT NOT NULL COMMENT '变动后余额',
    type TINYINT COMMENT '类型: 1-签到 2-完成任务 3-发布心愿 4-评价 5-系统发放 6-扣减',
    related_id BIGINT COMMENT '关联ID',
    related_type VARCHAR(64) COMMENT '关联类型',
    description VARCHAR(512) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分日志表';

-- ============================================
-- 活动模块 (activity-service)
-- ============================================

-- 活动表
CREATE TABLE IF NOT EXISTS activity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '活动ID',
    title VARCHAR(256) NOT NULL COMMENT '活动标题',
    description TEXT COMMENT '活动描述',
    cover_url VARCHAR(512) COMMENT '封面图',
    organizer_id BIGINT NOT NULL COMMENT '组织者ID',
    category VARCHAR(64) COMMENT '分类',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    location VARCHAR(256) COMMENT '地点',
    max_participants INT COMMENT '最大参与人数',
    current_participants INT DEFAULT 0 COMMENT '当前参与人数',
    credit_reward INT DEFAULT 0 COMMENT '积分奖励',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-报名中 1-进行中 2-已结束 3-已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_organizer_id (organizer_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';

-- 活动报名表
CREATE TABLE IF NOT EXISTS activity_signup (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '报名ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-已报名 1-已参加 2-已取消',
    check_in_time DATETIME COMMENT '签到时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_activity_id (activity_id),
    INDEX idx_user_id (user_id),
    UNIQUE KEY uk_activity_user (activity_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动报名表';

-- ============================================
-- 服务模块 (service-service)
-- ============================================

-- 服务进度表
CREATE TABLE IF NOT EXISTS service_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '进度ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    step TINYINT NOT NULL COMMENT '步骤序号',
    title VARCHAR(256) NOT NULL COMMENT '步骤标题',
    description TEXT COMMENT '步骤描述',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-未完成 1-已完成',
    images JSON COMMENT '图片列表',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务进度表';

-- ============================================
-- 管理后台 (admin-service)
-- ============================================

-- 管理员表
CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '管理员ID',
    username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(256) NOT NULL COMMENT '密码(加密)',
    real_name VARCHAR(64) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(128) COMMENT '邮箱',
    role TINYINT DEFAULT 3 COMMENT '角色: 1-超级管理员 2-管理员 3-操作员',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 无障碍设置表
CREATE TABLE IF NOT EXISTS user_accessibility_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '设置ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    large_font TINYINT DEFAULT 0 COMMENT '是否大字体: 0-否 1-是',
    high_contrast TINYINT DEFAULT 0 COMMENT '是否高对比度: 0-否 1-是',
    simplify_mode TINYINT DEFAULT 0 COMMENT '是否简化模式: 0-否 1-是',
    reduce_motion TINYINT DEFAULT 0 COMMENT '是否减少动画: 0-否 1-是',
    font_size INT DEFAULT 32 COMMENT '字体大小',
    settings_json JSON COMMENT '完整设置JSON',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户无障碍设置表';

-- ============================================
-- 插入初始数据
-- ============================================

-- 订单审计日志表
CREATE TABLE IF NOT EXISTS order_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '审计日志ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '操作用户ID',
    action VARCHAR(32) NOT NULL COMMENT '操作类型: CREATE/ACCEPT/COMPLETE/CANCEL',
    old_status TINYINT COMMENT '变更前状态',
    new_status TINYINT COMMENT '变更后状态',
    detail VARCHAR(512) COMMENT '操作详情',
    operator_ip VARCHAR(64) COMMENT '操作IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id),
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单审计日志表';

-- 插入默认管理员 (密码: admin123)
INSERT IGNORE INTO admin_user (username, password, real_name, role, status) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 1, 1);

-- 插入测试用户 (密码: user123)
INSERT IGNORE INTO sys_user (username, password, nickname, user_type, credit_score, status) 
VALUES ('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户', 1, 100, 1);

-- 插入服务类型字典数据
CREATE TABLE IF NOT EXISTS sys_dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典ID',
    dict_type VARCHAR(64) NOT NULL COMMENT '字典类型',
    dict_key VARCHAR(64) NOT NULL COMMENT '字典键',
    dict_value VARCHAR(256) NOT NULL COMMENT '字典值',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统字典表';

INSERT IGNORE INTO sys_dict (dict_type, dict_key, dict_value, sort) VALUES
('service_type', 'errand', '跑腿代办', 1),
('service_type', 'companion', '陪伴服务', 2),
('service_type', 'shopping', '代购代买', 3),
('service_type', 'care', '照护服务', 4),
('service_type', 'tech', '技术支持', 5),
('service_type', 'other', '其他', 6);
