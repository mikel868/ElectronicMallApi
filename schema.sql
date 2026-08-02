-- MySQL dump 10.13  Distrib 8.0.39, for Win64 (x86_64)
--
-- Host: localhost    Database: electronic_mall
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `address`
--

DROP TABLE IF EXISTS `address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `address` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `link_user` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '联系人',
  `link_address` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '地址',
  `link_phone` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '电话',
  `user_id` bigint DEFAULT NULL COMMENT '所属用户',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin ROW_FORMAT=DYNAMIC COMMENT='地址表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address`
--

LOCK TABLES `address` WRITE;
/*!40000 ALTER TABLE `address` DISABLE KEYS */;
INSERT INTO `address` VALUES (1,'张三','北京市','13333333333',1),(2,'张三','北京市','15888888888',2),(3,'张三','上海市','15555555555',2),(4,'张三','新疆','15888888888',2),(5,'刘','广东省汕尾市','123456789',5),(6,'liu','广东省汕尾市陆河县','13345678912',7);
/*!40000 ALTER TABLE `address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_order`
--

DROP TABLE IF EXISTS `ai_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `name` varchar(255) NOT NULL COMMENT '商品名',
  `num` int NOT NULL COMMENT '数量',
  `standard` varchar(255) DEFAULT NULL COMMENT '规格',
  `link_user` varchar(100) NOT NULL COMMENT '联系人',
  `link_phone` varchar(20) NOT NULL COMMENT '联系电话',
  `link_address` varchar(255) NOT NULL COMMENT '送货地址',
  PRIMARY KEY (`id`),
  KEY `idx_link_phone` (`link_phone`),
  KEY `idx_link_user` (`link_user`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_ai_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_order`
--

LOCK TABLES `ai_order` WRITE;
/*!40000 ALTER TABLE `ai_order` DISABLE KEYS */;
INSERT INTO `ai_order` VALUES (1,1,'AI智能音箱',1,'黑色款','张三','13800138000','北京市朝阳区某某街道123号'),(2,1,'Redmi K70',1,'月白/8+256G','张三','13800138000','北京市朝阳区某某街道123号'),(3,1,'Redmi K70',1,'月白/12+512G','用户','13800000000','默认地址'),(4,2,'Redmi K70',1,'8GB+128GB','张三','147258369','饭都花园865号'),(5,2,'Redmi K70',1,'月白/8GB+128GB','张三','147258369','饭都花园865号'),(6,2,'Redmi K70',1,'8GB+128GB','张三','147258369','饭都花园865号'),(7,2,'Redmi K70',1,'月白/8+128GB','张三','147258369','饭都花园865号'),(8,2,'Redmi K70',1,'月白/8+128G','张三','147258369','饭都花园865号'),(9,2,'Redmi K70',1,'月白/8+256G','张三','147258369','饭都花园865号'),(10,2,'Redmi K70',1,'月白/8+256G','张三','147258369','饭都花园865号'),(11,2,'Redmi K70',1,'8GB+256GB 黑色','用户','13800000000','北京市朝阳区某街道'),(12,2,'Redmi K70',1,'8GB+256GB 月白色','张三','15888888888','上海市'),(13,2,'Redmi K70',1,'月白/8GB+256GB','张三','15888888888','上海市'),(14,2,'Redmi K70',1,'月白/8GB+256GB','张三','15888888888','上海市'),(15,2,'Redmi K70',1,'月白/8+256G','张三','15888888888','上海市'),(16,2,'Redmi K70',1,'月白/8GB+256GB','张三','15888888888','上海市'),(17,2,'Redmi K70',1,'月白/8GB+256GB','张三','15888888888','上海市'),(18,2,'Redmi K70',1,'月白/8GB+256GB','张三','15888888888','上海市'),(19,2,'Redmi K70',1,'月白/8GB+256GB','张三','15888888888','上海市'),(20,2,'Redmi K70',1,'8GB+256GB 月白色','张三','15888888888','上海市'),(21,2,'Redmi K70',1,'月白/8GB+256GB','张三','15888888888','上海市'),(22,2,'Redmi K70',1,'月白/8GB+256GB','张三','15888888888','上海市'),(23,2,'Redmi K70',1,'月白/8+256G','张三','15888888888','上海市'),(24,2,'Redmi K70',1,'月白/8+256G','张三','15888888888','上海市'),(25,2,'Redmi K70',1,'月白/8+128G','张三','15888888888','上海市'),(26,5,'Redmi K70',1,'月白/8+128G','刘','12345678912','广东汕尾陆河县河田镇'),(27,7,'Redmi K70',1,'月白/12+512G','张三','13800000000','北京市朝阳区xxx路xx号');
/*!40000 ALTER TABLE `ai_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `avatar`
--

DROP TABLE IF EXISTS `avatar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `avatar` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `size` bigint DEFAULT NULL,
  `url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `md5` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin ROW_FORMAT=DYNAMIC COMMENT='头像表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `avatar`
--

LOCK TABLES `avatar` WRITE;
/*!40000 ALTER TABLE `avatar` DISABLE KEYS */;
INSERT INTO `avatar` VALUES (2,'jpg',492,'/avatar/978418fbe75243b4ba38da389a468b78.jpg','1e5802c8b96198fd524cc91ad3f9d476'),(3,'jpg',146,'/avatar/e8663626d17b41bd89707299fcd5ac81.jpg','5c072037e4e9662831fe448e28795770'),(4,'jpg',175,'/avatar/09cd5add81ff4abfbd1ccf91b2e9c820.jpg','507704f05fbca53793bce9970b40e6c8');
/*!40000 ALTER TABLE `avatar` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carousel`
--

DROP TABLE IF EXISTS `carousel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carousel` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `good_id` bigint DEFAULT NULL COMMENT '对应的商品id',
  `show_order` int DEFAULT NULL COMMENT '播放顺序',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin ROW_FORMAT=DYNAMIC COMMENT='轮播图表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carousel`
--

LOCK TABLES `carousel` WRITE;
/*!40000 ALTER TABLE `carousel` DISABLE KEYS */;
INSERT INTO `carousel` VALUES (4,6,2),(5,4,3),(6,7,4);
/*!40000 ALTER TABLE `carousel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart`
--

DROP TABLE IF EXISTS `cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `count` int DEFAULT NULL COMMENT '数量',
  `create_time` datetime DEFAULT NULL COMMENT '加入时间',
  `good_id` bigint DEFAULT NULL COMMENT '商品id',
  `standard` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin ROW_FORMAT=DYNAMIC COMMENT='购物车表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart`
--

LOCK TABLES `cart` WRITE;
/*!40000 ALTER TABLE `cart` DISABLE KEYS */;
INSERT INTO `cart` VALUES (14,1,'2026-04-10 13:32:44',11,'普通版',3),(19,1,'2026-04-10 13:32:44',11,'普通版',2),(20,1,'2026-04-10 13:32:44',10,'标准30cm',2),(24,4,'2026-04-21 01:56:44',3,'桌子+椅子',1);
/*!40000 ALTER TABLE `cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '类别名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin ROW_FORMAT=DYNAMIC COMMENT='分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'女装'),(2,'男装'),(10,'运动鞋'),(11,'休闲鞋'),(12,'靴子'),(13,'办公用品'),(14,'书籍'),(15,'笔记本'),(16,'手机'),(17,'平板电脑'),(18,'烹饪食材'),(19,'白酒'),(20,'茶叶'),(21,'咖啡'),(22,'宠物用品'),(23,'宠物饲料'),(25,'生活用品');
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `good`
--

DROP TABLE IF EXISTS `good`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `good` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '商品名称',
  `description` varchar(1600) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '描述',
  `discount` double(10,2) NOT NULL DEFAULT '1.00' COMMENT '折扣',
  `sales` bigint NOT NULL DEFAULT '0' COMMENT '销量',
  `sale_money` double(10,2) DEFAULT '0.00' COMMENT '销售额',
  `category_id` bigint DEFAULT NULL COMMENT '分类id',
  `imgs` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '商品图片',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `recommend` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否推荐。0不推荐，1推荐',
  `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除，0未删除，1删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin ROW_FORMAT=DYNAMIC COMMENT='商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `good`
--

LOCK TABLES `good` WRITE;
/*!40000 ALTER TABLE `good` DISABLE KEYS */;
INSERT INTO `good` VALUES (2,'衬衫','鳄鱼夹克男春季新款休闲百搭翻领外套男中青年时尚潮流夹克衫男装上衣 绿色 XL(130-145斤)',0.95,35,13700.50,2,'/file/74488020672944968462e9e4a9c89096.png','2026-04-10 13:32:44',1,0),(3,'桌椅套装','这款桌椅套装是您家庭和办公室的理想选择。精心设计的桌子和舒适的椅子，完美结合，给您带来了坐姿舒适和优雅的工作环境。优质材料和坚固的结构确保您享受长久的使用寿命。',0.98,2,235.20,13,'/file/b4ac53ed62c74c298366619399c39f99.jpg','2026-04-10 13:32:44',1,0),(4,'威士忌 大瓶','这款洋酒是一款精心酿造的上乘佳酿，给您带来无与伦比的品尝享受。精选优质的原料，经过精心的发酵和蒸馏工艺，使得这款洋酒口感柔和，回味悠长。',0.80,0,0.00,19,'/file/be9d2d6a17c5436fb0b8c2f7927484b2.jpg','2026-04-10 13:32:44',1,0),(5,'女上衣','酒红色圆领短袖T恤女休闲2023年新款上衣修身拼色女装S8220334 白色 XL',0.80,2,232.00,1,'/file/15cb9fc604984dfa97e0e968eb1d196d.jpg','2026-04-10 13:32:44',1,0),(6,'《PSALMS》英文版 图书','《PSALMS》英文版是一本精美的图书，专门收录了许多有趣的诗篇。这本书为读者带来了感人至深的心灵之旅。',1.00,0,0.00,14,'/file/8dc5354c7332454796c614bb4a0572fb.jpg','2026-04-10 13:32:44',1,0),(7,'休闲鞋','男士运动休闲鞋软底网面鞋健步鞋黑灰色42',0.96,4,406.22,11,'/file/0afa4eb1c51943808f6e83cd9ced25e8.jpg','2026-04-10 13:32:44',1,0),(9,'儿童简笔画册','适合儿童简笔画上色的底稿',1.00,1,50.00,13,'/file/2e2a1df657324a3293642344327310cb.png','2026-04-10 13:32:44',0,0),(10,'墨镜','抵抗紫外线',1.00,1,60.00,13,'/file/449ab0163ba648c08cb4a76b40a9dcec.jpg','2026-04-10 13:32:44',0,0),(11,'水浒传','四大名著之一',1.00,20,475.00,14,'/file/7081c443758c488ea20f8fe7a1270444.png','2026-04-10 13:32:44',1,0),(12,'西游记','四大名著之一',1.00,2,48.00,14,'/file/0ae3b65ba4bc432492100a8babe1afb5.png','2026-04-10 13:32:44',1,0),(13,'Redmi K70','第二新的红米最新力作',1.00,1,NULL,16,'/file/dcdc5b72dc2e4ad9869ddf0c4d4a367d.png','2026-04-10 13:32:44',0,0),(14,'洗衣液','好用便宜',1.00,0,0.00,25,'/file/89b45f6592a5497f81c80f567471b900.png','2026-04-10 13:32:44',0,1),(15,'洗衣液','好用',1.00,0,0.00,25,'/file/89b45f6592a5497f81c80f567471b900.png','2026-04-10 13:32:44',0,1),(16,'洗衣液','好用',1.00,0,0.00,25,'/file/89b45f6592a5497f81c80f567471b900.png','2026-04-10 13:32:44',0,1);
/*!40000 ALTER TABLE `good` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `good_standard`
--

DROP TABLE IF EXISTS `good_standard`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `good_standard` (
  `good_id` bigint DEFAULT NULL COMMENT '商品id',
  `value` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '规格',
  `price` decimal(10,2) DEFAULT NULL COMMENT '价格',
  `store` bigint DEFAULT NULL COMMENT '库存'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin ROW_FORMAT=DYNAMIC COMMENT='商品规格表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `good_standard`
--

LOCK TABLES `good_standard` WRITE;
/*!40000 ALTER TABLE `good_standard` DISABLE KEYS */;
INSERT INTO `good_standard` VALUES (8,'123',123.00,80),(5,'白色',145.00,298),(7,'43码',115.00,146),(6,'英文版',99.00,500),(4,'单瓶',2600.00,500),(4,'三瓶送礼套装',6100.00,900),(3,'桌子',90.00,599),(3,'椅子',50.00,500),(3,'桌子+椅子',150.00,499),(2,'S 小码',129.00,498),(2,'M 中码',129.00,496),(2,'L 大码',129.00,496),(9,'标准版',50.00,599),(10,'标准30cm',60.00,499),(11,'普通版',19.00,0),(11,'精装版',29.00,2),(12,'普通版',19.00,9),(12,'精装版',29.00,4),(13,'月白/8+128G',2399.00,20),(13,'月白/8+256G',2599.00,20),(13,'月白/12+256G',3099.00,20),(13,'月白/12+512G',3299.00,29),(13,'黑曜/8+128G',2399.00,66),(13,'黑曜/8+256G',2599.00,66),(14,'普通',10.00,10),(15,'普通',12.00,123),(16,'普通',12.00,123);
/*!40000 ALTER TABLE `good_standard` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `icon`
--

DROP TABLE IF EXISTS `icon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `icon` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `value` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '图标的识别码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin ROW_FORMAT=DYNAMIC COMMENT='图标表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `icon`
--

LOCK TABLES `icon` WRITE;
/*!40000 ALTER TABLE `icon` DISABLE KEYS */;
INSERT INTO `icon` VALUES (1,'&#xe600;'),(15,'&#xe617;'),(16,'&#xe709;'),(17,'&#xe601;'),(18,'&#xe618;'),(19,'&#xe602;'),(21,'&#xe606;');
/*!40000 ALTER TABLE `icon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `icon_category`
--

DROP TABLE IF EXISTS `icon_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `icon_category` (
  `category_id` bigint NOT NULL COMMENT '分类id',
  `icon_id` bigint NOT NULL COMMENT '图标id',
  PRIMARY KEY (`category_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin ROW_FORMAT=DYNAMIC COMMENT='商品分类 - 图标关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `icon_category`
--

LOCK TABLES `icon_category` WRITE;
/*!40000 ALTER TABLE `icon_category` DISABLE KEYS */;
INSERT INTO `icon_category` VALUES (1,1),(2,1),(10,15),(11,15),(12,15),(13,16),(14,16),(15,17),(16,17),(17,17),(18,18),(19,19),(20,19),(21,19),(22,21),(23,21),(25,16);
/*!40000 ALTER TABLE `icon_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_goods`
--

DROP TABLE IF EXISTS `order_goods`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_goods` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint DEFAULT NULL COMMENT '订单id',
  `good_id` bigint DEFAULT NULL COMMENT '商品id',
  `count` int DEFAULT NULL COMMENT '数量',
  `standard` varchar(1600) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '规格',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_goods`
--

LOCK TABLES `order_goods` WRITE;
/*!40000 ALTER TABLE `order_goods` DISABLE KEYS */;
INSERT INTO `order_goods` VALUES (9,9,2,3,'M 中码'),(10,10,2,3,'L 大码'),(11,11,2,1,'S 小码'),(20,20,2,1,'M 中码'),(21,21,2,1,'L 大码'),(22,22,7,1,'43码'),(23,23,2,1,'S 小码'),(24,24,5,1,'白色'),(25,25,7,1,'43码'),(26,26,5,1,'白色'),(27,27,9,1,'标准版'),(28,28,3,1,'桌子'),(29,29,10,1,'标准30cm'),(30,30,12,1,'普通版'),(31,31,3,1,'桌子+椅子'),(32,32,12,1,'精装版'),(33,33,11,1,'普通版'),(43,41,13,1,'月白/8+256G'),(44,42,13,1,'月白/8+256G'),(45,43,13,1,'月白/8+128G'),(46,44,11,1,'普通版'),(47,45,11,10,'精装版'),(48,46,13,1,'月白/8+128G'),(49,47,11,10,'精装版'),(50,48,11,9,'普通版'),(51,49,12,9,'普通版'),(52,50,7,2,'43码'),(53,51,13,1,'月白/12+512G');
/*!40000 ALTER TABLE `order_goods` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `seckill_order`
--

DROP TABLE IF EXISTS `seckill_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seckill_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `voucher_id` bigint NOT NULL COMMENT '优惠券ID',
  `pay_type` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '支付方式：1-余额支付，2-支付宝，3-微信支付',
  `status` tinyint DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `pay_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `use_time` timestamp NULL DEFAULT NULL COMMENT '使用时间',
  `refund_time` timestamp NULL DEFAULT NULL COMMENT '退款时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
  KEY `idx_voucher_id` (`voucher_id`) COMMENT '优惠券ID索引',
  KEY `idx_status` (`status`) COMMENT '订单状态索引',
  KEY `idx_order_stats` (`voucher_id`,`create_time`,`status`),
  CONSTRAINT `fk_voucher_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_voucher_order_voucher` FOREIGN KEY (`voucher_id`) REFERENCES `voucher` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `chk_order_pay_type` CHECK ((`pay_type` in (1,2,3))),
  CONSTRAINT `chk_order_status` CHECK ((`status` in (1,2,3)))
) ENGINE=InnoDB AUTO_INCREMENT=7423607571044368400 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `seckill_order`
--

LOCK TABLES `seckill_order` WRITE;
/*!40000 ALTER TABLE `seckill_order` DISABLE KEYS */;
INSERT INTO `seckill_order` VALUES (1,2,5001,1,2,'2024-12-15 02:30:00',NULL,NULL,NULL,'2024-12-15 02:30:00'),(7421757320608088070,3,5002,1,1,'2025-10-09 09:18:42',NULL,NULL,NULL,'2025-10-09 09:18:42'),(7421828449561477134,5,5002,1,1,'2026-04-17 10:29:15',NULL,NULL,NULL,'2026-04-17 10:29:15'),(7423607571044368399,7,5003,1,1,'2026-04-17 12:55:31',NULL,NULL,NULL,'2026-04-17 12:55:31');
/*!40000 ALTER TABLE `seckill_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `standard`
--

DROP TABLE IF EXISTS `standard`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `standard` (
  `goodId` bigint NOT NULL COMMENT '商品id',
  `value` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '商品规格',
  `price` decimal(10,2) DEFAULT NULL COMMENT '该规格的价格',
  `store` bigint DEFAULT NULL COMMENT '该规格的库存',
  PRIMARY KEY (`goodId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin ROW_FORMAT=DYNAMIC COMMENT='规格表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `standard`
--

LOCK TABLES `standard` WRITE;
/*!40000 ALTER TABLE `standard` DISABLE KEYS */;
/*!40000 ALTER TABLE `standard` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_file`
--

DROP TABLE IF EXISTS `sys_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '文件名称',
  `type` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '文件类型',
  `size` bigint DEFAULT NULL COMMENT '大小',
  `url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '文件路径',
  `is_delete` tinyint(1) DEFAULT NULL COMMENT '是否删除',
  `enable` tinyint(1) DEFAULT NULL COMMENT '是否启用',
  `md5` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'md5值',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin ROW_FORMAT=DYNAMIC COMMENT='系统文件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_file`
--

LOCK TABLES `sys_file` WRITE;
/*!40000 ALTER TABLE `sys_file` DISABLE KEYS */;
INSERT INTO `sys_file` VALUES (7,'07.jpg','jpg',1814,'/file/7dfd10628edc4b4e97de19c1cb86585e.jpg',0,0,'04271616ebc6914643c3af592dd58bef'),(8,'9a49edb823cc4cb799cb3438a7419a83.jpg','jpg',132,'/file/2b6249b9ba24491a9048c1e8c0b5256e.jpg',0,0,'f11ed5acc29b90770a453eceb7524712'),(9,'9a49edb823cc4cb799cb3438a7419a83.jpg','jpg',132,'/file/2b6249b9ba24491a9048c1e8c0b5256e.jpg',0,0,'f11ed5acc29b90770a453eceb7524712'),(10,'9a49edb823cc4cb799cb3438a7419a83.jpg','jpg',132,'/file/2b6249b9ba24491a9048c1e8c0b5256e.jpg',0,0,'f11ed5acc29b90770a453eceb7524712'),(11,'9a49edb823cc4cb799cb3438a7419a83.jpg','jpg',132,'/file/2b6249b9ba24491a9048c1e8c0b5256e.jpg',0,0,'f11ed5acc29b90770a453eceb7524712'),(12,'5a776cc21c1b407bbd2595a7af726a61.jpg','jpg',846,'/file/0e8132c00dc6484faa18b2d1487b34ec.jpg',0,0,'8f0a34a66bbc1a794b7c138897a66dad'),(13,'5a776cc21c1b407bbd2595a7af726a61.jpg','jpg',846,'/file/0e8132c00dc6484faa18b2d1487b34ec.jpg',0,0,'8f0a34a66bbc1a794b7c138897a66dad'),(14,'5a776cc21c1b407bbd2595a7af726a61.jpg','jpg',846,'/file/0e8132c00dc6484faa18b2d1487b34ec.jpg',0,0,'8f0a34a66bbc1a794b7c138897a66dad'),(15,'5a776cc21c1b407bbd2595a7af726a61.jpg','jpg',846,'/file/0e8132c00dc6484faa18b2d1487b34ec.jpg',0,0,'8f0a34a66bbc1a794b7c138897a66dad'),(16,'5a776cc21c1b407bbd2595a7af726a61.jpg','jpg',846,'/file/0e8132c00dc6484faa18b2d1487b34ec.jpg',0,0,'8f0a34a66bbc1a794b7c138897a66dad'),(17,'5a776cc21c1b407bbd2595a7af726a61.jpg','jpg',846,'/file/0e8132c00dc6484faa18b2d1487b34ec.jpg',0,0,'8f0a34a66bbc1a794b7c138897a66dad'),(18,'02.jpg','jpg',33,'/file/84ad8a9829424254811ce2220edc2d3b.jpg',0,0,'fcf09e93c497c75cf2b3656f80f997cc'),(19,'01.jpg','jpg',26,'/file/cef757d124ec4b169cffd65de5e3c47c.jpg',0,0,'d5b6bb3b068c1980d77c59079248a4a4'),(20,'03.jpg','jpg',22,'/file/7791be8ea1ee4aa0a149ae8e75c857d6.jpg',0,0,'75e8b3e8790e514fb799857f636a1623'),(21,'04.jpg','jpg',25,'/file/867aaf026b684b1e8b1a10c87e31df7e.jpg',0,0,'1d7397d5a4ce0995f711a1484d593f44'),(22,'05.jpg','jpg',19,'/file/f9f26a01e13d4ba68d7f7bd12df282e0.jpg',0,0,'b2d243af2652abf08a491074c8f099ea'),(23,'06.jpg','jpg',11,'/file/286aa7816325455b8cdcd522aca833fe.jpg',0,0,'c160a645c3dacb58ffb123a4239dcb50'),(24,'037c5b1f3e40406893b423563c557a91.jpg','jpg',1641,'/file/09bb6edab07a4c68a44cce41a3300d97.jpg',0,0,'067143803d2f87dcb939de5d4ace2bbb'),(25,'01.jpg','jpg',329,'/file/15cb9fc604984dfa97e0e968eb1d196d.jpg',0,0,'8c78b307ff66fbc7db624da25138f480'),(26,'02.jpg','jpg',738,'/file/e2cf8486c2384b8296972a550bf7e934.jpg',0,0,'7db1f7335529ad2a68367d29d0441695'),(27,'04.jpg','jpg',158,'/file/0afa4eb1c51943808f6e83cd9ced25e8.jpg',0,0,'0bfaaafc7ca1a9a5478baa8c9cae492c'),(28,'05 (1).jpg','jpg',773,'/file/8dc5354c7332454796c614bb4a0572fb.jpg',0,0,'925882b34e70434888ee7ca373bae52c'),(29,'03 (2).jpg','jpg',208,'/file/b4ac53ed62c74c298366619399c39f99.jpg',0,0,'1468738643a2f6dbd5fad1f7c80cdb00'),(30,'06.jpg','jpg',3494,'/file/be9d2d6a17c5436fb0b8c2f7927484b2.jpg',0,0,'d9950e2a7400a3d26ebde81c47e92e04'),(31,'02.png','png',5898,'/file/74488020672944968462e9e4a9c89096.png',0,0,'ad801047fc9918bd626656d08d696898'),(32,'02.png','png',71,'/file/2e2a1df657324a3293642344327310cb.png',0,0,'2c3a088b474cb2144645411f2e3da9c6'),(33,'03.jpg','jpg',31,'/file/449ab0163ba648c08cb4a76b40a9dcec.jpg',0,0,'fa95b8365bc4ec2096f4dc3f31dc8e27'),(34,'屏幕截图 2025-06-12 200753.png','png',130,'/file/7081c443758c488ea20f8fe7a1270444.png',0,0,'1428cb2dcc51bedefc36fd23ce6bac14'),(35,'屏幕截图 2025-06-12 200842.png','png',109,'/file/0ae3b65ba4bc432492100a8babe1afb5.png',0,0,'c92aff0639efd3e8accfe998172ca09e'),(36,'屏幕截图 2025-10-15 102852.png','png',218,'/file/dcdc5b72dc2e4ad9869ddf0c4d4a367d.png',0,0,'f3b5a93db8ed88c336806a3372882087'),(37,'屏幕截图 2026-04-17 201547.png','png',125,'/file/89b45f6592a5497f81c80f567471b900.png',0,0,'662dd0dfa4cc88dc84036718675b5bc3'),(38,'屏幕截图 2026-04-17 201547.png','png',125,'/file/89b45f6592a5497f81c80f567471b900.png',0,0,'662dd0dfa4cc88dc84036718675b5bc3'),(39,'屏幕截图 2026-04-17 201547.png','png',125,'/file/89b45f6592a5497f81c80f567471b900.png',0,0,'662dd0dfa4cc88dc84036718675b5bc3');
/*!40000 ALTER TABLE `sys_file` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '密码',
  `nickname` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '昵称',
  `email` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '手机号码',
  `address` varchar(1600) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '地址',
  `avatar_url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '头像链接',
  `role` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '角色',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin ROW_FORMAT=DYNAMIC COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'admin','e10adc3949ba59abbe56e057f20f883e','管理员','123@qq.com','13333333333',NULL,'/avatar/09cd5add81ff4abfbd1ccf91b2e9c820.jpg','admin'),(2,'user','e10adc3949ba59abbe56e057f20f883e','新用户','1234@qq.com','15888888888',NULL,'/avatar/978418fbe75243b4ba38da389a468b78.jpg','user'),(3,'hello','e40f01afbb1b9ae3dd6747ced5bca532','新用户',NULL,NULL,NULL,NULL,'user'),(4,'14725836912',NULL,'新用户',NULL,'14725836912',NULL,NULL,'user'),(5,'nihao','e10adc3949ba59abbe56e057f20f883e','新用户',NULL,NULL,NULL,NULL,'admin'),(6,'小明','e10adc3949ba59abbe56e057f20f883e','新用户',NULL,NULL,NULL,NULL,'user'),(7,'xiaoming','e10adc3949ba59abbe56e057f20f883e','新用户',NULL,NULL,NULL,NULL,'user');
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_order`
--

DROP TABLE IF EXISTS `t_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '订单号',
  `total_price` decimal(10,2) DEFAULT NULL COMMENT '总价',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `link_user` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '联系人',
  `link_phone` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '联系电话',
  `link_address` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '地址',
  `state` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '订单状态',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin ROW_FORMAT=DYNAMIC COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_order`
--

LOCK TABLES `t_order` WRITE;
/*!40000 ALTER TABLE `t_order` DISABLE KEYS */;
INSERT INTO `t_order` VALUES (9,'20230331223822860904',367.65,2,'张三','15888888888','新疆','已收货','2026-01-12 13:32:44'),(24,'20230805014642654151',116.00,2,'张三','15888888888','北京市','已支付','2026-01-12 13:32:44'),(25,'20250609173632896005',110.40,2,'张三','15888888888','新疆','已支付','2026-01-12 13:32:44'),(26,'20250609173741060411',116.00,2,'张三','15888888888','北京市','已支付','2026-01-12 13:32:44'),(27,'20250609195128417596',50.00,2,'张三','15888888888','北京市','已支付','2026-01-12 13:32:44'),(28,'20250609195954615607',88.20,2,'张三','15555555555','上海市','已支付','2026-01-12 13:32:44'),(29,'20250609200011355544',60.00,2,'张三','15888888888','北京市','已支付','2026-01-12 13:32:44'),(30,'20250616000258697145',19.00,2,'张三','15888888888','北京市','已支付','2026-01-12 13:32:44'),(31,'20250616142656619782',147.00,2,'张三','15888888888','北京市','已支付','2026-01-12 13:32:44'),(32,'20250912153517426723',29.00,2,'张三','15555555555','上海市','已支付','2026-01-12 13:32:44'),(33,'20250928114548081417',34.00,2,'张三','15888888888','北京市','待付款','2026-01-12 13:32:44'),(40,'20251020174128629000',2399.00,2,'张三','15888888888','上海市','待付款','2026-01-12 13:32:44'),(41,'20251020222114551635',2599.00,2,'张三','15888888888','上海市','待付款','2026-01-12 13:32:44'),(42,'20251020222500183686',2599.00,2,'张三','15888888888','上海市','待付款','2026-01-12 13:32:44'),(43,'20251020222547223769',2399.00,2,'张三','15888888888','上海市','待付款','2026-01-12 13:32:44'),(44,'20260417162720236205',34.00,5,'刘','123456789','广东省汕尾市','已支付','2026-04-17 16:27:20'),(45,'20260417172817795011',305.00,5,'刘','123456789','广东省汕尾市','待付款','2026-04-17 17:28:17'),(46,'20260417184729352596',2399.00,5,'刘','12345678912','广东汕尾陆河县河田镇','待付款','2026-04-17 18:47:29'),(47,'20260417185837642925',255.00,5,'刘','123456789','广东省汕尾市','已支付','2026-04-17 18:58:37'),(48,'20260417205354548620',186.00,7,'liu','13345678912','广东省汕尾市陆河县','已支付','2026-04-17 20:53:54'),(49,'20260417205505827252',186.00,7,'liu','13345678912','广东省汕尾市陆河县','待付款','2026-04-17 20:55:05'),(50,'20260417205714346551',185.42,7,'liu','13345678912','广东省汕尾市陆河县','已支付','2026-04-17 20:57:14'),(51,'20260417211246682878',3299.00,7,'张三','13800000000','北京市朝阳区xxx路xx号','待付款','2026-04-17 21:12:46');
/*!40000 ALTER TABLE `t_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_seckill_record`
--

DROP TABLE IF EXISTS `user_seckill_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_seckill_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `seckill_id` bigint NOT NULL COMMENT '秒杀券ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '获取时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_used` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已使用：0-未使用，1-已使用',
  `used_time` datetime DEFAULT NULL COMMENT '使用时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_seckill` (`user_id`,`seckill_id`) COMMENT '防重复获取唯一索引',
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
  KEY `idx_seckill_id` (`seckill_id`) COMMENT '秒杀券ID索引',
  KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引',
  KEY `idx_user_used` (`user_id`,`is_used`) COMMENT '用户使用状态复合索引',
  KEY `idx_seckill_used_stats` (`seckill_id`,`is_used`,`create_time`),
  CONSTRAINT `fk_user_seckill_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_seckill_voucher` FOREIGN KEY (`seckill_id`) REFERENCES `voucher` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户秒杀券记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_seckill_record`
--

LOCK TABLES `user_seckill_record` WRITE;
/*!40000 ALTER TABLE `user_seckill_record` DISABLE KEYS */;
INSERT INTO `user_seckill_record` VALUES (1,2,5001,'2024-12-15 10:30:00','2024-12-15 10:30:00',0,NULL);
/*!40000 ALTER TABLE `user_seckill_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `voucher`
--

DROP TABLE IF EXISTS `voucher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `voucher` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `voucher_amount` decimal(12,2) NOT NULL COMMENT '券面值（优惠金额）',
  `min_amount` decimal(12,2) DEFAULT '0.00' COMMENT '使用门槛（满多少可用）',
  `stock` int NOT NULL DEFAULT '0' COMMENT '库存数量',
  `begin_time` datetime NOT NULL COMMENT '秒杀开始时间',
  `end_time` datetime NOT NULL COMMENT '秒杀结束时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` int NOT NULL DEFAULT '0' COMMENT '状态：0-未开始，1-进行中，2-已结束，3-已停用',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`) COMMENT '状态索引',
  KEY `idx_begin_time` (`begin_time`) COMMENT '开始时间索引',
  KEY `idx_end_time` (`end_time`) COMMENT '结束时间索引',
  KEY `idx_active_voucher` (`status`,`begin_time`,`end_time`) COMMENT '活跃券复合索引',
  KEY `idx_stock_stats` (`stock`),
  CONSTRAINT `chk_stock` CHECK ((`stock` >= 0)),
  CONSTRAINT `chk_time` CHECK ((`end_time` > `begin_time`)),
  CONSTRAINT `chk_voucher_amount` CHECK ((`voucher_amount` > 0)),
  CONSTRAINT `chk_voucher_status` CHECK ((`status` in (0,1,2,3)))
) ENGINE=InnoDB AUTO_INCREMENT=5004 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀券表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `voucher`
--

LOCK TABLES `voucher` WRITE;
/*!40000 ALTER TABLE `voucher` DISABLE KEYS */;
INSERT INTO `voucher` VALUES (5001,100.00,200.00,950,'2024-12-15 00:00:00','2024-12-20 23:59:59','2025-09-29 16:35:26','2025-09-29 16:35:26',1),(5002,50.00,200.00,998,'2025-09-30 10:05:40','2025-09-30 11:05:40','2025-09-30 10:05:40','2026-04-17 18:29:15',1),(5003,50.00,200.00,998,'2025-09-30 10:06:52','2025-09-30 11:06:52','2025-09-30 10:06:52','2026-04-17 20:55:31',1);
/*!40000 ALTER TABLE `voucher` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'electronic_mall'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-02 19:35:53
