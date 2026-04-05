-- ============================================
-- VMS 志愿活动管理系统 - 测试数据初始化脚本
-- 执行前请先备份数据库
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. 修改表结构（扩展字段长度）
-- ============================================

ALTER TABLE `activity` MODIFY COLUMN `category_id` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务类别';

-- ============================================
-- 2. 清理现有测试数据（保留字典和行政区划）
-- ============================================

DELETE FROM `evaluation` WHERE 1=1;
DELETE FROM `volunteer_record` WHERE 1=1;
DELETE FROM `checkin_log` WHERE 1=1;
DELETE FROM `registration` WHERE 1=1;
DELETE FROM `activity_position` WHERE 1=1;
DELETE FROM `activity` WHERE 1=1;
DELETE FROM `organization` WHERE 1=1;
DELETE FROM `sys_user` WHERE 1=1;

-- ============================================
-- 3. 用户账号数据
-- 密码统一为: 123456
-- BCrypt加密值: $2a$10$WQTaC/yTzw9zZBE84MqTJOp7FhVfrIxsjt2kXfxNvARPlr2G0GrVq
-- ============================================

INSERT INTO `sys_user` (`user_id`, `username`, `password`, `real_name`, `role`, `phone`, `email`, `avatar_url`, `status`) VALUES
-- 志愿者账号 (role=0)
(1, 'zyz01', '$2a$10$WQTaC/yTzw9zZBE84MqTJOp7FhVfrIxsjt2kXfxNvARPlr2G0GrVq', '张志远', 0, '13800138001', 'zyz01@test.com', NULL, 1),
(2, 'zyz02', '$2a$10$WQTaC/yTzw9zZBE84MqTJOp7FhVfrIxsjt2kXfxNvARPlr2G0GrVq', '李明华', 0, '13800138002', 'zyz02@test.com', NULL, 1),
(3, 'zyz03', '$2a$10$WQTaC/yTzw9zZBE84MqTJOp7FhVfrIxsjt2kXfxNvARPlr2G0GrVq', '王小芳', 0, '13800138003', 'zyz03@test.com', NULL, 1),

-- 组织负责人账号 (role=1)
(10, 'zz01', '$2a$10$WQTaC/yTzw9zZBE84MqTJOp7FhVfrIxsjt2kXfxNvARPlr2G0GrVq', '赵队长', 1, '13900139001', 'zz01@test.com', NULL, 1),
(11, 'zz02', '$2a$10$WQTaC/yTzw9zZBE84MqTJOp7FhVfrIxsjt2kXfxNvARPlr2G0GrVq', '钱主任', 1, '13900139002', 'zz02@test.com', NULL, 1),
(12, 'zz03', '$2a$10$WQTaC/yTzw9zZBE84MqTJOp7FhVfrIxsjt2kXfxNvARPlr2G0GrVq', '孙经理', 1, '13900139003', 'zz03@test.com', NULL, 1),

-- 系统管理员账号 (role=2)
(20, 'admin', '$2a$10$WQTaC/yTzw9zZBE84MqTJOp7FhVfrIxsjt2kXfxNvARPlr2G0GrVq', '系统管理员', 2, '13700137001', 'admin@test.com', NULL, 1),
(21, 'admin01', '$2a$10$WQTaC/yTzw9zZBE84MqTJOp7FhVfrIxsjt2kXfxNvARPlr2G0GrVq', '管理员01', 2, '13700137002', 'admin01@test.com', NULL, 1);

-- ============================================
-- 4. 组织信息数据
-- 审核状态: 0待审核 1通过 2拒绝
-- ============================================

