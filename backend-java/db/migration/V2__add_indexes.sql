-- ============================================
-- 数据库索引优化脚本
-- 互助社区平台 - 性能优化
-- 执行日期: 2026-05-08
-- ============================================

-- 1. 用户表索引优化
-- 查询场景：登录验证、用户搜索、状态筛选
CREATE INDEX IF NOT EXISTS idx_user_openid ON user(openid);
CREATE INDEX IF NOT EXISTS idx_user_phone ON user(phone);
CREATE INDEX IF NOT EXISTS idx_user_status_deleted ON user(status, deleted);
CREATE INDEX IF NOT EXISTS idx_user_create_time ON user(create_time DESC);

-- 2. 订单表索引优化
-- 查询场景：用户订单列表、状态筛选、时间范围查询
CREATE INDEX IF NOT EXISTS idx_order_user_id ON `order`(user_id);
CREATE INDEX IF NOT EXISTS idx_order_wish_id ON `order`(wish_id);
CREATE INDEX IF NOT EXISTS idx_order_status ON `order`(status);
CREATE INDEX IF NOT EXISTS idx_order_user_status ON `order`(user_id, status);
CREATE INDEX IF NOT EXISTS idx_order_user_create_time ON `order`(user_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_order_status_create_time ON `order`(status, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_order_deleted ON `order`(deleted);

-- 3. 心愿表索引优化
-- 查询场景：心愿列表、分类筛选、状态查询
CREATE INDEX IF NOT EXISTS idx_wish_user_id ON wish(user_id);
CREATE INDEX IF NOT EXISTS idx_wish_category ON wish(category);
CREATE INDEX IF NOT EXISTS idx_wish_status ON wish(status);
CREATE INDEX IF NOT EXISTS idx_wish_user_status ON wish(user_id, status);
CREATE INDEX IF NOT EXISTS idx_wish_status_create_time ON wish(status, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_wish_deleted ON wish(deleted);

-- 4. 消息表索引优化
-- 查询场景：用户消息列表、未读消息、会话查询
CREATE INDEX IF NOT EXISTS idx_message_session_id ON message(session_id);
CREATE INDEX IF NOT EXISTS idx_message_sender_id ON message(sender_id);
CREATE INDEX IF NOT EXISTS idx_message_receiver_id ON message(receiver_id);
CREATE INDEX IF NOT EXISTS idx_message_is_read ON message(is_read);
CREATE INDEX IF NOT EXISTS idx_message_receiver_is_read ON message(receiver_id, is_read);
CREATE INDEX IF NOT EXISTS idx_message_session_create_time ON message(session_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_message_deleted ON message(deleted);

-- 5. 支付交易表索引优化
-- 查询场景：支付状态查询、用户交易记录
CREATE INDEX IF NOT EXISTS idx_payment_order_id ON payment_transaction(order_id);
CREATE INDEX IF NOT EXISTS idx_payment_user_id ON payment_transaction(user_id);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payment_transaction(status);
CREATE INDEX IF NOT EXISTS idx_payment_trade_no ON payment_transaction(trade_no);
CREATE INDEX IF NOT EXISTS idx_payment_user_create_time ON payment_transaction(user_id, create_time DESC);

-- 6. 积分日志表索引优化
-- 查询场景：用户积分明细、积分变动记录
CREATE INDEX IF NOT EXISTS idx_credit_user_id ON credit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_credit_type ON credit_log(type);
CREATE INDEX IF NOT EXISTS idx_credit_user_create_time ON credit_log(user_id, create_time DESC);

-- 7. 活动表索引优化
-- 查询场景：活动列表、状态筛选、报名查询
CREATE INDEX IF NOT EXISTS idx_activity_status ON activity(status);
CREATE INDEX IF NOT EXISTS idx_activity_start_time ON activity(start_time);
CREATE INDEX IF NOT EXISTS idx_activity_status_start_time ON activity(status, start_time);
CREATE INDEX IF NOT EXISTS idx_activity_deleted ON activity(deleted);

-- 8. 活动报名表索引优化
CREATE INDEX IF NOT EXISTS idx_signup_activity_id ON activity_signup(activity_id);
CREATE INDEX IF NOT EXISTS idx_signup_user_id ON activity_signup(user_id);
CREATE INDEX IF NOT EXISTS idx_signup_activity_status ON activity_signup(activity_id, status);

-- 9. 群聊表索引优化
CREATE INDEX IF NOT EXISTS idx_group_chat_creator ON group_chat(creator_id);
CREATE INDEX IF NOT EXISTS idx_group_chat_status ON group_chat(status);

-- 10. 群成员表索引优化
CREATE INDEX IF NOT EXISTS idx_group_member_group_id ON group_member(group_id);
CREATE INDEX IF NOT EXISTS idx_group_member_user_id ON group_member(user_id);
CREATE INDEX IF NOT EXISTS idx_group_member_group_user ON group_member(group_id, user_id);

-- 11. 好友关系表索引优化
CREATE INDEX IF NOT EXISTS idx_friend_user_id ON friend_relation(user_id);
CREATE INDEX IF NOT EXISTS idx_friend_friend_id ON friend_relation(friend_id);
CREATE INDEX IF NOT EXISTS idx_friend_user_status ON friend_relation(user_id, status);

-- 12. 服务进度表索引优化
CREATE INDEX IF NOT EXISTS idx_service_progress_order_id ON service_progress(order_id);
CREATE INDEX IF NOT EXISTS idx_service_progress_user_id ON service_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_service_progress_type ON service_progress(type);

-- 13. 风控表索引优化
CREATE INDEX IF NOT EXISTS idx_risk_user_id ON payment_risk_control(user_id);
CREATE INDEX IF NOT EXISTS idx_risk_order_id ON payment_risk_control(order_id);
CREATE INDEX IF NOT EXISTS idx_risk_level ON payment_risk_control(risk_level);

-- 14. 反馈表索引优化
CREATE INDEX IF NOT EXISTS idx_feedback_user_id ON feedback(user_id);
CREATE INDEX IF NOT EXISTS idx_feedback_status ON feedback(status);
CREATE INDEX IF NOT EXISTS idx_feedback_type ON feedback(type);

-- 15. 审计日志表索引优化
CREATE INDEX IF NOT EXISTS idx_audit_order_id ON order_audit_log(order_id);
CREATE INDEX IF NOT EXISTS idx_audit_operator ON order_audit_log(operator_id);
CREATE INDEX IF NOT EXISTS idx_audit_action ON order_audit_log(action);
CREATE INDEX IF NOT EXISTS idx_audit_create_time ON order_audit_log(create_time DESC);

-- ============================================
-- 复合索引优化（针对高频查询场景）
-- ============================================

-- 订单高频查询：用户+状态+时间
CREATE INDEX IF NOT EXISTS idx_order_user_status_time ON `order`(user_id, status, create_time DESC);

-- 心愿高频查询：状态+分类+时间
CREATE INDEX IF NOT EXISTS idx_wish_status_category_time ON wish(status, category, create_time DESC);

-- 消息高频查询：接收者+已读+时间
CREATE INDEX IF NOT EXISTS idx_message_receiver_read_time ON message(receiver_id, is_read, create_time DESC);

-- 支付高频查询：订单+状态
CREATE INDEX IF NOT EXISTS idx_payment_order_status ON payment_transaction(order_id, status);

-- ============================================
-- 验证索引创建结果
-- ============================================
-- SELECT table_name, index_name, column_name, seq_in_index 
-- FROM information_schema.statistics 
-- WHERE table_schema = DATABASE() 
-- ORDER BY table_name, index_name, seq_in_index;
