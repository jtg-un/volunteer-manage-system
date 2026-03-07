# VMS 志愿活动管理系统 - 开发进度

## 项目概述
- **技术栈**: Spring Boot 3 + MyBatis Plus + Vue 3 + Vite + Element Plus + Pinia (JavaScript)
- **架构**: 分布式独立服务
- **端口规划**: 前端 5173，用户服务 8081，活动服务 8083，系统服务 8087

---

## 项目结构

```
vsm/
├── vms-backend/                          # 后端 Maven 父工程
│   ├── pom.xml                           # 父 POM
│   ├── vms-common/                       # 公共模块（共享 JAR）
│   │   └── src/main/java/com/vms/common/
│   │       ├── config/
│   │       │   └── SecurityConfig.java   # Spring Security 配置
│   │       ├── dto/                      # 数据传输对象
│   │       │   ├── LoginDTO.java
│   │       │   ├── OrgAuditDTO.java
│   │       │   ├── OrgRegisterDTO.java
│   │       │   ├── OrgUpdateDTO.java
│   │       │   ├── VolunteerRegisterDTO.java
│   │       │   ├── ActivityPublishDTO.java
│   │       │   ├── ActivityQueryDTO.java
│   │       │   ├── ActivityAuditDTO.java
│   │       │   └── ActivityStatusDTO.java
│   │       ├── exception/
│   │       │   ├── BusinessException.java
│   │       │   └── GlobalExceptionHandler.java
│   │       ├── result/
│   │       │   └── Result.java           # 统一响应封装
│   │       ├── utils/
│   │       │   ├── JwtUtils.java
│   │       │   └── PasswordUtils.java
│   │       ├── context/                   # 用户上下文
│   │       │   └── UserContext.java
│   │       └── vo/                       # 视图对象
│   │           ├── DictVO.java
│   │           ├── LoginVO.java
│   │           ├── OrgDetailVO.java
│   │           ├── OrgListVO.java
│   │           ├── UserInfoVO.java
│   │           ├── ActivityListVO.java
│   │           ├── ActivityDetailVO.java
│   │           ├── MyActivityListVO.java
│   │           └── RegionVO.java
│   ├── vms-repository/                   # 数据访问层（共享 JAR）
│   │   └── src/main/java/com/vms/repository/
│   │       ├── config/
│   │       │   └── MybatisPlusConfig.java
│   │       ├── entity/
│   │       │   ├── Organization.java
│   │       │   ├── SysDict.java
│   │       │   ├── SysUser.java
│   │       │   ├── Activity.java
│   │       │   ├── ActivityPosition.java
│   │       │   ├── Registration.java
│   │       │   └── SysRegion.java
│   │       └── mapper/
│   │           ├── OrganizationMapper.java
│   │           ├── SysDictMapper.java
│   │           ├── SysUserMapper.java
│   │           ├── ActivityMapper.java
│   │           ├── ActivityPositionMapper.java
│   │           ├── RegistrationMapper.java
│   │           └── SysRegionMapper.java
│   ├── vms-service-user/                 # 用户服务（端口 8081）
│   │   └── src/main/
│   │       ├── java/com/vms/user/
│   │       │   ├── UserServiceApplication.java
│   │       │   ├── controller/
│   │       │   │   ├── AuthController.java
│   │       │   │   └── OrganizationController.java
│   │       │   └── service/              # 业务逻辑层
│   │       │       ├── AuthService.java
│   │       │       ├── OrganizationService.java
│   │       │       └── impl/
│   │       │           ├── AuthServiceImpl.java
│   │       │           └── OrganizationServiceImpl.java
│   │       └── resources/application.yml
│   ├── vms-service-activity/             # 活动服务（端口 8083）
│   │   └── src/main/
│   │       ├── java/com/vms/activity/
│   │       │   ├── ActivityServiceApplication.java
│   │       │   ├── controller/
│   │       │   │   ├── PublicActivityController.java
│   │       │   │   ├── OrgActivityController.java
│   │       │   │   └── AdminActivityController.java
│   │       │   └── service/              # 业务逻辑层（按职责拆分）
│   │       │       ├── PublicActivityService.java
│   │       │       ├── OrgActivityService.java
│   │       │       ├── AdminActivityService.java
│   │       │       ├── impl/
│   │       │       │   ├── PublicActivityServiceImpl.java
│   │       │       │   ├── OrgActivityServiceImpl.java
│   │       │       │   └── AdminActivityServiceImpl.java
│   │       │       └── support/
│   │       │           └── ActivitySupport.java  # 公共辅助方法
│   │       └── resources/application.yml
│   └── vms-service-system/               # 系统服务（端口 8087）
│       └── src/main/
│           ├── java/com/vms/system/
│           │   ├── SystemServiceApplication.java
│           │   ├── controller/
│           │   │   ├── DictController.java
│           │   │   └── RegionController.java
│           │   └── service/              # 业务逻辑层
│           │       ├── DictService.java
│           │       ├── RegionService.java
│           │       └── impl/
│           │           ├── DictServiceImpl.java
│           │           └── RegionServiceImpl.java
│           └── resources/application.yml
│
├── vms-frontend/                         # 前端 Vue 3 项目
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── main.js                       # 入口文件
│       ├── App.vue                       # 根组件
│       ├── api/                          # API 接口封装
│       │   ├── auth.js
│       │   ├── org.js
│       │   ├── activity.js
│       │   └── system.js
│       ├── router/
│       │   └── index.js                  # 路由配置
│       ├── stores/
│       │   └── user.js                   # Pinia 用户状态
│       ├── utils/
│       │   ├── request.js                # Axios 封装
│       │   └── activity.js               # 活动状态工具函数
│       ├── composables/
│       │   └── useRegion.js              # 地区级联加载
│       ├── components/
│       │   └── activity/
│       │       └── ActivityDetailDialog.vue  # 活动详情弹窗
│       └── views/
│           ├── login/index.vue           # 登录页
│           ├── register/index.vue        # 注册页
│           ├── dashboard/
│           │   ├── index.vue             # 布局框架
│           │   └── home.vue              # 首页
│           ├── admin/
│           │   ├── orgAudit.vue          # 管理员-组织审核
│           │   └── activityAudit.vue     # 管理员-活动审核
│           ├── org/
│           │   ├── profile.vue           # 组织-个人中心
│           │   ├── publishActivity.vue   # 组织-发布活动
│           │   └── myActivity.vue        # 组织-我的活动
│           └── activity/
│               └── list.vue              # 活动列表
│
├── vms_db.sql                            # 数据库建表脚本
└── CLAUDE.md                             # 项目开发文档
```

