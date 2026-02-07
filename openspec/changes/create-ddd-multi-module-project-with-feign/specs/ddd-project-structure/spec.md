# Spec: DDD项目结构规范

## ADDED Requirements

### Requirement: Maven多模块项目结构
系统 SHALL 使用Maven父POM加5个子模块的层级结构组织代码，每个子模块对应DDD的一个层次。

**模块划分**：
- `project-api`: API接口定义模块，可被外部项目依赖
- `project-domain`: 领域层模块，包含实体、值对象、领域服务、仓储接口
- `project-application`: 应用服务层模块，包含应用服务、DTO转换、用例编排
- `project-infrastructure`: 基础设施层模块，包含仓储实现、外部服务集成、持久化
- `project-interface`: 接口层模块，包含REST控制器、启动类、配置

#### Scenario: 父POM包含所有子模块声明
- **WHEN** 开发人员查看父POM的`<modules>`元素
- **THEN** 系统 SHALL 声明所有5个子模块：api、domain、application、infrastructure、interface

#### Scenario: 每个子模块都有独立的pom.xml
- **WHEN** 开发人员在任意子模块目录下执行`mvn clean package`
- **THEN** 系统 SHALL 成功编译并打包该子模块
- **AND** 子模块的`<parent>`元素 SHALL 指向父POM

### Requirement: 严格的模块依赖方向
系统 SHALL 强制执行单向依赖关系，上层模块可以依赖下层模块，但下层模块不能依赖上层模块。

**依赖方向规则**：
- `interface` → `application` → `domain`
- `interface` → `infrastructure`
- `infrastructure` → `domain`（通过依赖倒置实现）
- `api` 模块独立，不依赖任何业务模块

#### Scenario: domain模块不依赖application模块
- **WHEN** 开发人员在domain模块中引用application模块的类
- **THEN** Maven构建 SHALL 失败并报错：模块依赖违规
- **AND** 编译器 SHALL 提示找不到application模块的类

#### Scenario: api模块不依赖任何业务模块
- **WHEN** 开发人员在api模块的pom.xml中添加对domain/application/infrastructure/interface的依赖
- **THEN** Maven Enforcer Plugin SHALL 检测到依赖违规并构建失败

#### Scenario: infrastructure模块实现domain定义的接口
- **WHEN** infrastructure模块需要访问领域实体
- **THEN** infrastructure模块 SHALL 只能通过domain模块定义的接口（如Repository接口）访问领域对象
- **AND** infrastructure模块的pom.xml SHALL 包含对domain模块的依赖

### Requirement: DDD分层包结构
系统 SHALL 按照DDD分层组织包结构，每个层使用明确的包名和子包组织代码。

**标准包结构**：
```
com.example.projectname
├── api/
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   ├── vo/
│   ├── feign/
│   │   └── fallback/
│   ├── constant/
│   └── enums/
├── domain/
│   ├── model/
│   │   ├── entity/
│   │   ├── valueobject/
│   │   └── aggregate/
│   ├── service/
│   ├── repository/
│   ├── event/
│   ├── exception/
│   └── specification/
├── application/
│   ├── service/
│   ├── command/
│   ├── query/
│   ├── handler/
│   └── assembler/
├── infrastructure/
│   ├── repository/
│   ├── client/
│   ├── config/
│   ├── persistence/
│   └── messaging/
└── interface/
    ├── rest/
    ├── config/
    ├── interceptor/
    └── filter/
```

#### Scenario: domain层包含所有领域对象
- **WHEN** 开发人员在domain模块下创建新的实体
- **THEN** 实体类 SHALL 放在`domain/model/entity/`包下
- **AND** 实体类 SHALL 使用@Entity注解（如果是JPA实体）或无注解（纯领域对象）

#### Scenario: application层包含应用服务
- **WHEN** 开发人员在application模块下创建应用服务
- **THEN** 应用服务类 SHALL 放在`application/service/`包下
- **AND** 应用服务类 SHALL 命名为`XxxAppService`或`XxxService`
- **AND** 应用服务类 SHALL 使用@Service注解

#### Scenario: infrastructure层包含仓储实现
- **WHEN** 开发人员在infrastructure模块下实现仓储
- **THEN** 仓储实现类 SHALL 放在`infrastructure/repository/`包下
- **AND** 仓储实现类 SHALL 命名为`XxxRepositoryImpl`
- **AND** 仓储实现类 SHALL 实现domain模块定义的`XxxRepository`接口

### Requirement: 统一的命名约定
系统 SHALL 使用一致的命名约定来命名类、接口、包，以提高代码可读性。

