SET NAMES utf8mb4;

-- Roles
INSERT INTO roles (id, code, name, created_at, updated_at) VALUES
(1, 'ADMIN', '管理员', NOW(), NOW()),
(2, 'PARENT', '家长', NOW(), NOW()),
(3, 'CHILD', '孩子', NOW(), NOW());

-- Users (passwords: admin123 / parent123 / child123)
INSERT INTO users (id, username, password_hash, display_name, phone, points, enabled, last_login_at, created_at, updated_at) VALUES
(1, 'admin',  '$2a$10$RnsDscXtg5kKGBomLSdLmeVsdSPzEESBNnvjiO8xsCbrVENdj8JR.', '系统管理员', NULL, 0, 1, NULL, NOW(), NOW()),
(2, 'parent1', '$2a$10$V9pm9F0TKWbo7rqXrLqnaO7WQTL.WyehafRtMMPAjGAzUOhoHbBZa', '家长一', '13800000001', 0, 1, NULL, NOW(), NOW()),
(3, 'child1',  '$2a$10$7e7IhjPBfY5Sq7LldwHqweMwNeiG7NIofsIPayReKSqTDtb//PxbG', '小明', '13800000002', 20, 1, NULL, NOW(), NOW()),
(4, 'child2',  '$2a$10$7e7IhjPBfY5Sq7LldwHqweMwNeiG7NIofsIPayReKSqTDtb//PxbG', '小红', '13800000003', 5, 1, NULL, NOW(), NOW());

-- User roles
INSERT INTO sys_user_role (user_id, role_id, created_at, updated_at) VALUES
(1, 1, NOW(), NOW()),
(2, 2, NOW(), NOW()),
(3, 3, NOW(), NOW()),
(4, 3, NOW(), NOW());

-- Menus
INSERT INTO sys_menu (id, parent_id, name, path, component, type, permission, icon, sort_order, visible, cache, is_frame, created_at, updated_at) VALUES
(1, 0, '首页', '/home', 'HomeDashboard', 2, 'home.view', 'menu', 0, 1, 1, 0, NOW(), NOW()),

(10, 0, '系统管理', NULL, NULL, 1, NULL, 'setting', 10, 1, 1, 0, NOW(), NOW()),
(11, 10, '角色管理', '/admin/roles', 'AdminRoles', 2, 'admin.role.list', 'role', 1, 1, 1, 0, NOW(), NOW()),
(12, 10, '权限列表', '/admin/permissions', 'AdminPermissions', 2, 'admin.permission.list', 'lock', 2, 1, 1, 0, NOW(), NOW()),
(13, 10, '用户管理', '/admin/users', 'AdminUsers', 2, 'admin.user.list', 'user', 3, 1, 1, 0, NOW(), NOW()),
(14, 10, '日志管理', '/admin/logs', 'AdminLogs', 2, 'admin.log.list', 'menu', 4, 1, 1, 0, NOW(), NOW()),
(110, 11, '新增角色', NULL, NULL, 3, 'admin.role.create', 'menu', 0, 1, 1, 0, NOW(), NOW()),
(111, 11, '分配权限', NULL, NULL, 3, 'admin.role.assign', 'menu', 0, 1, 1, 0, NOW(), NOW()),
(130, 13, '新增用户', NULL, NULL, 3, 'admin.user.create', 'menu', 0, 1, 1, 0, NOW(), NOW()),
(131, 13, '编辑用户', NULL, NULL, 3, 'admin.user.edit', 'menu', 0, 1, 1, 0, NOW(), NOW()),
(132, 13, '删除用户', NULL, NULL, 3, 'admin.user.delete', 'menu', 0, 1, 1, 0, NOW(), NOW()),

(20, 0, '家长中心', NULL, NULL, 1, NULL, 'parent', 20, 1, 1, 0, NOW(), NOW()),
(21, 20, '孩子管理', '/parent/children', 'ParentChildren', 2, 'parent.child.list', 'child', 1, 1, 1, 0, NOW(), NOW()),
(22, 20, '任务列表', '/parent/tasks', 'ParentTaskList', 2, 'parent.task.list', 'task', 2, 1, 1, 0, NOW(), NOW()),
(23, 20, '任务创建', '/parent/task-create', 'ParentTaskCreate', 2, 'parent.task.create', 'task', 3, 1, 1, 0, NOW(), NOW()),
(24, 20, '任务统计', '/parent/task-stats', 'ParentTaskStats', 2, 'parent.task.stats', 'chart', 4, 1, 1, 0, NOW(), NOW()),
(25, 20, '奖励管理', '/parent/rewards', 'ParentRewardList', 2, 'parent.reward.list', 'gift', 5, 1, 1, 0, NOW(), NOW()),
(26, 20, '兑换审核', '/parent/redemptions', 'ParentRedemptions', 2, 'parent.redemption.list', 'check', 6, 1, 1, 0, NOW(), NOW()),
(27, 20, '报表统计', '/parent/reports', 'ParentReports', 2, 'parent.report.view', 'chart', 7, 1, 1, 0, NOW(), NOW()),
(210, 21, '新增孩子', NULL, NULL, 3, 'parent.child.create', 'menu', 0, 1, 1, 0, NOW(), NOW()),
(250, 25, '新增奖励', NULL, NULL, 3, 'parent.reward.create', 'menu', 0, 1, 1, 0, NOW(), NOW()),
(260, 26, '兑换审核', NULL, NULL, 3, 'parent.redemption.review', 'menu', 0, 1, 1, 0, NOW(), NOW()),
(270, 27, '导出报表', NULL, NULL, 3, 'parent.report.export', 'menu', 0, 1, 1, 0, NOW(), NOW()),