### 模块依赖关系
```
vms-common (公共模块)
    ↑
vms-repository (数据访问层)
    ↑
├── vms-service-user
├── vms-service-activity
└── vms-service-system
```

### 服务端口分配
| 服务 | 端口 | 职责 |
|------|------|------|
| vms-service-user | 8081 | 登录、注册、组织审核 |
| vms-service-activity | 8083 | 活动发布、岗位管理、活动检索、活动审核、状态管理 |
| vms-service-system | 8087 | 字典管理、行政区划 |

### 前端 Proxy 配置
```javascript
// vite.config.js
proxy: {
  '/api/auth': { target: 'http://localhost:8081' },
  '/api/org': { target: 'http://localhost:8081' },
  '/api/activity': { target: 'http://localhost:8083' },
  '/api/system': { target: 'http://localhost:8087' },
  '/api/dict': { target: 'http://localhost:8087' }
}
```

---

## 垂直切片一：多角色注册与登录系统 [已完成]

### 后端实现
- [x] Maven 父子工程结构
- [x] JWT 签发与验证工具类
- [x] 密码加密（BCrypt）
- [x] 志愿者注册（单表插入 sys_user）
- [x] 组织注册（双表事务：sys_user + organization，状态为待审核）
- [x] 统一登录接口
- [x] 获取当前用户信息

