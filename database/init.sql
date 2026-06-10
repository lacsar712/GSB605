-- 假期学生管理系统数据库初始化脚本
-- 编码: UTF-8

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像',
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT' COMMENT '角色: STUDENT-学生, TEACHER-教师, SCHOOL-学校管理员',
    student_no VARCHAR(50) COMMENT '学号/工号',
    school VARCHAR(100) COMMENT '所属学校',
    department VARCHAR(100) COMMENT '院系',
    major VARCHAR(100) COMMENT '专业',
    grade VARCHAR(20) COMMENT '年级',
    gpa DECIMAL(3,2) COMMENT '绩点',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_student_no (student_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建活动表
CREATE TABLE IF NOT EXISTS activities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '活动标题',
    summary VARCHAR(500) COMMENT '活动简介',
    content TEXT COMMENT '活动详情',
    cover_image VARCHAR(255) COMMENT '封面图片',
    activity_type VARCHAR(20) DEFAULT 'OFFLINE' COMMENT '活动类型: ONLINE-线上, OFFLINE-线下',
    signup_start_time DATETIME COMMENT '报名开始时间',
    signup_end_time DATETIME COMMENT '报名截止时间',
    activity_start_time DATETIME COMMENT '活动开始时间',
    activity_end_time DATETIME COMMENT '活动结束时间',
    location VARCHAR(200) COMMENT '活动地点',
    max_participants INT DEFAULT 0 COMMENT '招生人数限制',
    current_participants INT DEFAULT 0 COMMENT '当前报名人数',
    required_gpa DECIMAL(3,2) COMMENT '绩点要求',
    required_grades VARCHAR(100) COMMENT '年级要求',
    required_majors VARCHAR(500) COMMENT '专业要求',
    need_approval TINYINT DEFAULT 1 COMMENT '是否需要审批',
    is_banner TINYINT DEFAULT 0 COMMENT '是否轮播展示',
    banner_image VARCHAR(255) COMMENT '轮播图片',
    attachments TEXT COMMENT '附件列表JSON',
    registration_config TEXT COMMENT '报名字段配置JSON',
    feedback_start_time DATETIME COMMENT '反馈开始时间',
    feedback_end_time DATETIME COMMENT '反馈截止时间',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-草稿, 1-发布, 2-结束',
    creator_id BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_type (activity_type),
    INDEX idx_creator (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- 创建报名记录表
CREATE TABLE IF NOT EXISTS registrations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_school VARCHAR(100) COMMENT '招生对象学校',
    contact_info VARCHAR(100) COMMENT '联系方式',
    remark VARCHAR(500) COMMENT '备注信息',
    attachments TEXT COMMENT '附件JSON',
    approval_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '审批状态: PENDING-待审批, APPROVED-通过, REJECTED-拒绝',
    approver_id BIGINT COMMENT '审批人ID',
    approval_time DATETIME COMMENT '审批时间',
    approval_comment VARCHAR(500) COMMENT '审批意见',
    group_id BIGINT COMMENT '分组ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_activity_user (activity_id, user_id),
    INDEX idx_user (user_id),
    INDEX idx_activity (activity_id),
    INDEX idx_status (approval_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报名记录表';

-- 创建反馈表
CREATE TABLE IF NOT EXISTS feedbacks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    registration_id BIGINT NOT NULL COMMENT '报名记录ID',
    user_id BIGINT NOT NULL COMMENT '提交用户ID',
    title VARCHAR(200) NOT NULL COMMENT '反馈标题',
    content TEXT COMMENT '反馈内容',
    attachments TEXT COMMENT '附件JSON',
    feedback_type VARCHAR(20) DEFAULT 'SUMMARY' COMMENT '反馈类型',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-草稿, 1-已提交',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_activity (activity_id),
    INDEX idx_registration (registration_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反馈表';

-- 创建上传文件表
CREATE TABLE IF NOT EXISTS uploaded_files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stored_name VARCHAR(255) NOT NULL UNIQUE COMMENT '磁盘文件名',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    uploader_id BIGINT NOT NULL COMMENT '上传人ID',
    owner_type VARCHAR(30) NOT NULL DEFAULT 'TEMP' COMMENT '归属类型: TEMP/ACTIVITY/REGISTRATION/FEEDBACK/USER',
    owner_id BIGINT COMMENT '归属业务ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_uploader (uploader_id),
    INDEX idx_owner (owner_type, owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上传文件表';

-- 创建活动分组表
CREATE TABLE IF NOT EXISTS activity_groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    group_name VARCHAR(100) NOT NULL COMMENT '分组名称',
    description VARCHAR(500) COMMENT '分组描述',
    member_count INT DEFAULT 0 COMMENT '成员数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_activity (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动分组表';

-- 创建学校名称标准化表
CREATE TABLE IF NOT EXISTS school_names (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    standard_name VARCHAR(100) NOT NULL UNIQUE COMMENT '标准学校名称',
    aliases VARCHAR(500) COMMENT '别名',
    usage_count INT DEFAULT 1 COMMENT '使用次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (standard_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学校名称标准化表';

-- =====================================================
-- 初始化演示数据
-- =====================================================

-- 插入演示用户 (密码都是123456的SHA-256哈希)
INSERT INTO users (username, password, real_name, email, phone, role, student_no, school, department, major, grade, gpa, status) VALUES
('admin', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '系统管理员', 'admin@school.edu', '13800000000', 'SCHOOL', 'A001', '示范大学', '招生办', NULL, NULL, NULL, 1),
('teacher', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '张老师', 'teacher@school.edu', '13800000001', 'TEACHER', 'T001', '示范大学', '计算机学院', NULL, NULL, NULL, 1),
('student', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '李同学', 'student@school.edu', '13800000002', 'STUDENT', '2021001', '示范大学', '计算机学院', '软件工程', '大三', 3.80, 1),
('student2', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '王同学', 'student2@school.edu', '13800000003', 'STUDENT', '2021002', '示范大学', '计算机学院', '计算机科学', '大二', 3.50, 1),
('student3', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '赵同学', 'student3@school.edu', '13800000004', 'STUDENT', '2022001', '示范大学', '数学学院', '数学与应用数学', '大一', 3.90, 1);

-- 插入演示活动
INSERT INTO activities (title, summary, content, activity_type, signup_start_time, signup_end_time, activity_start_time, activity_end_time, location, max_participants, required_gpa, need_approval, is_banner, registration_config, feedback_start_time, feedback_end_time, status, creator_id) VALUES
('2024年暑期招生宣传志愿者招募', '招募优秀学生参与学校暑期招生宣传工作，前往各地高中进行招生宣传。', '<h3>活动简介</h3><p>本次活动旨在招募优秀在校学生作为招生宣传志愿者，利用暑假时间返回家乡高中进行招生宣传工作。</p><h3>工作内容</h3><ul><li>向高中生介绍学校情况</li><li>解答学生和家长咨询</li><li>发放招生宣传资料</li><li>组织招生宣讲会</li></ul><h3>报名要求</h3><ol><li>大二及以上年级学生</li><li>绩点3.0以上</li><li>沟通能力强，形象气质佳</li></ol>', 'OFFLINE', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 60 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), '各省市高中', 100, 3.00, 1, 1, '[{"key":"targetSchool","label":"招生学校","required":true},{"key":"contactInfo","label":"联系方式","required":true},{"key":"remark","label":"补充说明","required":false},{"key":"attachments","label":"报名附件","required":false}]', DATE_ADD(NOW(), INTERVAL 60 DAY), DATE_ADD(NOW(), INTERVAL 97 DAY), 1, 2),
('线上招生咨询会', '面向高考考生及家长的在线招生咨询活动，由各学院老师和优秀学生代表在线答疑。', '<h3>活动介绍</h3><p>本次线上咨询会将通过网络直播的形式，为广大考生和家长提供全方位的招生政策解读和专业咨询服务。</p><h3>参与方式</h3><p>志愿者需要在线值班，回答考生和家长的各类问题。</p>', 'ONLINE', NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY), DATE_ADD(NOW(), INTERVAL 20 DAY), DATE_ADD(NOW(), INTERVAL 21 DAY), '线上平台', 50, 3.50, 1, 1, '[{"key":"contactInfo","label":"联系方式","required":true},{"key":"remark","label":"值班说明","required":true}]', DATE_ADD(NOW(), INTERVAL 20 DAY), DATE_ADD(NOW(), INTERVAL 23 DAY), 1, 2),
('校园开放日志愿者招募', '招募校园开放日志愿者，负责接待来访考生和家长，展示校园风采。', '<h3>活动说明</h3><p>校园开放日是学校面向社会公众开放参观的重要活动，需要大量志愿者参与接待工作。</p><h3>志愿者职责</h3><ul><li>校园导览讲解</li><li>咨询点值班答疑</li><li>活动秩序维护</li><li>宣传资料发放</li></ul>', 'OFFLINE', NOW(), DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 25 DAY), DATE_ADD(NOW(), INTERVAL 26 DAY), '学校主校区', 200, 2.50, 0, 0, '[{"key":"contactInfo","label":"联系方式","required":true}]', DATE_ADD(NOW(), INTERVAL 25 DAY), DATE_ADD(NOW(), INTERVAL 33 DAY), 1, 2);

-- 插入常用学校名称
INSERT INTO school_names (standard_name, aliases, usage_count) VALUES
('清华大学', '清华', 100),
('北京大学', '北大', 100),
('武汉大学', '武大', 80),
('武汉理工大学', '武汉理工,理工大', 60),
('华中科技大学', '华科,华中科技', 70),
('复旦大学', '复旦', 90),
('上海交通大学', '上交,交大', 85),
('浙江大学', '浙大', 88),
('南京大学', '南大', 75),
('中山大学', '中大', 70);

-- 插入演示报名记录
INSERT INTO registrations (activity_id, user_id, target_school, contact_info, remark, approval_status, group_id) VALUES
(1, 3, '武汉理工大学', '13800000002', '我是武汉理工大学的学生，希望回母校进行招生宣传。', 'APPROVED', NULL),
(1, 4, '华中科技大学', '13800000003', '希望能参加招生宣传活动。', 'PENDING', NULL),
(2, 3, NULL, '13800000002', '对线上咨询很感兴趣。', 'APPROVED', NULL);

SELECT '数据库初始化完成！' AS message;
