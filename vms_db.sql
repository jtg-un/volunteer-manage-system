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

 Date: 07/03/2026 14:11:55
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
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '志愿项目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity
-- ----------------------------
INSERT INTO `activity` VALUES (1, 1, 'P202603050001', '教育服务1', 'education', '110101', '小孩儿', '2026-02-28 16:12:11', '2026-03-09 16:01:02', 2, '描述', NULL, NULL);
INSERT INTO `activity` VALUES (2, 1, 'P202603050002', '活动02', 'charity', '320508', '老人', '2026-03-16 16:09:00', '2026-03-30 16:00:00', 4, '', NULL, NULL);
INSERT INTO `activity` VALUES (3, 1, 'P202603060001', '活动03', 'charity', '310107', '儿童', '2026-03-23 16:00:00', '2026-03-30 16:00:00', 4, '描述', '原因123', NULL);
INSERT INTO `activity` VALUES (4, 1, 'P202603060002', '活动04', 'charity', '110107', '老人', '2026-03-30 16:00:00', '2026-04-15 16:00:00', 2, '', NULL, NULL);
INSERT INTO `activity` VALUES (5, 1, 'P202603060003', '1234', 'education', '3204', '123', '2026-03-07 16:00:00', '2026-03-20 16:00:00', 4, '', '原因1', NULL);
INSERT INTO `activity` VALUES (6, 1, 'P202603060004', '12345', 'charity', '310107', '', '2026-03-22 16:00:00', '2026-03-30 16:00:00', 2, '描述', NULL, NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动岗位表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity_position
-- ----------------------------
INSERT INTO `activity_position` VALUES (1, 1, '志愿者1', 2, 0);
INSERT INTO `activity_position` VALUES (2, 1, '志愿者2', 2, 0);
INSERT INTO `activity_position` VALUES (3, 2, '岗位1', 1, 0);
INSERT INTO `activity_position` VALUES (4, 3, '岗位01', 1, 0);
INSERT INTO `activity_position` VALUES (5, 4, '岗位9', 1, 0);
INSERT INTO `activity_position` VALUES (6, 5, '123', 1, 0);
INSERT INTO `activity_position` VALUES (7, 6, '1234', 1, 0);

-- ----------------------------
-- Table structure for checkin_log
-- ----------------------------
DROP TABLE IF EXISTS `checkin_log`;
CREATE TABLE `checkin_log`  (
  `log_id` bigint NOT NULL AUTO_INCREMENT,
  `reg_id` bigint NOT NULL COMMENT '关联报名ID',
  `check_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `check_type` tinyint NOT NULL COMMENT '0签到 1签退',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `fk_log_reg`(`reg_id` ASC) USING BTREE,
  CONSTRAINT `fk_log_reg` FOREIGN KEY (`reg_id`) REFERENCES `registration` (`reg_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考勤流水表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of checkin_log
-- ----------------------------

-- ----------------------------
-- Table structure for evaluation
-- ----------------------------
DROP TABLE IF EXISTS `evaluation`;
CREATE TABLE `evaluation`  (
  `eval_id` bigint NOT NULL AUTO_INCREMENT,
  `reg_id` bigint NOT NULL COMMENT '关联报名ID',
  `score_training` decimal(3, 2) NULL DEFAULT 5.00,
  `score_cooperation` decimal(3, 2) NULL DEFAULT 5.00,
  `score_execution` decimal(3, 2) NULL DEFAULT 5.00,
  `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`eval_id`) USING BTREE,
  INDEX `fk_eval_reg`(`reg_id` ASC) USING BTREE,
  CONSTRAINT `fk_eval_reg` FOREIGN KEY (`reg_id`) REFERENCES `registration` (`reg_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '志愿队伍详情表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of organization
-- ----------------------------
INSERT INTO `organization` VALUES (1, 3, '队伍01', NULL, '', NULL, '2026-03-02', '王一', '13598323186', '河南省', '', 1, '2026-03-05 22:00:54', 5, NULL);
INSERT INTO `organization` VALUES (2, 6, '队伍02', NULL, NULL, NULL, NULL, '李二', '15568923189', '选填', '选填', 1, '2026-03-05 22:10:51', 5, NULL);
INSERT INTO `organization` VALUES (3, 7, '组织03', NULL, NULL, NULL, NULL, '张三', '13058397590', '地址', '简介', 2, '2026-03-05 22:11:54', 5, '没有填写地址');
INSERT INTO `organization` VALUES (4, 9, '组织04', NULL, NULL, NULL, NULL, '组织4', '13498756478', '地址', '简介', 1, '2026-03-05 22:48:50', 5, NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报名表' ROW_FORMAT = Dynamic;

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
-- Table structure for sys_file
-- ----------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file`  (
  `file_id` bigint NOT NULL AUTO_INCREMENT,
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件存储路径',
  `file_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '后缀名',
  `assoc_id` bigint NULL DEFAULT NULL COMMENT '关联业务ID(如活动ID)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`file_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_file
-- ----------------------------

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `notice_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告内容',
  `type` tinyint NULL DEFAULT 0 COMMENT '类型: 0通知 1公告',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发布人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------

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
INSERT INTO `sys_region` VALUES ('11', '北京市', NULL, 1);
INSERT INTO `sys_region` VALUES ('1101', '市辖区', '11', 2);
INSERT INTO `sys_region` VALUES ('110101', '东城区', '1101', 3);
INSERT INTO `sys_region` VALUES ('110102', '西城区', '1101', 3);
INSERT INTO `sys_region` VALUES ('110105', '朝阳区', '1101', 3);
INSERT INTO `sys_region` VALUES ('110106', '丰台区', '1101', 3);
INSERT INTO `sys_region` VALUES ('110107', '石景山区', '1101', 3);
INSERT INTO `sys_region` VALUES ('110108', '海淀区', '1101', 3);
INSERT INTO `sys_region` VALUES ('110109', '门头沟区', '1101', 3);
INSERT INTO `sys_region` VALUES ('110111', '房山区', '1101', 3);
INSERT INTO `sys_region` VALUES ('110112', '通州区', '1101', 3);
INSERT INTO `sys_region` VALUES ('110113', '顺义区', '1101', 3);
INSERT INTO `sys_region` VALUES ('110114', '昌平区', '1101', 3);
INSERT INTO `sys_region` VALUES ('110115', '大兴区', '1101', 3);
INSERT INTO `sys_region` VALUES ('110116', '怀柔区', '1101', 3);
INSERT INTO `sys_region` VALUES ('110117', '平谷区', '1101', 3);
INSERT INTO `sys_region` VALUES ('110118', '密云区', '1101', 3);
INSERT INTO `sys_region` VALUES ('110119', '延庆区', '1101', 3);
INSERT INTO `sys_region` VALUES ('12', '天津市', NULL, 1);
INSERT INTO `sys_region` VALUES ('13', '河北省', NULL, 1);
INSERT INTO `sys_region` VALUES ('31', '上海市', NULL, 1);
INSERT INTO `sys_region` VALUES ('3101', '市辖区', '31', 2);
INSERT INTO `sys_region` VALUES ('310101', '黄浦区', '3101', 3);
INSERT INTO `sys_region` VALUES ('310104', '徐汇区', '3101', 3);
INSERT INTO `sys_region` VALUES ('310105', '长宁区', '3101', 3);
INSERT INTO `sys_region` VALUES ('310106', '静安区', '3101', 3);
INSERT INTO `sys_region` VALUES ('310107', '普陀区', '3101', 3);
INSERT INTO `sys_region` VALUES ('310109', '虹口区', '3101', 3);
INSERT INTO `sys_region` VALUES ('310110', '杨浦区', '3101', 3);
INSERT INTO `sys_region` VALUES ('310112', '闵行区', '3101', 3);
INSERT INTO `sys_region` VALUES ('310113', '宝山区', '3101', 3);
INSERT INTO `sys_region` VALUES ('310114', '嘉定区', '3101', 3);
INSERT INTO `sys_region` VALUES ('310115', '浦东新区', '3101', 3);
INSERT INTO `sys_region` VALUES ('310116', '金山区', '3101', 3);
INSERT INTO `sys_region` VALUES ('310117', '松江区', '3101', 3);
INSERT INTO `sys_region` VALUES ('310118', '青浦区', '3101', 3);
INSERT INTO `sys_region` VALUES ('310120', '奉贤区', '3101', 3);
INSERT INTO `sys_region` VALUES ('310151', '崇明区', '3101', 3);
INSERT INTO `sys_region` VALUES ('32', '江苏省', NULL, 1);
INSERT INTO `sys_region` VALUES ('3201', '南京市', '32', 2);
INSERT INTO `sys_region` VALUES ('320102', '玄武区', '3201', 3);
INSERT INTO `sys_region` VALUES ('320104', '秦淮区', '3201', 3);
INSERT INTO `sys_region` VALUES ('320105', '建邺区', '3201', 3);
INSERT INTO `sys_region` VALUES ('320106', '鼓楼区', '3201', 3);
INSERT INTO `sys_region` VALUES ('320111', '浦口区', '3201', 3);
INSERT INTO `sys_region` VALUES ('320113', '栖霞区', '3201', 3);
INSERT INTO `sys_region` VALUES ('320114', '雨花台区', '3201', 3);
INSERT INTO `sys_region` VALUES ('320115', '江宁区', '3201', 3);
INSERT INTO `sys_region` VALUES ('320116', '六合区', '3201', 3);
INSERT INTO `sys_region` VALUES ('320117', '溧水区', '3201', 3);
INSERT INTO `sys_region` VALUES ('320118', '高淳区', '3201', 3);
INSERT INTO `sys_region` VALUES ('3202', '无锡市', '32', 2);
INSERT INTO `sys_region` VALUES ('3203', '徐州市', '32', 2);
INSERT INTO `sys_region` VALUES ('3204', '常州市', '32', 2);
INSERT INTO `sys_region` VALUES ('3205', '苏州市', '32', 2);
INSERT INTO `sys_region` VALUES ('320505', '虎丘区', '3205', 3);
INSERT INTO `sys_region` VALUES ('320506', '吴中区', '3205', 3);
INSERT INTO `sys_region` VALUES ('320507', '相城区', '3205', 3);
INSERT INTO `sys_region` VALUES ('320508', '姑苏区', '3205', 3);
INSERT INTO `sys_region` VALUES ('320509', '吴江区', '3205', 3);
INSERT INTO `sys_region` VALUES ('320576', '苏州工业园区', '3205', 3);
INSERT INTO `sys_region` VALUES ('320581', '常熟市', '3205', 3);
INSERT INTO `sys_region` VALUES ('320582', '张家港市', '3205', 3);
INSERT INTO `sys_region` VALUES ('320583', '昆山市', '3205', 3);
INSERT INTO `sys_region` VALUES ('320585', '太仓市', '3205', 3);
INSERT INTO `sys_region` VALUES ('3206', '南通市', '32', 2);
INSERT INTO `sys_region` VALUES ('3207', '连云港市', '32', 2);
INSERT INTO `sys_region` VALUES ('3208', '淮安市', '32', 2);
INSERT INTO `sys_region` VALUES ('3209', '盐城市', '32', 2);
INSERT INTO `sys_region` VALUES ('3210', '扬州市', '32', 2);
INSERT INTO `sys_region` VALUES ('3211', '镇江市', '32', 2);
INSERT INTO `sys_region` VALUES ('3212', '泰州市', '32', 2);
INSERT INTO `sys_region` VALUES ('3213', '宿迁市', '32', 2);
INSERT INTO `sys_region` VALUES ('33', '浙江省', NULL, 1);
INSERT INTO `sys_region` VALUES ('34', '安徽省', NULL, 1);
INSERT INTO `sys_region` VALUES ('35', '福建省', NULL, 1);
INSERT INTO `sys_region` VALUES ('36', '江西省', NULL, 1);
INSERT INTO `sys_region` VALUES ('37', '山东省', NULL, 1);
INSERT INTO `sys_region` VALUES ('41', '河南省', NULL, 1);
INSERT INTO `sys_region` VALUES ('42', '湖北省', NULL, 1);
INSERT INTO `sys_region` VALUES ('43', '湖南省', NULL, 1);
INSERT INTO `sys_region` VALUES ('44', '广东省', NULL, 1);
INSERT INTO `sys_region` VALUES ('4401', '广州市', '44', 2);
INSERT INTO `sys_region` VALUES ('440103', '荔湾区', '4401', 3);
INSERT INTO `sys_region` VALUES ('440104', '越秀区', '4401', 3);
INSERT INTO `sys_region` VALUES ('440105', '海珠区', '4401', 3);
INSERT INTO `sys_region` VALUES ('440106', '天河区', '4401', 3);
INSERT INTO `sys_region` VALUES ('440111', '白云区', '4401', 3);
INSERT INTO `sys_region` VALUES ('440112', '黄埔区', '4401', 3);
INSERT INTO `sys_region` VALUES ('440113', '番禺区', '4401', 3);
INSERT INTO `sys_region` VALUES ('440114', '花都区', '4401', 3);
INSERT INTO `sys_region` VALUES ('440115', '南沙区', '4401', 3);
INSERT INTO `sys_region` VALUES ('440117', '从化区', '4401', 3);
INSERT INTO `sys_region` VALUES ('440118', '增城区', '4401', 3);
INSERT INTO `sys_region` VALUES ('4402', '韶关市', '44', 2);
INSERT INTO `sys_region` VALUES ('4403', '深圳市', '44', 2);
INSERT INTO `sys_region` VALUES ('440303', '罗湖区', '4403', 3);
INSERT INTO `sys_region` VALUES ('440304', '福田区', '4403', 3);
INSERT INTO `sys_region` VALUES ('440305', '南山区', '4403', 3);
INSERT INTO `sys_region` VALUES ('440306', '宝安区', '4403', 3);
INSERT INTO `sys_region` VALUES ('440307', '龙岗区', '4403', 3);
INSERT INTO `sys_region` VALUES ('440308', '盐田区', '4403', 3);
INSERT INTO `sys_region` VALUES ('440309', '龙华区', '4403', 3);
INSERT INTO `sys_region` VALUES ('440310', '坪山区', '4403', 3);
INSERT INTO `sys_region` VALUES ('440311', '光明区', '4403', 3);
INSERT INTO `sys_region` VALUES ('4404', '珠海市', '44', 2);
INSERT INTO `sys_region` VALUES ('4405', '汕头市', '44', 2);
INSERT INTO `sys_region` VALUES ('4406', '佛山市', '44', 2);
INSERT INTO `sys_region` VALUES ('4407', '江门市', '44', 2);
INSERT INTO `sys_region` VALUES ('4408', '湛江市', '44', 2);
INSERT INTO `sys_region` VALUES ('4409', '茂名市', '44', 2);
INSERT INTO `sys_region` VALUES ('4412', '肇庆市', '44', 2);
INSERT INTO `sys_region` VALUES ('4413', '惠州市', '44', 2);
INSERT INTO `sys_region` VALUES ('4414', '梅州市', '44', 2);
INSERT INTO `sys_region` VALUES ('4415', '汕尾市', '44', 2);
INSERT INTO `sys_region` VALUES ('4416', '河源市', '44', 2);
INSERT INTO `sys_region` VALUES ('4417', '阳江市', '44', 2);
INSERT INTO `sys_region` VALUES ('4418', '清远市', '44', 2);
INSERT INTO `sys_region` VALUES ('4419', '东莞市', '44', 2);
INSERT INTO `sys_region` VALUES ('4420', '中山市', '44', 2);
INSERT INTO `sys_region` VALUES ('4451', '潮州市', '44', 2);
INSERT INTO `sys_region` VALUES ('4452', '揭阳市', '44', 2);
INSERT INTO `sys_region` VALUES ('4453', '云浮市', '44', 2);
INSERT INTO `sys_region` VALUES ('45', '广西壮族自治区', NULL, 1);
INSERT INTO `sys_region` VALUES ('46', '海南省', NULL, 1);
INSERT INTO `sys_region` VALUES ('50', '重庆市', NULL, 1);
INSERT INTO `sys_region` VALUES ('51', '四川省', NULL, 1);
INSERT INTO `sys_region` VALUES ('52', '贵州省', NULL, 1);
INSERT INTO `sys_region` VALUES ('53', '云南省', NULL, 1);
INSERT INTO `sys_region` VALUES ('54', '西藏自治区', NULL, 1);
INSERT INTO `sys_region` VALUES ('61', '陕西省', NULL, 1);
INSERT INTO `sys_region` VALUES ('62', '甘肃省', NULL, 1);
INSERT INTO `sys_region` VALUES ('63', '青海省', NULL, 1);
INSERT INTO `sys_region` VALUES ('64', '宁夏回族自治区', NULL, 1);
INSERT INTO `sys_region` VALUES ('65', '新疆维吾尔自治区', NULL, 1);

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
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户账号表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'zyz01', '$2a$10$0TvE1LfBKreCR07BDjhoE.OCaGitV9HTdsMmbUMG1qxqQoQD6EhYK', '李子硕', 0, '13598323186', '2695375620@qq.com', NULL, 1, NULL);
INSERT INTO `sys_user` VALUES (2, 'zyz02', '$2a$10$ixf2BjcweGNbhnkz9qg9xuImlb8Rl2/PPQAMoYcASdWxVxriZSfda', '李一', 0, '13589323186', '', NULL, 1, NULL);
INSERT INTO `sys_user` VALUES (3, 'zz01', '$2a$10$vUotgDu.WwLkoqccQ8aBYeZfBcvvRldyQRI8msgZbgpx16WMSHAse', '王一', 1, '13598323186', '', '/uploads/avatar/2026/03/3_1772862387120.png', 1, NULL);
INSERT INTO `sys_user` VALUES (4, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 2, NULL, NULL, NULL, 1, '2026-03-05 20:47:21');
INSERT INTO `sys_user` VALUES (5, 'admin01', '$2a$10$WQTaC/yTzw9zZBE84MqTJOp7FhVfrIxsjt2kXfxNvARPlr2G0GrVq', '系统管理员01', 2, '13598323185', '', NULL, 1, NULL);
INSERT INTO `sys_user` VALUES (6, 'zz02', '$2a$10$DojhG8ZBfCPjxuByp0r1OOTRhKNmG7Z68873jJQfP5jl47mOxBTrm', '李二', 1, '15568923189', '', NULL, 1, NULL);
INSERT INTO `sys_user` VALUES (7, 'zz03', '$2a$10$V19g2xRP6/MDGkWd3Yu/NOlU52OIulVEUK2xSKpOA6KPaiArfBBQO', '张三', 1, '13058397590', '2301@qq.com', NULL, 0, NULL);
INSERT INTO `sys_user` VALUES (8, 'zyz03', '$2a$10$ltKxnoDRTJiVLqo2ECEgueHMUABZrGWfwryH2RmiIZXv4S.m0xVS.', '志愿者三', 0, '13856783456', '', NULL, 1, NULL);
INSERT INTO `sys_user` VALUES (9, 'zz04', '$2a$10$IlrICNSciBevMt..rHBr5eqG3pEBVA9KXAA3qA.NoZHkl7r8ZY4HK', '组织4', 1, '13498756478', '', NULL, 1, NULL);
INSERT INTO `sys_user` VALUES (10, 'zyz08', '$2a$10$3hJrl2V1bPn4B0XT3APP9ek4/ccihXjMc9Lt.g..r2QH6ViQ10ApG', '王八', 0, '13020305069', '', NULL, 1, NULL);

-- ----------------------------
-- Table structure for volunteer_record
-- ----------------------------
DROP TABLE IF EXISTS `volunteer_record`;
CREATE TABLE `volunteer_record`  (
  `record_id` bigint NOT NULL AUTO_INCREMENT,
  `reg_id` bigint NOT NULL COMMENT '一笔报名对应一笔最终时长',
  `hours` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '核定小时数',
  `points` int NULL DEFAULT 0 COMMENT '核定积分',
  `audit_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发放时间',
  `auditor_id` bigint NULL DEFAULT NULL COMMENT '审核人(负责人)',
  PRIMARY KEY (`record_id`) USING BTREE,
  UNIQUE INDEX `reg_id`(`reg_id` ASC) USING BTREE,
  CONSTRAINT `fk_rec_reg` FOREIGN KEY (`reg_id`) REFERENCES `registration` (`reg_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '志愿时长记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of volunteer_record
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