(30, 0, '孩子中心', NULL, NULL, 1, NULL, 'child', 30, 1, 1, 0, NOW(), NOW()),
(31, 30, '今日任务', '/child/tasks', 'ChildTasks', 2, 'child.task.list', 'task', 1, 1, 1, 0, NOW(), NOW()),
(32, 30, '兑换记录', '/child/rewards', 'ChildRewards', 2, 'child.reward.list', 'ticket', 2, 1, 1, 0, NOW(), NOW()),
(33, 30, '积分明细', '/child/points', 'ChildPoints', 2, 'child.points.list', 'points', 3, 1, 1, 0, NOW(), NOW()),
(310, 31, '提交任务', NULL, NULL, 3, 'child.task.complete', 'menu', 0, 1, 1, 0, NOW(), NOW()),
(320, 32, '兑换奖励', NULL, NULL, 3, 'child.reward.redeem', 'menu', 0, 1, 1, 0, NOW(), NOW());

-- Role menus
INSERT INTO sys_role_menu (role_id, menu_id, created_at, updated_at)
SELECT 1, id, NOW(), NOW() FROM sys_menu;

INSERT INTO sys_role_menu (role_id, menu_id, created_at, updated_at) VALUES
(2, 1, NOW(), NOW()),
(2, 20, NOW(), NOW()),
(2, 21, NOW(), NOW()),
(2, 22, NOW(), NOW()),
(2, 23, NOW(), NOW()),
(2, 24, NOW(), NOW()),
(2, 25, NOW(), NOW()),
(2, 26, NOW(), NOW()),
(2, 27, NOW(), NOW()),
(2, 210, NOW(), NOW()),
(2, 250, NOW(), NOW()),
(2, 260, NOW(), NOW()),
(2, 270, NOW(), NOW());

INSERT INTO sys_role_menu (role_id, menu_id, created_at, updated_at) VALUES
(3, 1, NOW(), NOW()),
(3, 30, NOW(), NOW()),
(3, 31, NOW(), NOW()),
(3, 32, NOW(), NOW()),
(3, 33, NOW(), NOW()),
(3, 310, NOW(), NOW()),
(3, 320, NOW(), NOW());

-- Parent-child mapping
INSERT INTO parent_children (id, parent_id, child_id, created_at, updated_at) VALUES
(1, 2, 3, NOW(), NOW()),
(2, 2, 4, NOW(), NOW());

-- Rewards
INSERT INTO rewards (id, parent_id, name, description, points_cost, stock, status, created_at, updated_at) VALUES
(1, 2, '周末电影票', '完成任务即可兑换', 30, 5, 'ACTIVE', NOW(), NOW()),
(2, 2, '游戏时间 30 分钟', '周末奖励', 20, 10, 'ACTIVE', NOW(), NOW());

-- Tasks
INSERT INTO tasks (id, parent_id, child_id, title, description, points, status, submitted_at, completed_at, checkin_note, checkin_images, created_at, updated_at) VALUES
(1, 2, 3, '完成数学作业', '完成课后练习', 10, 'PENDING', NOW(), NULL, '已提交作业', NULL, NOW(), NOW()),
(2, 2, 3, '整理书包', '上学前整理书包', 5, 'COMPLETED', NOW(), NOW(), '整理完成', NULL, NOW(), NOW()),
(3, 2, 4, '背诵英语单词', '每天 20 个', 8, 'PENDING', NULL, NULL, NULL, NULL, NOW(), NOW());

-- Redemptions
INSERT INTO redemptions (id, reward_id, child_id, points_cost, status, redeemed_at, review_note, reviewed_at, created_at, updated_at) VALUES
(1, 1, 3, 30, 'PENDING', NOW(), NULL, NULL, NOW(), NOW()),
(2, 2, 3, 20, 'APPROVED', NOW(), '同意兑换', NOW(), NOW(), NOW());

-- Points ledger
INSERT INTO points_ledger (id, child_id, points, type, ref_type, ref_id, note, created_at, updated_at) VALUES
(1, 3, 10, 'EARN', 'TASK', 2, '任务完成', NOW(), NOW()),
(2, 3, 20, 'SPEND', 'REWARD', 2, '兑换奖励', NOW(), NOW());
