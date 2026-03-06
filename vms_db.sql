/*
 Navicat Premium Dump SQL

 Source Server         : spbt
 Source Server Type    : MySQL
 Source Server Version : 80027 (8.0.27)
 Source Host           : localhost:3306
 Source Schema         : vms_db

 Target Server Type    : MySQL
 Target Server Version : 80027 (8.0.27)
 File Encoding         : 65001

 Date: 05/03/2026 20:51:02
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity
-- ----------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity`  (
  `activity_id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` bigint NOT NULL COMMENT '发起队伍',
  `project_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项目编号 P开头',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `category_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务类别',
  `region_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项目属地',
  `target_audience` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务对象',
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `status` tinyint NULL DEFAULT 0 COMMENT '0待启动 1运行中 2已结项 3待审核 4拒绝',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `reject_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`activity_id`) USING BTREE,
  UNIQUE INDEX `project_code`(`project_code` ASC) USING BTREE,
  INDEX `fk_act_org`(`org_id` ASC) USING BTREE,
  CONSTRAINT `fk_act_org` FOREIGN KEY (`org_id`) REFERENCES `organization` (`org_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '志愿项目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity
-- ----------------------------

-- ----------------------------
-- Table structure for activity_position
-- ----------------------------
DROP TABLE IF EXISTS `activity_position`;
CREATE TABLE `activity_position`  (
  `pos_id` bigint NOT NULL AUTO_INCREMENT,
  `activity_id` bigint NOT NULL,
  `pos_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `plan_count` int NULL DEFAULT 0,
  `current_count` int NULL DEFAULT 0,
  PRIMARY KEY (`pos_id`) USING BTREE,
  INDEX `fk_pos_act`(`activity_id` ASC) USING BTREE,
  CONSTRAINT `fk_pos_act` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`activity_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动岗位表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity_position
-- ----------------------------

-- ----------------------------
-- Table structure for checkin_log
-- ----------------------------
DROP TABLE IF EXISTS `checkin_log`;
CREATE TABLE `checkin_log`  (
  `log_id` bigint NOT NULL AUTO_INCREMENT,
  `reg_id` bigint NOT NULL COMMENT '关联报名ID',
  `user_id` bigint NOT NULL COMMENT '志愿者ID',
  `check_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `check_type` tinyint NOT NULL COMMENT '0签到 1签退',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `fk_log_reg`(`reg_id` ASC) USING BTREE,
  CONSTRAINT `fk_log_reg` FOREIGN KEY (`reg_id`) REFERENCES `registration` (`reg_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考勤流水表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of checkin_log
-- ----------------------------

-- ----------------------------
-- Table structure for evaluation
-- ----------------------------
DROP TABLE IF EXISTS `evaluation`;
CREATE TABLE `evaluation`  (
  `eval_id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` bigint NOT NULL,
  `activity_id` bigint NOT NULL,
  `user_id` bigint NOT NULL COMMENT '评价人',
  `score_training` decimal(3, 2) NULL DEFAULT 5.00,
  `score_cooperation` decimal(3, 2) NULL DEFAULT 5.00,
  `score_execution` decimal(3, 2) NULL DEFAULT 5.00,
  `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`eval_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of evaluation
-- ----------------------------

-- ----------------------------
-- Table structure for organization
-- ----------------------------
DROP TABLE IF EXISTS `organization`;
CREATE TABLE `organization`  (
  `org_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '关联负责人账号',
  `org_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '队伍全称',
  `org_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '队伍联络编号',
  `unit_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单位类型',
  `region_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '属地编码',
  `found_date` date NULL DEFAULT NULL,
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `audit_status` tinyint NULL DEFAULT 0 COMMENT '审核状态: 0待审核 1通过 2拒绝',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `auditor_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `reject_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  PRIMARY KEY (`org_id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC) USING BTREE,
  UNIQUE INDEX `org_code`(`org_code` ASC) USING BTREE,
  CONSTRAINT `fk_org_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '志愿队伍详情表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of organization
-- ----------------------------
INSERT INTO `organization` VALUES (1, 3, '队伍01', NULL, NULL, NULL, NULL, '王一', '13598323186', '', '', 0, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for registration
-- ----------------------------
DROP TABLE IF EXISTS `registration`;
CREATE TABLE `registration`  (
  `reg_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `activity_id` bigint NOT NULL,
  `pos_id` bigint NULL DEFAULT NULL,
  `reg_status` tinyint NULL DEFAULT 0 COMMENT '0待审 1成功 2拒绝 3取消',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`reg_id`) USING BTREE,
  UNIQUE INDEX `uk_user_act`(`user_id` ASC, `activity_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报名表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of registration
-- ----------------------------

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `dict_id` int NOT NULL AUTO_INCREMENT,
  `dict_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型: service_category, unit_type, target_audience',
  `dict_key` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典键',
  `dict_value` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典值',
  PRIMARY KEY (`dict_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
INSERT INTO `sys_dict` VALUES (21, 'unit_type', 'enterprise', '企业');
INSERT INTO `sys_dict` VALUES (22, 'unit_type', 'school', '学校');
INSERT INTO `sys_dict` VALUES (23, 'unit_type', 'community', '社区');
INSERT INTO `sys_dict` VALUES (24, 'unit_type', 'government', '政府机关');
INSERT INTO `sys_dict` VALUES (25, 'unit_type', 'ngo', '社会组织');
INSERT INTO `sys_dict` VALUES (26, 'service_category', 'education', '教育服务');
INSERT INTO `sys_dict` VALUES (27, 'service_category', 'elderly', '助老服务');
INSERT INTO `sys_dict` VALUES (28, 'service_category', 'environment', '环境保护');
INSERT INTO `sys_dict` VALUES (29, 'service_category', 'charity', '公益慈善');
INSERT INTO `sys_dict` VALUES (30, 'service_category', 'culture', '文化体育');

-- ----------------------------
-- Table structure for sys_region
-- ----------------------------
DROP TABLE IF EXISTS `sys_region`;
CREATE TABLE `sys_region`  (
  `region_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区划编码',
  `region_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
  `parent_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '父级编码',
  `level` tinyint NULL DEFAULT NULL COMMENT '层级: 1省 2市 3区',
  PRIMARY KEY (`region_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '行政区划字典' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_region
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `role` tinyint NOT NULL COMMENT '角色: 0志愿者 1组织负责人 2系统管理员',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 1 COMMENT '0禁用 1正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户账号表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'zyz01', '$2a$10$0TvE1LfBKreCR07BDjhoE.OCaGitV9HTdsMmbUMG1qxqQoQD6EhYK', '李一三', 0, '13597383186', '2695375620@qq.com', NULL, 1, NULL);
INSERT INTO `sys_user` VALUES (2, 'zyz02', '$2a$10$ixf2BjcweGNbhnkz9qg9xuImlb8Rl2/PPQAMoYcASdWxVxriZSfda', '李一', 0, '13585323186', '', NULL, 1, NULL);
INSERT INTO `sys_user` VALUES (3, 'zz01', '$2a$10$g569vf6RK1XAkXAzudIRae6WaBleSIXsgMadyjePfqf1Am/bTh.0q', '王一', 1, '13598233646', '', NULL, 1, NULL);
INSERT INTO `sys_user` VALUES (4, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 2, NULL, NULL, NULL, 1, '2026-03-05 20:47:21');

-- ----------------------------
-- Table structure for volunteer_record
-- ----------------------------
DROP TABLE IF EXISTS `volunteer_record`;
CREATE TABLE `volunteer_record`  (
  `record_id` bigint NOT NULL AUTO_INCREMENT,
  `reg_id` bigint NOT NULL COMMENT '一笔报名对应一笔最终时长',
  `user_id` bigint NOT NULL,
  `activity_id` bigint NOT NULL,
  `hours` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '核定小时数',
  `points` int NULL DEFAULT 0 COMMENT '核定积分',
  `audit_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发放时间',
  `auditor_id` bigint NULL DEFAULT NULL COMMENT '审核人(负责人)',
  PRIMARY KEY (`record_id`) USING BTREE,
  UNIQUE INDEX `reg_id`(`reg_id` ASC) USING BTREE,
  CONSTRAINT `fk_rec_reg` FOREIGN KEY (`reg_id`) REFERENCES `registration` (`reg_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '志愿时长记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of volunteer_record
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