INSERT INTO `organization` (`org_id`, `user_id`, `org_name`, `org_code`, `unit_type`, `region_code`, `found_date`, `contact_person`, `contact_phone`, `address`, `intro`, `audit_status`, `audit_time`, `auditor_id`, `reject_reason`) VALUES
-- zz01的组织 - 已审核通过
(1, 10, '阳光志愿服务总队', 'ORG001', 'community', '3205', '2023-05-15', '赵队长', '13900139001', '江苏省苏州市姑苏区人民路100号', '致力于社区公益服务的志愿组织，服务范围包括助老、助学、环保等多个领域。', 1, NOW(), 20, NULL),

-- zz02的组织 - 已审核通过
(2, 11, '爱心公益联盟', 'ORG002', 'ngo', '1101', '2022-08-20', '钱主任', '13900139002', '北京市朝阳区建国路88号', '专业的社会公益组织，长期开展扶贫帮困、教育支持等志愿活动。', 1, NOW(), 20, NULL),

-- zz03的组织 - 审核未通过（用于测试审核拒绝场景）
(3, 12, '待审志愿服务队', 'ORG003', 'enterprise', '3101', '2024-01-10', '孙经理', '13900139003', '上海市浦东新区陆家嘴', '企业志愿服务团队申请。', 2, NOW(), 20, '组织资质材料不完整，请补充营业执照副本和志愿服务计划书。');

-- ============================================
-- 5. 活动数据
-- 状态: 0待启动 1运行中 2已结项 3待审核 4拒绝
-- ============================================

INSERT INTO `activity` (`activity_id`, `org_id`, `project_code`, `title`, `category_id`, `region_code`, `target_audience`, `start_time`, `end_time`, `status`, `description`, `reject_reason`, `create_time`) VALUES
-- 活动1: 已结项 (status=2) - 用于测试完整流程
(1, 1, 'P202603180001', '社区环保清洁行动', 'environment', '320508', '社区居民', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 2, '组织志愿者对社区公共区域进行清洁，包括街道清扫、绿化带维护等。', NULL, DATE_SUB(NOW(), INTERVAL 15 DAY)),

-- 活动2: 运行中 (status=1) - 用于测试报名、签到
(2, 1, 'P202603180002', '关爱老人送温暖活动', 'elderly', '320506', '独居老人', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1, '为社区独居老人提供生活帮助和心理慰藉服务。', NULL, DATE_SUB(NOW(), INTERVAL 10 DAY)),

-- 活动3: 待启动 (status=0) - 用于测试启动、编辑
(3, 1, 'P202603180003', '青少年课后辅导计划', 'education', '320505', '中小学生', DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 0, '为社区中小学生提供免费的课后作业辅导和学习指导。', NULL, DATE_SUB(NOW(), INTERVAL 5 DAY)),

-- 活动4: 待审核 (status=3) - 用于测试管理员审核
(4, 1, 'P202603180004', '公益慈善义卖活动', 'charity', '320508', '社区居民', DATE_ADD(NOW(), INTERVAL 15 DAY), DATE_ADD(NOW(), INTERVAL 16 DAY), 3, '组织慈善义卖，所得款项用于帮扶困难家庭。', NULL, NOW()),

-- 活动5: 已拒绝 (status=4) - 用于测试拒绝场景
(5, 1, 'P202603180005', '文化体育运动会', 'culture', '320507', '社区居民', DATE_ADD(NOW(), INTERVAL 20 DAY), DATE_ADD(NOW(), INTERVAL 21 DAY), 4, '组织社区趣味运动会。', '活动方案不够详细，请补充具体的活动流程、安全保障措施和应急预案。', NOW()),

-- 活动6: 已结项 (status=2) - zz02组织的活动
(6, 2, 'P202603180006', '城市绿化植树活动', 'environment', '110105', '市民群众', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY), 2, '组织志愿者参与城市绿化植树活动。', NULL, DATE_SUB(NOW(), INTERVAL 30 DAY)),

-- 活动7: 运行中 (status=1) - zz02组织的活动
(7, 2, 'P202603180007', '助老服务日活动', 'elderly', '110106', '老年群体', NOW(), DATE_ADD(NOW(), INTERVAL 5 DAY), 1, '为养老院老人提供陪伴服务。', NULL, DATE_SUB(NOW(), INTERVAL 7 DAY));

