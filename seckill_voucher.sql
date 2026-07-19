-- =============================================
-- 秒杀券系统数据库设计（完全符合实体类）
-- 管理员：创建/删除秒杀券，查看状态
-- 用户：抢购秒杀券并记录到用户记录表，购物车结算时使用
-- 数据流程：
-- 1. 用户成功抢购秒杀券 -> user_seckill_record表记录
-- 2. 用户下单结算时 -> 创建seckill_order订单
-- 3. 订单完成后 -> 更新user_seckill_record为已使用状态
-- =============================================

-- 1. 秒杀券表（对应 SeckillVoucher 实体）
CREATE TABLE `voucher` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `voucher_amount` decimal(12,2) NOT NULL COMMENT '券面值（优惠金额）',
  `min_amount` decimal(12,2) DEFAULT 0.00 COMMENT '使用门槛（满多少可用）',
  `stock` int(11) NOT NULL DEFAULT 0 COMMENT '库存数量',
  `begin_time` datetime NOT NULL COMMENT '秒杀开始时间',
  `end_time` datetime NOT NULL COMMENT '秒杀结束时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` int(1) NOT NULL DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已结束，3-已停用',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`) COMMENT '状态索引',
  KEY `idx_begin_time` (`begin_time`) COMMENT '开始时间索引',
  KEY `idx_end_time` (`end_time`) COMMENT '结束时间索引',
  KEY `idx_active_voucher` (`status`, `begin_time`, `end_time`) COMMENT '活跃券复合索引',
  -- 数据约束
  CONSTRAINT `chk_voucher_amount` CHECK (`voucher_amount` > 0),
  CONSTRAINT `chk_stock` CHECK (`stock` >= 0),
  CONSTRAINT `chk_time` CHECK (`end_time` > `begin_time`),
  CONSTRAINT `chk_voucher_status` CHECK (`status` IN (0, 1, 2, 3))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀券表';

-- 2. 用户秒杀券记录表（记录成功获取秒杀券的用户）
CREATE TABLE `user_seckill_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `seckill_id` bigint(20) NOT NULL COMMENT '秒杀券ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '获取时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_used` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已使用：0-未使用，1-已使用',
  `used_time` datetime DEFAULT NULL COMMENT '使用时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_seckill` (`user_id`, `seckill_id`) COMMENT '防重复获取唯一索引',
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
  KEY `idx_seckill_id` (`seckill_id`) COMMENT '秒杀券ID索引',
  KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引',
  KEY `idx_user_used` (`user_id`, `is_used`) COMMENT '用户使用状态复合索引',
  CONSTRAINT `fk_user_seckill_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_seckill_voucher` FOREIGN KEY (`seckill_id`) REFERENCES `voucher` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户秒杀券记录表';

-- 3. 秒杀订单表（简化版本，专注核心订单信息）
CREATE TABLE `seckill_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `voucher_id` bigint(20) NOT NULL COMMENT '优惠券ID',
  `pay_type` tinyint(1) unsigned NOT NULL DEFAULT 1 COMMENT '支付方式：1-余额支付，2-支付宝，3-微信支付',
  `status` tinyint(1) unsigned NOT NULL DEFAULT 1 COMMENT '订单状态：1-未支付，2-已支付，3-已取消',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `pay_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `use_time` timestamp NULL DEFAULT NULL COMMENT '使用时间',
  `refund_time` timestamp NULL DEFAULT NULL COMMENT '退款时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
  KEY `idx_voucher_id` (`voucher_id`) COMMENT '优惠券ID索引',
  KEY `idx_status` (`status`) COMMENT '订单状态索引',
  -- 外键约束
  CONSTRAINT `fk_voucher_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_voucher_order_voucher` FOREIGN KEY (`voucher_id`) REFERENCES `voucher` (`id`) ON DELETE RESTRICT,
  -- 字段值约束
  CONSTRAINT `chk_order_pay_type` CHECK (`pay_type` IN (1, 2, 3)),
  CONSTRAINT `chk_order_status` CHECK (`status` IN (1, 2, 3))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券订单表';

-- 4. 秒杀券示例数据
-- 1. 插入秒杀券数据
INSERT INTO `voucher` (
    `id`, `voucher_amount`, `min_amount`, `stock`,
    `begin_time`, `end_time`, `status`
) VALUES (
             5001, 100.00, 200.00, 950,
             '2024-12-15 00:00:00', '2024-12-20 23:59:59', 1
         );

-- 2. 插入用户秒杀券记录数据
INSERT INTO `user_seckill_record` (
    `user_id`, `seckill_id`, `is_used`, `create_time`, `update_time`
) VALUES (
             2, 5001, 0, '2024-12-15 10:30:00', '2024-12-15 10:30:00'
         );

-- 3. 插入秒杀订单数据（使用新的简化表结构）
INSERT INTO `seckill_order` (
    `user_id`, `voucher_id`, `pay_type`, `status`,
    `create_time`, `update_time`
) VALUES (
             2, 5001, 1, 2,
             '2024-12-15 10:30:00', '2024-12-15 10:30:00'
         );

-- 5. 性能优化索引
-- 针对秒杀订单统计查询优化
ALTER TABLE `seckill_order` ADD INDEX `idx_order_stats` (`voucher_id`, `create_time`, `status`);
ALTER TABLE `voucher` ADD INDEX `idx_stock_stats` (`stock`);
-- 针对用户秒杀券记录查询优化
ALTER TABLE `user_seckill_record` ADD INDEX `idx_seckill_used_stats` (`seckill_id`, `is_used`, `create_time`);