# Proposal: 创建支持多模式部署的DDD多模块项目

## Why

当前需要构建一个基于DDD（领域驱动设计）架构的新项目，该项目需要具备灵活的部署和使用模式：既可以作为独立应用通过Nacos注册中心提供HTTP API服务，也可以作为Maven模块被其他业务项目依赖引用。当被其他项目引用时，通过OpenFeign实现服务间调用，避免代码重复并保持服务边界清晰。

这种架构设计解决了以下问题：
1. **服务复用性**：核心领域能力可以被多个业务系统共享，避免重复开发
2. **部署灵活性**：根据业务需求选择独立部署或嵌入式部署
3. **技术栈统一**：基于Spring Cloud Alibaba生态，利用Nacos实现服务注册发现
4. **架构清晰性**：采用DDD分层，确保领域逻辑纯净，易于维护和扩展

## What Changes

### 项目结构变更
- 创建一个新的Maven多模块项目，采用父POM + 子模块结构
- 建立4个核心子模块：
  - `xxx-api`: API接口定义模块（可被外部项目依赖）
  - `xxx-application`: 应用服务层模块
  - `xxx-domain`: 领域层模块
  - `xxx-infrastructure`: 基础设施层模块
  - `xxx-interface`: 用户接口层模块（包含启动类，可独立运行）

### API模块能力
- **接口定义**: 暴露DTO、VO、API接口定义
- **Feign Client集成**: 提供开箱即用的OpenFeign客户端
- **独立依赖**: 其他项目只需引入`xxx-api`依赖即可使用服务

### 部署模式支持
- **独立部署模式**: 启动`xxx-interface`模块，通过Nacos注册提供服务
- **嵌入式模式**: 其他项目引入`xxx-api`依赖，通过Feign调用（服务提供方仍需独立部署）

### 技术栈集成
- JDK 17
- Spring Boot 3.x
- Spring Cloud Alibaba 2022.x+
- Nacos (服务注册与发现)
- Maven多模块管理
- OpenFeign (声明式HTTP客户端)

## Capabilities

### New Capabilities

#### `ddd-project-structure`
建立标准的DDD分层架构和多模块项目结构，包含：
- **领域层（domain）**：实体、值对象、领域服务、领域事件、仓储接口
- **应用层（application）**：应用服务、DTO转换、用例编排
- **基础设施层（infrastructure）**：仓储实现、外部服务集成、持久化
- **接口层（interface）**：REST控制器、启动类、配置
- **API模块（api）**：跨项目接口定义、Feign Client、DTO/VO

**覆盖范围**：
- Maven父子模块POM配置
- 模块间依赖关系管理
- 分层架构的代码组织规范
- 包结构和命名约定

#### `api-module-isolation`
实现API模块的独立引用能力，支持：
- **接口定义标准化**：定义清晰的API接口契约（请求/响应DTO）
- **模块独立性**：API模块不依赖业务实现，仅依赖基础库
- **版本管理**：通过Semantic Versioning管理API兼容性
- **文档生成**：自动生成API文档（Swagger/OpenAPI）

**覆盖范围**：
- API模块的依赖隔离设计
- 公共DTO/VO定义
- API接口规范和注解
- 依赖传递配置

#### `feign-client-integration`
集成OpenFeign实现服务间调用，包含：
- **Feign Client自动配置**：可插拔的Feign客户端配置
- **服务发现集成**：通过Nacos动态发现服务地址
- **负载均衡**：集成Spring Cloud LoadBalancer
- **熔断降级**：集成Sentinel实现熔断保护（可选）
- **日志与监控**：请求日志记录和性能监控

**覆盖范围**：
- Feign Client接口定义
- Feign配置类（超时、日志、拦截器）
- Nacos服务发现配置
- 熔断降级策略

### Modified Capabilities

*无（新项目，不存在现有规范变更）*

## Impact

### 技术影响
- **新增依赖**：
  - Spring Boot 3.x
  - Spring Cloud Alibaba 2022.x+
  - Nacos Client
  - OpenFeign
  - Spring Cloud LoadBalancer
  - (可选) Sentinel

- **构建工具**：Maven多模块配置，需要定义父子POM关系

- **代码结构**：引入DDD分层架构，需要团队理解并遵循分层规范

### 开发流程影响
- **开发规范**：需要制定DDD分层开发规范和最佳实践文档
- **代码审查**：需要检查模块依赖关系，确保分层不越界
- **测试策略**：单元测试（领域层）、集成测试（应用层）、端到端测试（接口层）

### 部署与运维影响
- **部署选项**：支持独立部署（Spring Boot应用）和嵌入式部署（作为依赖）
- **服务注册**：需要部署和维护Nacos服务器
- **监控观测**：需要集成Spring Boot Actuator和链路追踪（如SkyWalking）
- **配置管理**：通过Nacos Config实现配置中心化（可选）

### 团队协作影响
- **知识要求**：团队需要学习DDD概念和Spring Cloud Alibaba生态
- **接口管理**：API变更需要通知所有消费方，遵循兼容性原则
- **版本发布**：API模块独立版本管理，需要制定发版流程

### 外部依赖
- **Nacos Server**：需要预先部署Nacos服务（单机或集群模式）
- **Maven仓库**：需要私服（如Nexus）来发布API模块供其他项目引用

### 兼容性
- **JDK版本**：要求JDK 17+
- **Spring Boot**：要求3.x版本（与Spring Cloud Alibaba 2022.x+兼容）
- **消费方要求**：引用API模块的项目也需要使用JDK 17和兼容的Spring Boot版本

## 非目标（Out of Scope）

本变更不包含以下内容（可作为后续增强）：
- 具体的业务领域实现（如用户管理、订单系统等）
- 数据库设计与持久化细节（将在infrastructure层实现）
- 安全认证与授权（如OAuth2、JWT等）
- API网关集成（如Spring Cloud Gateway）
- 分布式事务解决方案（如Seata）
- 消息队列集成（如RocketMQ、Kafka）
- 缓存策略（如Redis）
- 国际化（i18n）支持

这些内容可以在后续的变更中逐步添加。