### 前端实现
- [x] Vue 3 + Vite 项目初始化 (JavaScript)
- [x] Element Plus + Vue Router + Pinia 配置
- [x] Axios 拦截器封装（Token 自动注入、统一错误处理）
- [x] 统一登录页面
- [x] 区分角色的注册页面（志愿者/组织）
- [x] 路由守卫（登录状态校验）
- [x] Dashboard 布局框架

### API 接口
| 方法 | 路径 | 端口 | 描述 |
|------|------|------|------|
| POST | /api/auth/register/volunteer | 8081 | 志愿者注册 |
| POST | /api/auth/register/org | 8081 | 组织注册 |
| POST | /api/auth/login | 8081 | 统一登录 |
| GET | /api/auth/userinfo | 8081 | 获取当前用户信息 |

---

## 垂直切片二：组织审核管理 [已完成]

### 后端实现
- [x] 数据库更新：organization 表添加审核字段
- [x] 实体类更新：Organization 添加审核字段
- [x] DTO/VO 创建：OrgAuditDTO, OrgUpdateDTO, OrgDetailVO, OrgListVO, DictVO
- [x] OrganizationService: 组织服务接口与实现
- [x] DictService: 字典服务接口与实现
- [x] OrganizationController: 组织管理控制器
- [x] DictController: 字典控制器
- [x] MybatisPlusConfig: 分页插件配置
- [x] 登录逻辑更新：组织用户检查审核状态

### 后端 API 接口
| 方法 | 路径 | 端口 | 描述 |
|------|------|------|------|
| GET | /api/org/admin/list | 8081 | 管理员获取组织列表（分页） |
| GET | /api/org/admin/detail/{orgId} | 8081 | 管理员获取组织详情 |
| POST | /api/org/admin/audit | 8081 | 管理员审核组织 |
| GET | /api/org/my | 8081 | 组织获取自己的信息 |
| PUT | /api/org/my | 8081 | 组织更新信息 |
| GET | /api/dict/{dictType} | 8087 | 获取字典列表 |

### 前端实现
- [x] API 封装：`src/api/org.js`
- [x] 管理员组织审核页面：`src/views/admin/orgAudit.vue`
- [x] 组织个人中心页面：`src/views/org/profile.vue`
- [x] 路由配置更新：添加管理员和组织路由
- [x] 侧边栏菜单更新：根据角色显示不同菜单

---

## 垂直切片三：志愿活动管理 [已完成]

### 后端实现
- [x] 架构物理拆分为三个独立服务
  - vms-service-user (8081): 登录、注册、组织审核
  - vms-service-activity (8083): 活动、岗位管理
  - vms-service-system (8087): 字典、行政区划
- [x] 实体类创建：Activity, ActivityPosition, SysRegion
- [x] Mapper 创建：ActivityMapper, ActivityPositionMapper, SysRegionMapper
- [x] DTO/VO 创建：ActivityPublishDTO, ActivityQueryDTO, ActivityListVO, ActivityDetailVO, RegionVO
- [x] ActivityService: 活动服务接口与实现
  - 发布活动（事务内保存活动主表及多个岗位）
  - 自动生成 P 开头的项目编号
  - 分页查询活动列表（动态多维筛选）
  - 获取活动详情（包含岗位信息）
- [x] RegionService: 行政区划服务接口与实现
  - 根据父级编码获取子级区划（支持省市区三级级联）
- [x] ActivityController: 活动管理控制器
- [x] RegionController: 行政区划控制器

### 后端 API 接口
| 方法 | 路径 | 端口 | 描述 |
|------|------|------|------|
| POST | /api/activity/publish | 8083 | 发布活动（含岗位） |
| GET | /api/activity/list | 8083 | 活动列表（分页、多维筛选） |
| GET | /api/activity/detail/{id} | 8083 | 活动详情（含岗位） |
| GET | /api/system/region/list | 8087 | 行政区划列表（级联） |

