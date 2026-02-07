# DMS2 Multi-Module Project

基于DDD（领域驱动设计）架构的多模块Spring Boot项目，支持独立部署和API模块引用。

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

## 快速开始

### 前置条件

- JDK 17+
- Maven 3.6+
- Nacos Server（可选，本地开发可不启动）

### 构建项目

```bash
# 克隆项目
cd dms2-project

# 编译项目
mvn clean install

# 打包项目
mvn clean package
```

### 启动应用

#### 方式1：独立部署

```bash
# 启动interface模块
cd dms2-interface
mvn spring-boot:run

# 或者直接运行JAR
java -jar dms2-interface/target/dms2-interface-1.0.0-SNAPSHOT.jar
```

#### 方式2：IDE启动

运行 `dms2-interface/src/main/java/com/example/dms2/infrastructure/Application.java`

### 访问应用

- **应用地址**: http://localhost:8080
- **健康检查**: http://localhost:8081/actuator/health
- **API文档**: http://localhost:8080/swagger-ui/index.html
- **Prometheus指标**: http://localhost:8081/actuator/prometheus

## API使用示例

### 方式1：直接调用REST API

```bash
# 创建用户
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "realName": "Alice"
  }'

# 查询用户
curl http://localhost:8080/api/users/1

# 分页查询
curl -X POST http://localhost:8080/api/users/page \
  -H "Content-Type: application/json" \
  -d '{
    "pageNum": 1,
    "pageSize": 10
  }'
```

### 方式2：在其他项目中引用API模块

1. 在消费方项目的pom.xml中添加依赖：

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>dms2-api</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

2. 启用Feign Client：

```java
@SpringBootApplication
@EnableFeignClients(basePackages = {"com.example.dms2.api.feign"})
public class ConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
```

3. 注入并使用Feign Client：

```java
@Service
public class ConsumerService {
    private final UserFeignClient userFeignClient;

    public ConsumerService(UserFeignClient userFeignClient) {
        this.userFeignClient = userFeignClient;
    }

    public UserDTO getUser(Long id) {
        ApiResponse<UserDTO> response = userFeignClient.getUserById(id);
        return response.getData();
    }
}
```

## 配置说明

### application.yml配置项

```yaml
# 服务端口
server:
  port: 8080

# Nacos配置
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848  # Nacos地址
        namespace: dev               # 命名空间
        group: DEFAULT_GROUP         # 分组

# Feign配置
feign:
  client:
    config:
      default:
        connectTimeout: 5000        # 连接超时（毫秒）
        readTimeout: 30000          # 读取超时（毫秒）
        loggerLevel: BASIC          # 日志级别
```

## DDD分层说明

### API模块（dms2-api）
- **职责**: 定义接口契约，可被外部项目依赖
- **包含**: DTO、Feign Client、API配置
- **依赖**: 仅依赖基础库，不依赖业务模块

### Domain模块（dms2-domain）
- **职责**: 核心业务逻辑，领域模型
- **包含**: 实体、值对象、领域服务、仓储接口
- **依赖**: 仅依赖API模块

### Application模块（dms2-application）
- **职责**: 用例编排，DTO转换
- **包含**: 应用服务、DTO转换器
- **依赖**: Domain模块、API模块

### Infrastructure模块（dms2-infrastructure）
- **职责**: 技术实现，外部集成
- **包含**: 仓储实现、外部客户端
- **依赖**: Domain模块

### Interface模块（dms2-interface）
- **职责**: 对外接口，启动入口
- **包含**: REST控制器、配置、启动类
- **依赖**: Application、Infrastructure、API模块

## 测试

```bash
# 运行所有测试
mvn test

# 运行单个模块测试
mvn test -pl dms2-domain

# 运行集成测试
mvn verify
```

## 部署

### Docker部署

```bash
# 构建镜像
docker build -t dms2-service:1.0.0 .

# 运行容器
docker run -d -p 8080:8080 -p 8081:8081 \
  -e NACOS_SERVER=nacos:8848 \
  dms2-service:1.0.0
```

### Kubernetes部署

```bash
# 部署到K8s
kubectl apply -f k8s/
```

## 开发规范

### 命名约定

- **实体**: `User`、`Order`
- **DTO**: `UserDTO`、`CreateUserRequest`
- **仓储接口**: `UserRepository`
- **仓储实现**: `UserRepositoryImpl`
- **应用服务**: `UserAppService`
- **领域服务**: `UserDomainService`
- **控制器**: `UserController`
- **Feign Client**: `UserFeignClient`

### 分层规则

- **禁止**: 上层模块依赖下层模块（如Domain依赖Application）
- **必须**: Controller → AppService → Repository → Domain
- **依赖倒置**: Domain定义接口，Infrastructure实现

## 版本管理

API模块遵循[语义化版本](https://semver.org/)：
- **MAJOR**: 不兼容的API变更
- **MINOR**: 向后兼容的功能新增
- **PATCH**: 向后兼容的问题修复

## 许可证

Apache License 2.0

## 联系方式

- 项目地址: https://github.com/example/dms2-project
- 问题反馈: https://github.com/example/dms2-project/issues
