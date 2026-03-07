# VMS 志愿活动管理系统

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4.21-brightgreen)](https://vuejs.org/)
[![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5.5-brightgreen)](https://baomidou.com/)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

## 📖 项目简介

VMS（Volunteer Management System）是一个基于 Spring Boot 3 + Vue 3 的分布式志愿活动管理系统。系统采用微服务架构，实现了志愿者、志愿组织、系统管理员多角色的完整业务流程，涵盖组织审核、活动发布与管理、报名统计等功能。

### 🎯 核心功能

- **多角色支持**：志愿者、组织负责人、系统管理员
- **组织管理**：注册申请、资质审核、信息维护
- **活动管理**：活动发布、岗位设置、状态流转（待审核/待启动/运行中/已结项/已拒绝）
- **审核流程**：组织审核、活动审核
- **个人中心**：信息修改、密码修改、头像上传

## 🏗️ 技术架构

### 后端技术栈

| 组件 | 技术 | 说明 |
|------|------|------|
| 核心框架 | Spring Boot 3.2.3 | 基础框架 |
| 安全框架 | Spring Security | 认证授权 |
| 持久层 | MyBatis Plus 3.5.5 | ORM 框架 |
| 数据库 | MySQL 8.0 | 关系型数据库 |
| JWT | JJWT 0.11.5 | Token 认证 |
| 项目管理 | Maven | 依赖管理 |

### 前端技术栈

| 组件 | 技术 | 说明 |
|------|------|------|
| 核心框架 | Vue 3 | 渐进式框架 |
| 构建工具 | Vite 5 | 前端构建 |
| UI 组件 | Element Plus 2.6 | UI 组件库 |
| 状态管理 | Pinia 2.1 | 状态管理 |
| 路由 | Vue Router 4 | 路由管理 |
| HTTP 客户端 | Axios 1.6 | 网络请求 |

## 📂 项目结构

### 后端模块划分
vms-backend/ # 后端 Maven 父工程
├── vms-common/ # 公共模块（DTO/VO/工具类/异常处理）
├── vms-repository/ # 数据访问层（实体/Mapper）
├── vms-service-user/ # 用户服务（端口 8081）
│ ├── 登录/注册
│ ├── 组织审核
│ ├── 用户管理
│ └── 文件上传
├── vms-service-activity/ # 活动服务（端口 8083）
│ ├── 活动发布/更新/取消
│ ├── 活动列表/详情
│ ├── 活动审核
│ └── 状态管理
└── vms-service-system/ # 系统服务（端口 8087）
├── 字典管理
└── 行政区划

### 前端模块划分
vms-frontend/ # 前端 Vue 3 项目
├── src/
│ ├── api/ # API 接口封装
│ │ ├── auth.js # 认证相关
│ │ ├── user.js # 用户相关
│ │ ├── org.js # 组织相关
│ │ ├── activity.js # 活动相关
│ │ └── system.js # 系统相关
│ ├── components/ # 公共组件
│ │ └── activity/
│ │ └── ActivityDetailDialog.vue # 活动详情弹窗
│ ├── composables/ # 组合式函数
│ │ └── useRegion.js # 地区级联加载
│ ├── stores/ # Pinia 状态
│ │ └── user.js # 用户状态
│ ├── utils/ # 工具函数
│ │ ├── request.js # Axios 封装
│ │ └── activity.js # 活动状态工具
│ └── views/ # 页面视图
│ ├── login/ # 登录页
│ ├── register/ # 注册页
│ ├── dashboard/ # 布局框架/首页
│ ├── admin/ # 管理员页面
│ │ ├── orgAudit.vue # 组织审核
│ │ ├── activityAudit.vue # 活动审核
│ │ └── userManage.vue # 用户管理
│ ├── org/ # 组织页面
│ │ ├── profile.vue # 组织信息
│ │ ├── publishActivity.vue # 发布活动
│ │ └── myActivity.vue # 我的活动
│ ├── user/ # 用户页面
│ │ └── profile.vue # 个人中心
│ └── activity/ # 活动页面
│ └── list.vue # 活动列表


## 🚀 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0
- Maven 3.8+

### 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE vms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 导入表结构
source /path/to/vms_db.sql

--配置文件修改
各服务的 application.yml 需要修改数据库连接：
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/vms?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
    
### 后端启动（需启动三个服务）

# 克隆项目
git clone <repository-url>
cd vms

# 编译打包
cd vms-backend
mvn clean install -DskipTests

# 启动用户服务 (端口 8081)
cd vms-service-user
mvn spring-boot:run

# 新终端 - 启动活动服务 (端口 8083)
cd vms-service-activity
mvn spring-boot:run

# 新终端 - 启动系统服务 (端口 8087)
cd vms-service-system
mvn spring-boot:run

### 前端启动

cd vms-frontend
npm install
npm run dev
访问 http://localhost:5173 即可进入系统。

### 📊 功能模块详情
1. 用户认证模块
接口	方法	路径	端口	说明
志愿者注册	POST	/api/auth/register/volunteer	8081	普通用户注册
组织注册	POST	/api/auth/register/org	8081	组织账号注册（待审核）
统一登录	POST	/api/auth/login	8081	返回 JWT Token
获取用户信息	GET	/api/auth/userinfo	8081	获取当前登录用户信息
2. 组织管理模块
接口	方法	路径	端口	说明
组织列表	GET	/api/org/admin/list	8081	管理员分页查询组织
组织详情	GET	/api/org/admin/detail/{orgId}	8081	管理员查看组织详情
组织审核	POST	/api/org/admin/audit	8081	审核通过/拒绝
我的组织	GET	/api/org/my	8081	组织查看自己信息
更新组织	PUT	/api/org/my	8081	组织更新信息
3. 活动管理模块
接口	方法	路径	端口	说明
发布活动	POST	/api/activity/publish	8083	组织发布活动（含岗位）
活动列表	GET	/api/activity/list	8083	公开活动列表（多维筛选）
活动详情	GET	/api/activity/detail/{id}	8083	查看活动详情（含岗位）
待审核列表	GET	/api/activity/admin/pending	8083	管理员查看待审核活动
活动审核	POST	/api/activity/admin/audit	8083	管理员审核活动
我的活动	GET	/api/activity/my/list	8083	组织查看自己发布的活动
状态变更	PUT	/api/activity/my/status	8083	组织更新活动状态
更新活动	PUT	/api/activity/update	8083	编辑活动（待启动状态）
取消活动	DELETE	/api/activity/cancel/{activityId}	8083	取消活动
4. 用户管理模块
接口	方法	路径	端口	说明
更新信息	PUT	/api/user/info	8081	更新个人信息
修改密码	PUT	/api/user/password	8081	修改密码（需原密码）
上传头像	POST	/api/file/avatar	8081	上传头像图片
用户列表	GET	/api/admin/user/list	8081	管理员获取用户列表
状态管理	PUT	/api/admin/user/status	8081	管理员封禁/启用用户
5. 系统管理模块
接口	方法	路径	端口	说明
字典列表	GET	/api/dict/{dictType}	8087	获取字典数据
行政区划	GET	/api/system/region/list	8087	省市区级联数据

### 💾数据库设计

---核心表说明

表名	说明	关键字段
sys_user	用户账号表	username, password, role, status
organization	组织详情表	org_name, credit_code, legal_person, status
activity	活动表	project_no, title, status, reject_reason
activity_position	岗位表	position_name, num, hours
registration	报名表	volunteer_id, position_id, status
sys_dict	字典表	dict_type, dict_label, dict_value
sys_region	行政区划	region_code, region_name, parent_code

### 📝 开发计划
---已完成功能 ✅
--多角色注册与登录（志愿者/组织/管理员）

--JWT 认证与拦截

--组织审核管理

--活动发布与岗位管理

--活动列表与详情（多维筛选）

--活动审核流程

--组织活动管理（状态变更/编辑/取消）

--个人中心（信息修改/密码修改）

--头像上传（文件存储）

--管理员用户管理（封禁/启用）

--省市区级联选择

--字典管理

---待开发功能 🔜
--志愿者报名与签到

--签到/签退功能

--时长记录

--组织评价志愿者

--积分计算与展示

### 📄 许可证
MIT License

### 👥 贡献指南
---Fork 项目

--创建特性分支 (git checkout -b feature/AmazingFeature)

--提交更改 (git commit -m 'Add some AmazingFeature')

--推送到分支 (git push origin feature/AmazingFeature)

--提交 Pull Request

### 📞 联系方式
---项目维护者：[jtg_un]

Email：[2695375620@qq.com]

GitHub：@jtg-un