### 前端实现
- [x] API 封装：`src/api/activity.js`, `src/api/system.js`
- [x] 组织发布活动页面：`src/views/org/publishActivity.vue`
  - 动态增减岗位表单
  - 省市区三级联动选择
  - 服务类别下拉选择
- [x] 活动列表页面：`src/views/activity/list.vue`
  - 响应式筛选工具栏
  - 分页展示
  - 活动详情弹窗
- [x] 路由配置更新
- [x] 侧边栏菜单更新

---

## 垂直切片四：活动全生命周期管理 [已完成]

### 后端实现
- [x] 活动状态流转设计：
  - 发布后状态为 3（待审核）
  - 管理员审核通过 → 0（待启动）
  - 管理员审核拒绝 → 4（已拒绝）
  - 组织启动活动 → 1（运行中）
  - 组织结项活动 → 2（已结项）
- [x] Registration 实体与 Mapper 创建（用于报名统计）
- [x] DTO 创建：ActivityAuditDTO, ActivityStatusDTO
- [x] VO 创建：MyActivityListVO（含报名统计）
- [x] ActivityService 新增方法：
  - 管理员审核活动
  - 管理员获取待审核活动列表
  - 组织更新活动状态
  - 组织获取自己的活动列表（含报名统计）
- [x] ActivityController 新增接口：
  - `/admin/pending` 管理员活动列表（支持状态筛选）
  - `/admin/audit` 管理员审核活动
  - `/my/list` 组织活动列表
  - `/my/status` 组织状态变更
- [x] Activity 实体添加 `rejectReason` 字段存储拒绝原因
- [x] ActivityListVO、ActivityDetailVO、MyActivityListVO 添加 `rejectReason` 字段

### 后端 API 接口
| 方法 | 路径 | 端口 | 描述 |
|------|------|------|------|
| GET | /api/activity/admin/pending | 8083 | 管理员获取活动列表（支持状态筛选，不传则查全部） |
| POST | /api/activity/admin/audit | 8083 | 管理员审核活动 |
| GET | /api/activity/my/list | 8083 | 组织获取自己的活动列表 |
| PUT | /api/activity/my/status | 8083 | 组织更新活动状态 |

### 前端实现
- [x] API 封装：`src/api/activity.js` 新增审核和状态接口
- [x] 管理员活动管理页面：`src/views/admin/activityAudit.vue`
  - 活动列表（支持状态筛选：待审核/待启动/运行中/已结项/已拒绝）
  - 审核通过/拒绝操作（仅待审核状态可操作）
  - 拒绝原因输入与显示
  - 活动详情查看
- [x] 组织活动管理页面：`src/views/org/myActivity.vue`
  - 自己发布的活动列表
  - 报名人数统计
  - 待审核报名数提示
  - 启动/结项操作
  - 拒绝原因显示（状态为已拒绝时）
- [x] 路由配置更新
- [x] 侧边栏菜单更新：管理员菜单改为子菜单形式

---

## 问题修复记录

### 2026-03-05: 架构物理拆分
**问题描述**:
- 原单体架构需拆分为分布式独立服务

**解决方案**:
1. 保持 vms-common 和 vms-repository 作为共享 Jar 依赖
2. 创建三个独立的 Spring Boot 应用：
   - vms-service-user (8081): 用户相关
   - vms-service-activity (8083): 活动相关
   - vms-service-system (8087): 系统配置相关
3. 服务之间不进行 RPC 调用
4. 前端通过 Vite Proxy 将不同前缀的 API 请求分发到对应后端端口

### 2026-03-05: 模块循环依赖问题
**问题描述**:
- `AuthService.java` 无法找到 `com.vms.api.dto` 包
- 原因：DTO/VO 放在 vms-api 模块，但 vms-api 依赖 vms-service，造成循环依赖

**解决方案**:
- 将 `dto` 和 `vo` 从 `vms-api` 移动到 `vms-common` 模块
- 更新相关类的 import：`AuthService.java`, `AuthServiceImpl.java`, `AuthController.java`

