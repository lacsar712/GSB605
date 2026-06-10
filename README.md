# 假期学生管理系统

基于 Spring Boot + Vue3 的前后端分离假期学生管理系统，包含学生端、教师端、学校管理端三种身份。

## 🛠 技术栈

- **Frontend**: Vue3 + Vite + Element Plus + Pinia + Axios
- **Backend**: Spring Boot 3.2 + MyBatis + JWT
- **Database**: MySQL 8.0

## 🚀 启动指南 (How to Run)

1. 确保 Docker Desktop 已启动
2. 在根目录执行：
```bash
docker compose up --build
```
3. 等待容器启动完成（首次构建约需5-10分钟）

## 🔗 服务地址 (Services)

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8000/api
- **Swagger文档**: http://localhost:8000/api/swagger-ui.html
- **Database**: localhost:3306 (user: root / pass: root123)

## 🧪 测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | 123456 | 学校管理端 |
| 教师 | teacher | 123456 | 教师端 |
| 学生 | student | 123456 | 学生端 |

## 📋 功能模块

### 1. 学生端 (Student)
- ✅ 统一身份认证登录
- ✅ 活动列表（轮播图、筛选查询、线上/线下分类）
- ✅ 活动详情（视频、图片、附件下载）
- ✅ 活动报名（信息填写、学校名称智能匹配、附件上传）
- ✅ 已报名活动查看（审批进度、分组信息）
- ✅ 活动反馈提交（富文本、附件上传）
- ✅ 个人中心（信息修改、密码修改）

### 2. 教师端 (Teacher)
- ✅ 活动管理（创建、编辑、发布、下架）
- ✅ 报名管理（审批、查看详情）
- ✅ 个人中心

### 3. 学校管理端 (School Admin)
- ✅ 数据概览（统计数据、最新动态）
- ✅ 活动管理（所有活动的管理）
- ✅ 用户管理（学生/教师管理、状态控制）
- ✅ 系统设置

## 🔧 特色功能

### 学校名称标准化
系统实现了学校名称的智能标准化功能：
- 输入"武汉理工"自动标准化为"武汉理工大学"
- 支持模糊搜索和自动补全
- 根据历史数据智能推荐

### 报名资格自动校验
- 根据绩点、年级、专业等条件自动筛选可参加的活动
- 不符合条件的活动对学生不可见
- 防止重复报名

### 活动分组
- 按招生对象学校自动分组
- 方便管理和统计

## 📁 项目结构

```
taskId605/
├── docker-compose.yml      # Docker编排配置
├── README.md               # 项目说明
├── backend/                # Spring Boot后端
│   ├── Dockerfile
│   ├── pom.xml
│   ├── settings.xml        # Maven镜像配置
│   └── src/main/
│       ├── java/com/vacation/student/
│       │   ├── config/     # 配置类
│       │   ├── controller/ # 控制器
│       │   ├── dto/        # 数据传输对象
│       │   ├── entity/     # 实体类
│       │   ├── mapper/     # MyBatis映射
│       │   ├── service/    # 业务逻辑
│       │   └── util/       # 工具类
│       └── resources/
│           └── application.yml
├── frontend/               # Vue3前端
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api/            # API接口
│       ├── assets/         # 静态资源
│       ├── router/         # 路由配置
│       ├── stores/         # Pinia状态
│       └── views/          # 页面组件
│           ├── common/     # 公共页面
│           ├── student/    # 学生端
│           ├── teacher/    # 教师端
│           └── school/     # 学校端
└── database/
    └── init.sql            # 数据库初始化脚本
```

## 🐳 Docker配置说明

### 端口映射
- Frontend: 3000:3000
- Backend: 8000:8000
- MySQL: 3306:3306

### 服务依赖
- Frontend → Backend
- Backend → Database

### 数据持久化
MySQL数据通过Docker Volume持久化存储。

## ⚠️ 注意事项

1. 首次启动时数据库初始化需要等待MySQL健康检查通过
2. 如遇到端口冲突，请先关闭占用端口的服务
3. 前端构建可能需要较长时间，请耐心等待

## 📝 API接口

### 认证相关
- POST `/api/auth/login` - 登录
- POST `/api/auth/register` - 注册
- POST `/api/auth/change-password` - 修改密码

### 用户相关
- GET `/api/users/me` - 获取当前用户信息
- PUT `/api/users/me` - 更新用户信息

### 活动相关
- GET `/api/activities` - 获取活动列表
- GET `/api/activities/{id}` - 获取活动详情
- POST `/api/activities` - 创建活动
- PUT `/api/activities/{id}` - 更新活动

### 报名相关
- GET `/api/registrations/my` - 获取我的报名记录
- POST `/api/registrations` - 提交报名
- PUT `/api/registrations/{id}/approve` - 审批报名

### 反馈相关
- GET `/api/feedbacks/my` - 获取我的反馈
- POST `/api/feedbacks` - 提交反馈

## 🎨 UI设计

- 采用现代化渐变配色方案
- 响应式布局，支持移动端适配
- Element Plus组件库
- 骨架屏加载状态
- Toast消息提示
