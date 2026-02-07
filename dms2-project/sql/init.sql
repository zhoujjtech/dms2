-- 创建数据库
CREATE DATABASE IF NOT EXISTS `dms2` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `dms2`;

-- 用户表
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `email`       VARCHAR(100) NOT NULL COMMENT '邮箱',
    `phone`       VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
    `real_name`   VARCHAR(50)           DEFAULT NULL COMMENT '真实姓名',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';

-- 插入测试数据
INSERT INTO `t_user` (`username`, `email`, `phone`, `real_name`)
VALUES ('admin', 'admin@example.com', '13800138000', '管理员'),
       ('alice', 'alice@example.com', '13800138001', 'Alice'),
       ('bob', 'bob@example.com', '13800138002', 'Bob');