**命名约定**：
- **实体**：`XxxEntity` 或直接 `Xxx`（如User、Order）
- **值对象**：`XxxVO`（如AddressVO、MoneyVO）
- **聚合根**：`Xxx` 或 `XxxAggregate`（如User、Order）
- **领域服务**：`XxxDomainService`
- **仓储接口**：`XxxRepository`
- **仓储实现**：`XxxRepositoryImpl`
- **应用服务**：`XxxAppService` 或 `XxxService`
- **DTO（请求）**：`XxxRequest`
- **DTO（响应）**：`XxxResponse`
- **Feign Client**：`XxxFeignClient`
- **控制器**：`XxxController`

#### Scenario: 实体类命名规范
- **WHEN** 开发人员创建新的领域实体
- **THEN** 实体类名 SHALL 遵循`XxxEntity`或`Xxx`格式
- **AND** 实体类名 SHALL 使用名词单数形式（如User而非Users）

#### Scenario: DTO命名规范
- **WHEN** 开发人员创建API请求DTO
- **THEN** DTO类名 SHALL 以`Request`结尾（如CreateUserRequest）
- **AND** 请求DTO SHALL 放在`api/dto/request/`包下

#### Scenario: 仓储接口命名规范
- **WHEN** 开发人员创建新的仓储接口
- **THEN** 接口名 SHALL 以`Repository`结尾（如UserRepository）
- **AND** 接口 SHALL 放在`domain/repository/`包下

### Requirement: 父POM统一依赖管理
系统 SHALL 在父POM的`<dependencyManagement>`元素中统一管理所有依赖版本，子模块通过`<dependency>`元素引用时不指定版本。

#### Scenario: 子模块不指定依赖版本
- **WHEN** 子模块的pom.xml中添加Spring Boot Starter依赖
- **THEN** 依赖声明 SHALL 不包含`<version>`元素
- **AND** 版本 SHALL 由父POM的`<dependencyManagement>`提供

#### Scenario: 所有子模块使用相同版本的核心依赖
- **WHEN** 多个子模块引入同一个依赖（如lombok）
- **THEN** 系统 SHALL 确保所有子模块使用完全相同的版本号
- **AND** 版本号 SHALL 在父POM中定义一次

### Requirement: Maven构建配置
系统 SHALL 配置必要的Maven插件以确保代码质量和构建标准化。

**必需插件**：
- **maven-compiler-plugin**: 指定JDK 17编译
- **maven-enforcer-plugin**: 检测循环依赖和依赖冲突
- **maven-surefire-plugin**: 运行单元测试
- **maven-failsafe-plugin**: 运行集成测试

#### Scenario: 编译插件配置JDK 17
- **WHEN** 开发人员执行`mvn clean compile`
- **THEN** maven-compiler-plugin SHALL 使用JDK 17编译源代码
- **AND** 编译器 SHALL 设置`-parameters`参数以保留参数名

#### Scenario: Enforcer插件检测循环依赖
- **WHEN** 开发人员引入循环依赖（如A依赖B，B依赖A）
- **THEN** maven-enforcer-plugin SHALL 检测到循环依赖
- **AND** 构建过程 SHALL 失败并报错：Circular dependency detected

#### Scenario: 测试插件自动运行测试
- **WHEN** 开发人员执行`mvn clean package`
- **THEN** maven-surefire-plugin SHALL 自动运行所有单元测试
- **AND** 如果有测试失败，构建过程 SHALL 失败

### Requirement: 项目启动类位置
系统 SHALL 将Spring Boot启动类放在`interface`模块中，且启动类 SHALL 位于接口层的根包下。

#### Scenario: 启动类位于interface模块
- **WHEN** 开发人员查看interface模块的包结构
- **THEN** 系统 SHALL 包含一个带有@SpringBootApplication注解的启动类
- **AND** 启动类 SHALL 命名为`Application.java`或`XxxApplication.java`
- **AND** 启动类 SHALL 放在`com.example.projectname`包下（interface模块的根包）

#### Scenario: 启动类扫描所有组件
- **WHEN** 系统启动
- **THEN** @SpringBootApplication注解的@ComponentScan SHALL 扫描所有模块的组件（domain、application、infrastructure、interface）
- **AND** 系统 SHALL 成功启动并加载所有Spring Bean

### Requirement: 架构守护测试
系统 SHALL 包含ArchUnit测试以在编译期强制执行架构规则，防止分层违规。

**架构规则**：
- domain层不应依赖application、infrastructure、interface层
- application层不应依赖infrastructure、interface层
- api模块不应依赖任何业务模块

#### Scenario: ArchUnit测试检测分层违规
- **WHEN** 开发人员在domain层引入application层的类
- **AND** 执行ArchUnit测试
- **THEN** 测试 SHALL 失败并报错：Domain layer should not depend on Application layer

#### Scenario: ArchUnit测试作为CI/CD的一部分
- **WHEN** 开发人员提交代码到Git仓库
- **THEN** CI/CD流水线 SHALL 自动运行ArchUnit测试
- **AND** 如果测试失败，构建 SHALL 失败并阻止合并
