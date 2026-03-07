VMS 志愿活动管理系统 (Volunteer Management System)VMS 是一款专为高校或社区设计的分布式志愿服务管理平台。系统采用现代微服务拆分思想，实现了从组织入驻、活动发布、全生命周期审核到资源映射存储的闭环管理。🌟 核心特性🚀 分布式架构：基于业务逻辑将系统拆分为用户、活动、系统三大独立服务，支持物理隔离部署。🛡️ 权限控制：集成 Spring Security + JWT，支持志愿者、组织负责人、系统管理员三类角色的动态权限校验。📋 全生命周期管理：严谨的活动流转逻辑（待审核 -> 待启动 -> 运行中 -> 已结项），支持管理员拒绝理由回馈。🗺️ 行政区划联动：内置省市区三级级联数据，支持地理维度的精准活动投放。📁 智能资源映射：实现动静分离，头像与海报通过物理磁盘存储结合虚拟路径映射，保障数据库轻量化。🏗️ 系统架构系统采用 Maven 多模块 构建，通过共享仓库（Repository）确保数据一致性，各业务服务通过不同的端口对外提供 RESTful API。模块依赖关系代码段graph TD
    Common(vms-common) --> Repo(vms-repository)
    Repo --> UserSvc(vms-service-user:8081)
    Repo --> ActSvc(vms-service-activity:8083)
    Repo --> SysSvc(vms-service-system:8087)
    Frontend(vms-frontend:5173) -- Proxy --> UserSvc
    Frontend -- Proxy --> ActSvc
    Frontend -- Proxy --> SysSvc
🛠️ 技术栈后端核心框架: Spring Boot 3.xORM: MyBatis Plus 3.5.x安全: Spring Security + JWT (JSON Web Token)数据库: MySQL 8.0构建工具: Maven 3.9前端框架: Vue 3 (Composition API)构建: ViteUI 组件: Element Plus状态管理: Pinia网络请求: Axios📅 开发进度已完成模块 [100%][x] 垂直切片一：多角色认证系统（BCrypt 加密、JWT 签发）。[x] 垂直切片二：组织入驻审核流（管理员列表审核、组织资料维护）。[x] 垂直切片三：活动发布系统（支持动态岗位增删、行政区划级联选择）。[x] 垂直切片四：活动生命周期管控（状态自动机逻辑、拒绝原因回显）。[x] 垂直切片五：用户中心与运维（头像上传、账号封禁、活动编辑与取消）。待开发计划 [进行中][ ] 垂直切片六：志愿者报名与实时签到记录。[ ] 垂直切片七：服务时长核定与双向评价体系。🚀 快速启动1. 数据库准备导入根目录下的 vms_db.sql 到 MySQL 数据库。2. 后端启动按顺序启动以下服务实例：Bash# 1. 编译公共依赖
mvn clean install -DskipTests

# 2. 依次启动三个微服务 (可并行)
cd vms-service-user && mvn spring-boot:run     # Port: 8081
cd vms-service-activity && mvn spring-boot:run # Port: 8083
cd vms-service-system && mvn spring-boot:run   # Port: 8087
3. 前端启动Bashcd vms-frontend
npm install
npm run dev
访问地址: http://localhost:5173📂 项目结构一览模块职责vms-commonDTO/VO 定义、异常拦截、JWT 及通用工具类vms-repository数据库 Entity、Mapper 接口及 MyBatis-Plus 分页配置vms-service-user认证、组织审核、管理员用户运维vms-service-activity活动发布、审核流、状态控制、岗位管理vms-service-system系统字典、行政区划查询、物理文件存储服务