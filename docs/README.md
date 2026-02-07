# DMS2 文档中心

欢迎来到 DMS2 项目的文档中心。DMS2 是一个基于DDD（领域驱动设计）架构的多模块Spring Boot项目。

## 文档导航

### 快速开始
适合新用户快速了解和上手项目。

- [项目介绍](getting-started/README.md) - 项目概述、技术栈、架构说明
- [快速开始指南](getting-started/QUICK-START.md) - 5分钟快速上手
- [使用指南](getting-started/HOW-TO-USE.md) - 详细的操作说明

### 开发指南
面向开发人员的规范和最佳实践。

- [DDD开发标准](guides/DDD-DEVELOPMENT-STANDARD.md) - 领域驱动设计开发规范
- [DDD操作手册](guides/DDD-OPERATIONS-MANUAL.md) - 实际操作指导
- [代码风格](guides/CODE-STYLE.md) - Java代码规范和Checkstyle配置

### 项目报告
项目总结和测试报告。

- [项目总结](reports/PROJECT-SUMMARY.md) - 项目架构设计总结
- [测试报告](reports/TEST-REPORT.md) - 测试用例和覆盖情况

## 项目结构

```
dms2-project/
├── dms2-api/              # API接口定义模块（可被外部项目依赖）
├── dms2-domain/           # 领域层模块（实体、值对象、领域服务、仓储接口）
├── dms2-application/      # 应用服务层模块（应用服务、DTO转换、用例编排）
├── dms2-infrastructure/   # 基础设施层模块（仓储实现、外部服务集成）
└── dms2-interface/        # 接口层模块（REST控制器、启动类）
```

## 技术栈

- **JDK**: 17
- **Spring Boot**: 3.2.0
- **Spring Cloud Alibaba**: 2022.0.0.0
- **Nacos**: 服务注册与发现
- **OpenFeign**: 声明式HTTP客户端
- **SpringDoc**: API文档生成

## 快速链接

- **应用地址**: http://localhost:8080
- **健康检查**: http://localhost:8081/actuator/health
- **API文档**: http://localhost:8080/swagger-ui/index.html
- **Prometheus指标**: http://localhost:8081/actuator/prometheus
