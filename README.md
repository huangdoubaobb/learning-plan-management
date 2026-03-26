# 学习计划管理系统

这是一个基于 **Vue 3 + Spring Boot + MySQL** 的学习计划管理系统原型，包含：
- 后台管理（角色/权限/用户管理）
- 家长注册与登录
- 家长创建孩子账号、每日/周期任务、积分与奖励
- 孩子查看任务、完成任务、打卡备注、积分兑换奖励（需家长审核）
- 兑换待审核提醒
- 报表概览与趋势图（完成率、积分）
- Excel 报表导出
- 移动端自适应页面

## 目录结构
- `backend`：Spring Boot 后端
- `frontend`：Vue 3 前端
- `database/schema.sql`：MySQL 参考建表脚本

## 环境要求
- JDK 1.8
- Maven 3.8+
- Node.js 18+
- MySQL 8+

## 数据库配置
`backend/src/main/resources/application.yml` 已配置：
- 地址：`127.0.0.1:3306`
- 用户名：`root`
- 密码：`a123456`
- 数据库：`learning_plan`

你可以先执行：
```sql
CREATE DATABASE IF NOT EXISTS learning_plan;
```

## 启动后端
进入 `backend` 目录：
```bash
mvn spring-boot:run
```

默认会创建系统管理员账号：
- 用户名：`admin`
- 密码：`admin123`

如需修改，调整 `application.yml` 中：
```yaml
app:
  admin:
    username: admin
    password: admin123
```

## 启动前端
进入 `frontend` 目录：
```bash
npm install
npm run dev
```

浏览器访问：`http://localhost:5173`

## 核心接口（节选）
- `POST /api/auth/register-parent` 家长注册
- `POST /api/auth/login` 登录
- `GET /api/admin/roles` 角色列表
- `PUT /api/admin/roles/{id}/permissions` 角色权限
- `POST /api/parent/children` 家长创建孩子
- `POST /api/parent/tasks` 创建任务
- `GET /api/parent/redemptions` 兑换审核列表
- `GET /api/parent/redemptions/pending-count` 待审核数量
- `POST /api/parent/redemptions/{id}/approve` 同意兑换
- `POST /api/parent/redemptions/{id}/reject` 拒绝兑换
- `POST /api/child/tasks/{id}/complete` 孩子完成任务（可附打卡备注）
- `POST /api/child/rewards/{id}/redeem` 孩子兑换奖励
- `GET /api/parent/report/summary?childId=...` 报表概览
- `GET /api/parent/report/trend?childId=...&days=14` 完成率趋势
- `GET /api/parent/report/points-trend?childId=...&days=14` 积分趋势
- `GET /api/parent/report/export?childId=...&days=30` 导出 Excel

## 周期任务说明
创建任务时可传入：
- `repeatType`: `NONE` / `DAILY` / `WEEKLY` / `MONTHLY`
- `repeatUntil`: `YYYY-MM-DD`

孩子完成任务后，系统会自动生成下一次任务，直到超过 `repeatUntil`。
