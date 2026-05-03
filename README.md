# 云图库 (ZXL-Picture)

一个基于 Spring Boot + Vue 3 的前后端分离图片管理系统。

## 技术栈

### 前端
- **框架**: Vue 3 + TypeScript
- **构建工具**: Vite
- **UI 组件库**: Ant Design Vue
- **状态管理**: Pinia
- **路由**: Vue Router
- **图表**: ECharts + vue-echarts
- **图片处理**: vue-cropper
- **HTTP 客户端**: Axios

### 后端
- **框架**: Spring Boot 2.7.6
- **ORM**: MyBatis Plus
- **数据库**: MySQL
- **缓存**: Redis + Caffeine
- **认证**: Sa-Token
- **分库分表**: ShardingSphere
- **实时通信**: WebSocket + Disruptor
- **对象存储**: 腾讯云 COS
- **接口文档**: Knife4j

## 项目结构

```
zxl-picture-frontend/     # 前端项目
zxl-picture-backend/     # 后端项目
zxl-picture-backend-ddd/  # 后端DDD架构版本
```

## 功能特性

- 用户管理（登录、注册、权限控制）
- 图片上传（本地上传、URL上传、批量上传）
- 图片管理（浏览、编辑、删除、审核）
- 空间管理（私有空间、团队空间）
- 空间成员管理（角色：viewer/editor/admin）
- 空间分析（使用统计、标签分析、分类分析）
- 图片搜索（名称、分类、标签、颜色相似度）
- 团队协作（WebSocket 实时同步）

## 快速开始

### 后端启动

```bash
cd zxl-picture-backend
# 配置数据库和Redis连接
mvn spring-boot:run
```

### 前端启动

```bash
cd zxl-picture-frontend
npm install
npm run dev
```

## 数据库

数据库脚本位于 `zxl-picture-backend/sql/create_user.sql`

主要表：
- `user` - 用户表
- `picture` - 图片表
- `space` - 空间表
- `space_user` - 空间成员表

## API 文档

后端启动后访问: `http://localhost:8080/doc.html`