### 2026-03-05: Axios 类型导入错误
**问题描述**:
- 前端页面空白，控制台报错：`The requested module 'axios.js' does not provide an export named 'AxiosInstance'`
- 原因：Vite 要求分开导入值和类型

**解决方案**:
```javascript
// 错误写法
import axios, { AxiosInstance } from 'axios'

// 正确写法 (JavaScript)
import axios from 'axios'
```


### 2026-03-05: @RequestParam/@PathVariable 参数名反射问题
**问题描述**:
- 后端报错：`Name for argument of type [int] not specified`
- 原因：Java 编译器默认不保留参数名称

**解决方案**:
- 在 `@RequestParam` 和 `@PathVariable` 中显式指定 `value` 属性

### 2026-03-05: Result<Void> 类型不兼容问题
**问题描述**:
- ActivityController 编译报错：`不兼容的类型。实际为 Result<String>，需要 Result<Void>`
- 原因：`Result.success("审核完成")` 调用的是 `success(T data)` 方法，T 被推断为 String

**解决方案**:
- 在 `Result.java` 中添加 `successMsg(String message)` 方法，专门用于返回带消息但无数据的 `Result<Void>`
- 将 Controller 中的 `Result.success("消息")` 改为 `Result.successMsg("消息")`

### 2026-03-05: 组织审核列表无法显示问题
**问题描述**:
- 系统管理员和组织看不到待审核的组织列表
- 原因：OrganizationController 中权限验证失败时使用 `Result.error(403, "无权限访问")` 返回，但前端响应拦截器处理不正确

**解决方案**:
- 将 OrganizationController 中的权限验证失败改为抛出 `BusinessException`
- 添加 `BusinessException` 的 import
- 统一异常处理方式，确保 GlobalExceptionHandler 能正确捕获并返回错误信息

### 2026-03-06: 活动列表页面报错 Cannot read properties of undefined (reading 'records')
**问题描述**:
- 前端报错：`TypeError: Cannot read properties of undefined (reading 'records')`
- 原因：`request.js` 封装已返回 `response.data.data`，但 Vue 组件中又使用了 `res.data.records`

**解决方案**:
- 修正 `myActivity.vue` 和 `activityAudit.vue` 中的数据访问方式
- 将 `res.data.records` 改为 `res.records`
- 将 `res.data.total` 改为 `res.total`
- 将 `res.data` 改为 `res`

### 2026-03-06: 架构解耦重构
**问题描述**:
- vms-service 共享模块包含所有 Service 接口和实现类，导致严重的逻辑耦合
- 各独立服务模块都依赖这个共享模块，违反了微服务独立部署的原则

**解决方案**:
1. 将 Service 实现类物理迁移到各自的服务模块中
2. 废弃 vms-service 共享模块，实现真正的服务解耦
3. 更新模块依赖关系：
   - 移除根 pom.xml 中的 vms-service 模块声明
   - 各服务模块依赖改为 vms-repository
4. 更新所有 Controller 的 import 语句
5. 删除 vms-service 目录

**迁移文件清单**:
| 原位置 | 新位置 |
|--------|--------|
| vms-service/.../AuthService.java | vms-service-user/.../service/AuthService.java |
| vms-service/.../OrganizationService.java | vms-service-user/.../service/OrganizationService.java |
| vms-service/.../impl/AuthServiceImpl.java | vms-service-user/.../service/impl/AuthServiceImpl.java |
| vms-service/.../impl/OrganizationServiceImpl.java | vms-service-user/.../service/impl/OrganizationServiceImpl.java |
| vms-service/.../ActivityService.java | vms-service-activity/.../service/ActivityService.java |
| vms-service/.../impl/ActivityServiceImpl.java | vms-service-activity/.../service/impl/ActivityServiceImpl.java |
| vms-service/.../DictService.java | vms-service-system/.../service/DictService.java |
| vms-service/.../RegionService.java | vms-service-system/.../service/RegionService.java |
| vms-service/.../impl/DictServiceImpl.java | vms-service-system/.../service/impl/DictServiceImpl.java |
| vms-service/.../impl/RegionServiceImpl.java | vms-service-system/.../service/impl/RegionServiceImpl.java |