-- ============================================
-- 6. 活动岗位数据
-- ============================================

INSERT INTO `activity_position` (`pos_id`, `activity_id`, `pos_name`, `plan_count`, `current_count`) VALUES
-- 活动1的岗位 (已结项)
(1, 1, '清洁志愿者', 10, 5),
(2, 1, '宣传志愿者', 5, 3),

-- 活动2的岗位 (运行中) - 用于报名测试
(3, 2, '陪伴志愿者', 8, 2),
(4, 2, '物资管理', 3, 1),

-- 活动3的岗位 (待启动) - 用于编辑测试
(5, 3, '语文辅导', 5, 0),
(6, 3, '数学辅导', 5, 0),
(7, 3, '英语辅导', 5, 0),

-- 活动4的岗位 (待审核)
(8, 4, '义卖志愿者', 20, 0),
(9, 4, '场地布置', 10, 0),

-- 活动5的岗位 (已拒绝)
(10, 5, '运动员引导', 15, 0),

-- 活动6的岗位 (已结项)
(11, 6, '植树志愿者', 30, 20),
(12, 6, '后勤保障', 10, 8),

-- 活动7的岗位 (运行中)
(13, 7, '服务志愿者', 15, 5);

-- ============================================
-- 7. 报名数据
-- 状态: 0待审核 1已通过 2已拒绝 3已取消
-- ============================================

