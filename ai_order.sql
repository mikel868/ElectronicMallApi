/*
 AI订单表
*/

DROP TABLE IF EXISTS ai_order;
CREATE TABLE ai_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(255) NOT NULL COMMENT '商品名',
    num INT NOT NULL COMMENT '数量',
    standard VARCHAR(255) COMMENT '规格',
    link_user VARCHAR(100) NOT NULL COMMENT '联系人',
    link_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    link_address VARCHAR(255) NOT NULL COMMENT '送货地址',
    INDEX idx_link_phone (link_phone),
    INDEX idx_link_user (link_user),
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_ai_order_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT 'AI订单表';

-- 插入一条示例数据
INSERT INTO ai_order (user_id, name, num, standard, link_user, link_phone, link_address) 
VALUES (1, 'AI智能音箱', 1, '黑色款', '张三', '13800138000', '北京市朝阳区某某街道123号');