### 2026-03-07: 后端代码拆分重构（接口隔离）
**问题描述**:
- ActivityServiceImpl.java 代码行数达 427 行，职责混杂
- ActivityController.java 代码行数达 212 行，包含大量重复代码
- 违反单一职责原则，可读性和可维护性差

**解决方案**:

1. **按职责拆分 Service 接口**：
   - `PublicActivityService.java` - 公开接口（活动列表、详情）
   - `OrgActivityService.java` - 组织接口（发布、管理活动）
   - `AdminActivityService.java` - 管理员接口（审核活动）

2. **每个接口对应一个实现类**：
   - `PublicActivityServiceImpl.java` (~92行)
   - `OrgActivityServiceImpl.java` (~141行)
   - `AdminActivityServiceImpl.java` (~93行)

3. **提取公共辅助类**：
   - `UserContext.java` - Token 解析和权限验证
   - `ActivitySupport.java` - VO 转换、字典查询、项目编号生成

4. **拆分 Controller**：
   - `PublicActivityController.java` (~43行)
   - `OrgActivityController.java` (~92行)
   - `AdminActivityController.java` (~54行)

**重构收益**:
- 每个文件控制在 150 行以内
- 职责分离清晰，符合接口隔离原则
- 消除 Token 解析和组织查询的重复代码

### 2026-03-07: 前端代码拆分重构
**问题描述**:
- `views/activity/list.vue` 代码行数达 346 行
- `views/admin/activityAudit.vue` 代码行数达 251 行
- 存在大量重复的工具函数和组件代码

**解决方案**:

1. **提取工具函数** (`utils/activity.js`):
   - `getStatusName()` - 状态名称映射
   - `getStatusType()` - 状态标签类型
   - `formatDateTime()` - 时间格式化
   - `getCountClass()` - 报名人数样式

2. **提取 Composable** (`composables/useRegion.js`):
   - 地区级联加载逻辑
   - 支持懒加载

3. **提取公共组件** (`components/activity/ActivityDetailDialog.vue`):
   - 活动详情弹窗组件
   - 支持插槽自定义操作按钮

**重构收益**:
- `list.vue`: 346行 → 239行
- `activityAudit.vue`: 251行 → 198行
- 消除重复代码，提高复用性

---

## 待开发功能

### 垂直切片五：志愿者报名与签到
- [ ] 志愿者报名活动
- [ ] 签到/签退功能
- [ ] 时长记录

### 垂直切片六：评价与积分
- [ ] 组织评价志愿者
- [ ] 积分计算与展示

---

## 数据库表结构
- `sys_user`: 用户账号表（角色: 0志愿者 1组织负责人 2系统管理员）
- `organization`: 志愿队伍详情表
- `activity`: 志愿项目表
- `activity_position`: 活动岗位表
- `registration`: 报名表
- `checkin_log`: 考勤流水表
- `volunteer_record`: 志愿时长记录表
- `evaluation`: 评价表
- `sys_dict`: 系统字典表
- `sys_region`: 行政区划字典

---

## 启动说明

### 后端启动（需启动三个服务）
```bash
cd vms-backend
mvn clean install -DskipTests

# 启动用户服务 (8081)
cd vms-service-user
mvn spring-boot:run

# 新终端 - 启动活动服务 (8083)
cd vms-service-activity
mvn spring-boot:run

# 新终端 - 启动系统服务 (8087)
cd vms-service-system
mvn spring-boot:run
```

### 前端启动
```bash
cd vms-frontend
npm run dev
```

### 数据库配置
修改各服务 `application.yml` 中的数据库连接信息。
- 当前密码: `123456`

---

*最后更新: 2026-03-07*