INSERT INTO `registration` (`reg_id`, `user_id`, `activity_id`, `pos_id`, `reg_status`, `create_time`) VALUES
-- 活动1的报名 (已结项活动)
(1, 1, 1, 1, 1, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(2, 2, 1, 1, 1, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(3, 3, 1, 2, 1, DATE_SUB(NOW(), INTERVAL 11 DAY)),

-- 活动2的报名 (运行中活动) - 用于测试签到签退
(4, 1, 2, 3, 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),  -- 已通过，可签到
(5, 2, 2, 3, 0, DATE_SUB(NOW(), INTERVAL 3 DAY)),  -- 待审核
(6, 3, 2, 4, 1, DATE_SUB(NOW(), INTERVAL 4 DAY)),  -- 已通过

-- 活动3的报名 (待启动活动)
(7, 1, 3, 5, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(8, 2, 3, 6, 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- 活动6的报名 (已结项)
(9, 1, 6, 11, 1, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(10, 2, 6, 11, 1, DATE_SUB(NOW(), INTERVAL 25 DAY)),

-- 活动7的报名 (运行中)
(11, 3, 7, 13, 1, DATE_SUB(NOW(), INTERVAL 3 DAY));

-- ============================================
-- 8. 签到签退数据
-- ============================================

INSERT INTO `checkin_log` (`log_id`, `reg_id`, `check_time`, `check_type`) VALUES
-- 活动1的签到签退 (已结项)
(1, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), 0),  -- 签到
(2, 1, DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 4 HOUR, 1),  -- 签退
(3, 2, DATE_SUB(NOW(), INTERVAL 10 DAY), 0),
(4, 2, DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 3 HOUR, 1),
(5, 3, DATE_SUB(NOW(), INTERVAL 10 DAY), 0),
(6, 3, DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 5 HOUR, 1),

-- 活动2的签到签退 (运行中)
(7, 4, NOW() - INTERVAL 2 HOUR, 0),  -- zyz01已签到，未签退

-- 活动6的签到签退 (已结项)
(8, 9, DATE_SUB(NOW(), INTERVAL 20 DAY), 0),
(9, 9, DATE_SUB(NOW(), INTERVAL 20 DAY) + INTERVAL 6 HOUR, 1),
(10, 10, DATE_SUB(NOW(), INTERVAL 20 DAY), 0),
(11, 10, DATE_SUB(NOW(), INTERVAL 20 DAY) + INTERVAL 5 HOUR, 1),

-- 活动7的签到签退 (运行中)
(12, 11, NOW() - INTERVAL 1 HOUR, 0);  -- zyz03已签到

-- ============================================
-- 9. 志愿时长记录
-- ============================================

INSERT INTO `volunteer_record` (`record_id`, `reg_id`, `hours`, `points`, `audit_time`, `auditor_id`) VALUES
-- 活动1的时长记录
(1, 1, 4.00, 40, DATE_SUB(NOW(), INTERVAL 5 DAY), 10),
(2, 2, 3.00, 30, DATE_SUB(NOW(), INTERVAL 5 DAY), 10),
(3, 3, 5.00, 50, DATE_SUB(NOW(), INTERVAL 5 DAY), 10),

-- 活动6的时长记录
(4, 9, 6.00, 60, DATE_SUB(NOW(), INTERVAL 19 DAY), 11),
(5, 10, 5.00, 50, DATE_SUB(NOW(), INTERVAL 19 DAY), 11);

-- ============================================
-- 10. 评价数据
-- ============================================

INSERT INTO `evaluation` (`eval_id`, `reg_id`, `score_training`, `score_cooperation`, `score_execution`, `comment`, `create_time`) VALUES
-- 活动1的评价
(1, 1, 4.50, 5.00, 4.50, '服务态度认真负责，配合度高，能够很好地完成分配的任务。', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 2, 4.00, 4.50, 4.00, '工作积极主动，具有良好的团队协作精神。', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 3, 5.00, 5.00, 5.00, '表现出色，宣传文案写得很好，超出预期。', DATE_SUB(NOW(), INTERVAL 5 DAY)),

-- 活动6的评价
(4, 9, 4.50, 4.50, 5.00, '植树过程中表现出很强的责任心，不怕苦不怕累。', DATE_SUB(NOW(), INTERVAL 19 DAY)),
(5, 10, 4.00, 4.00, 4.50, '积极参与各项工作，值得肯定。', DATE_SUB(NOW(), INTERVAL 19 DAY));

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 数据验证查询
-- ============================================

SELECT '=== 用户账号统计 ===' AS info;
SELECT role, COUNT(*) as count,
       CASE role WHEN 0 THEN '志愿者' WHEN 1 THEN '组织负责人' WHEN 2 THEN '管理员' END as role_name
FROM sys_user GROUP BY role;

SELECT '=== 组织统计 ===' AS info;
SELECT audit_status, COUNT(*) as count,
       CASE audit_status WHEN 0 THEN '待审核' WHEN 1 THEN '已通过' WHEN 2 THEN '已拒绝' END as status_name
FROM organization GROUP BY audit_status;

SELECT '=== 活动统计 ===' AS info;
SELECT status, COUNT(*) as count,
       CASE status WHEN 0 THEN '待启动' WHEN 1 THEN '运行中' WHEN 2 THEN '已结项' WHEN 3 THEN '待审核' WHEN 4 THEN '已拒绝' END as status_name
FROM activity GROUP BY status;

SELECT '=== 报名统计 ===' AS info;
SELECT reg_status, COUNT(*) as count,
       CASE reg_status WHEN 0 THEN '待审核' WHEN 1 THEN '已通过' WHEN 2 THEN '已拒绝' WHEN 3 THEN '已取消' END as status_name
FROM registration GROUP BY reg_status;

SELECT '=== 签到记录 ===' AS info;
SELECT COUNT(*) as checkin_count FROM checkin_log;

SELECT '=== 时长记录 ===' AS info;
SELECT COUNT(*) as record_count, SUM(hours) as total_hours, SUM(points) as total_points FROM volunteer_record;

SELECT '=== 评价记录 ===' AS info;
SELECT COUNT(*) as eval_count FROM evaluation;