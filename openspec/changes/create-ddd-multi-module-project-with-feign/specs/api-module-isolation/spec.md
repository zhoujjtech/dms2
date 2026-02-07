# Spec: API模块隔离规范

## ADDED Requirements

### Requirement: API模块独立依赖性
API模块 SHALL 仅依赖基础库和工具库， SHALL NOT 依赖任何业务实现模块（domain、application、infrastructure、interface）。

**允许的依赖**：
- JDK标准库（java.*、javax.*）
- Lombok
- Spring Web（spring-boot-starter-web，仅用于注解）
- Spring Cloud OpenFeign（仅用于注解）
- Jackson（用于JSON序列化）
- Jakarta Validation（用于参数校验）
- Apache Commons Lang3 / Guava（工具类）
- Swagger/SpringDoc注解（用于API文档）

**禁止的依赖**：
- 项目内部业务模块（project-domain、project-application等）
- 数据持久化框架（MyBatis、JPA等）
- 业务相关的第三方SDK

#### Scenario: API模块依赖检查通过
- **WHEN** 开发人员在api模块的pom.xml中查看依赖列表
- **THEN** 系统 SHALL 只包含允许的依赖（如lombok、spring-web、validation等）
- **AND** 系统 SHALL NOT 包含任何项目内部的业务模块依赖

#### Scenario: API模块尝试引入业务依赖导致构建失败
- **WHEN** 开发人员在api模块的pom.xml中添加对project-domain的依赖
- **THEN** Maven构建 SHALL 失败
- **AND** 错误信息 SHALL 提示：API模块不应依赖业务实现模块

### Requirement: API接口定义标准
API模块 SHALL 定义清晰的接口契约，包括请求DTO、响应DTO、API接口定义。

**接口定义要素**：
- **业务接口**：定义服务能力的Java接口（如UserService）
- **Feign Client接口**：继承业务接口，添加@FeignClient注解
- **请求DTO**：使用@Valid注解支持参数校验
- **响应DTO**：包装返回数据，包含code、message、data字段
- **错误码枚举**：定义API错误码和错误信息

#### Scenario: 业务接口定义服务能力
- **WHEN** 开发人员在api模块定义用户服务接口
- **THEN** 系统 SHALL 创建`UserService`接口
- **AND** 接口 SHALL 包含方法签名（如`UserDTO getUserById(Long id)`）
- **AND** 接口 SHALL 不包含任何实现逻辑

#### Scenario: Feign Client接口继承业务接口
- **WHEN** 开发人员在api模块定义Feign Client
- **THEN** Feign Client接口 SHALL 继承业务接口（如`public interface UserFeignClient extends UserService`）
- **AND** Feign Client SHALL 添加@FeignClient注解
- **AND** @FeignClient注解 SHALL 指定name属性（服务名）和path属性（API路径前缀）

#### Scenario: 请求DTO包含校验注解
- **WHEN** 开发人员在api模块定义创建用户请求DTO
- **THEN** 系统 SHALL 创建`CreateUserRequest`类
- **AND** 字段 SHALL 包含校验注解（如@NotNull、@Size、@Email等）
- **AND** DTO SHALL 使用javax.validation.constraints包下的注解

#### Scenario: 响应DTO包装返回数据
- **WHEN** 开发人员在api模块定义响应DTO
- **THEN** 系统 SHALL 创建统一的响应包装类（如`ApiResponse<T>`）
- **AND** 响应DTO SHALL 包含code（状态码）、message（消息）、data（数据）字段
- **AND** data字段 SHALL 支持泛型以适配不同类型的返回数据

### Requirement: API文档自动生成
API模块 SHALL 集成SpringDoc（OpenAPI 3.0）以自动生成API文档。

**文档注解**：
- 在DTO上使用@Schema注解描述字段含义
- 在接口上使用@Tag注解分组
- 在操作上使用@Operation注解描述功能

#### Scenario: DTO包含@Schema注解
- **WHEN** 开发人员在api模块定义用户DTO
- **THEN** DTO字段 SHALL 使用@Schema注解添加描述
- **EXAMPLE**: `@Schema(description = "用户ID") private Long id;`

#### Scenario: API接口包含@Tag和@Operation注解
- **WHEN** 开发人员在Feign Client接口上添加注解
- **THEN** 接口 SHALL 使用@Tag注解指定分组（如`@Tag(name = "用户管理", description = "用户相关接口")`）
- **AND** 方法 SHALL 使用@Operation注解描述功能（如`@Operation(summary = "根据ID查询用户")`）

#### Scenario: 访问Swagger UI查看API文档
- **WHEN** 系统启动后访问`/swagger-ui/index.html`
- **THEN** 系统 SHALL 展示所有API接口的文档
- **AND** 文档 SHALL 包含接口路径、请求参数、响应示例、字段描述

### Requirement: API版本管理
API模块 SHALL 遵循Semantic Versioning（语义化版本）规范，版本号格式为`MAJOR.MINOR.PATCH`。

**版本规则**：
- **MAJOR（主版本）**：不兼容的API变更
- **MINOR（次版本）**：向后兼容的功能新增
- **PATCH（修订版）**：向后兼容的问题修复

**兼容性判断**：
- 新增接口：MINOR版本升级
- 修改接口：根据变更程度判断（兼容修改为MINOR，不兼容修改为MAJOR）
- 删除接口：MAJOR版本升级

#### Scenario: 新增接口升级MINOR版本
- **WHEN** 开发人员在现有API模块中添加新的接口方法
- **THEN** 系统 SHALL 升级MINOR版本号（如1.0.0 → 1.1.0）
- **AND** MAJOR和PATCH版本号 SHALL 保持不变

#### Scenario: 不兼容修改升级MAJOR版本
- **WHEN** 开发人员修改现有接口的方法签名（如删除必需参数）
- **THEN** 系统 SHALL 升级MAJOR版本号（如1.0.0 → 2.0.0）
- **AND** MINOR和PATCH版本号 SHALL 重置为0

#### Scenario: Bug修复升级PATCH版本
- **WHEN** 开发人员修复API的Bug但保持接口兼容
- **THEN** 系统 SHALL 升级PATCH版本号（如1.0.0 → 1.0.1）
- **AND** MAJOR和MINOR版本号 SHALL 保持不变

### Requirement: API变更兼容性保证
API模块 SHALL 保证向后兼容性，除非明确升级MAJOR版本。

**兼容变更（允许在MINOR/PATCH版本中）**：
- 新增接口
- 新增可选参数（带默认值）
- 新增响应字段
- 扩展枚举值
- 添加可选的请求头

**不兼容变更（必须升级MAJOR版本）**：
- 删除接口
- 修改接口路径
- 删除必需参数
- 修改参数类型（不兼容的变更）
- 修改响应字段类型（不兼容的变更）
- 删除响应字段
- 修改错误码

#### Scenario: 新增可选参数保持兼容
- **WHEN** 开发人员在现有接口方法中新增一个可选参数（带默认值）
- **THEN** 系统 SHALL 允许在MINOR版本中发布此变更
- **AND** 旧版本客户端 SHALL 仍可正常调用该接口

#### Scenario: 删除接口必须升级MAJOR版本
- **WHEN** 开发人员计划删除一个已发布的接口方法
- **THEN** 系统 SHALL 先标记接口为@Deprecated
- **AND** 系统 SHALL 至少保留一个大版本
- **AND** 系统 SHALL 在下一个MAJOR版本中才删除该接口

#### Scenario: 修改必需参数导致不兼容
- **WHEN** 开发人员将现有接口的必需参数改为可选，或反之
- **THEN** 系统 SHALL 判定为不兼容变更
- **AND** 系统 SHALL 升级MAJOR版本号

### Requirement: API模块发布到Maven仓库
API模块 SHALL 能够独立打包并发布到Maven仓库，供其他项目引用。

**发布流程**：
1. 执行`mvn clean deploy`将api模块发布到Maven私服
2. 其他项目在pom.xml中添加api模块依赖
3. 其他项目可以通过@Autowired注入Feign Client

#### Scenario: API模块独立打包
- **WHEN** 开发人员在api模块目录下执行`mvn clean package`
- **THEN** 系统 SHALL 生成jar文件（如project-api-1.0.0.jar）
- **AND** jar文件 SHALL 包含所有DTO、接口、Feign Client类
- **AND** jar文件 SHALL NOT 包含业务实现类

#### Scenario: API模块发布到Maven私服
- **WHEN** 开发人员执行`mvn clean deploy`
- **THEN** 系统 SHALL 将api模块的jar文件上传到Maven私服（如Nexus）
- **AND** 系统 SHALL 生成Maven元数据（maven-metadata.xml）

#### Scenario: 其他项目引用API模块
- **WHEN** 其他项目的pom.xml中添加api模块依赖
- **THEN** 系统 SHALL 从Maven仓库下载api模块的jar文件
- **AND** 其他项目 SHALL 可以通过@Autowired注入Feign Client
- **AND** 其他项目 SHALL 可以使用API模块定义的DTO

### Requirement: API模块配置可插拔
API模块 SHALL 支持配置外部化，允许消费方通过配置文件自定义Feign Client行为。

**可配置项**：
- 服务名称（@FeignClient的name属性）
- 请求超时时间
- 日志级别
- 是否启用熔断

#### Scenario: 服务名称可通过配置覆盖
- **WHEN** 消费方在application.yml中配置服务名称
- **THEN** Feign Client SHALL 使用配置的服务名称而非默认值
- **EXAMPLE**: `service.name.user-service: user-service-prod`

#### Scenario: 超时时间可配置
- **WHEN** 消费方在application.yml中配置Feign超时时间
- **THEN** Feign Client SHALL 使用配置的超时时间
- **AND** 配置项 SHALL 支持`connectTimeout`和`readTimeout`

#### Scenario: 日志级别可配置
- **WHEN** 消费方在application.yml中配置Feign日志级别
- **THEN** Feign Client SHALL 使用配置的日志级别（NONE、BASIC、HEADERS、FULL）
- **AND** 默认级别 SHALL 为BASIC

### Requirement: API模块降级处理
API模块 SHALL 支持Feign的降级机制（Fallback），当服务调用失败时执行降级逻辑。

**降级实现**：
- 在api模块中定义Fallback类
- Fallback类实现Feign Client接口
- 在@FeignClient注解中指定fallback属性

#### Scenario: 定义Fallback类
- **WHEN** 开发人员在api模块创建UserFeignClientFallback类
- **THEN** Fallback类 SHALL 实现UserFeignClient接口
- **AND** Fallback类 SHALL 使用@Component注解
- **AND** Fallback方法 SHALL 返回默认值或抛出降级异常

#### Scenario: Feign Client启用降级
- **WHEN** @FeignClient注解指定fallback属性
- **THEN** 系统 SHALL 在服务调用失败时执行Fallback方法
- **AND** 降级 SHALL 在以下场景触发：连接超时、读取超时、服务不可用

#### Scenario: 降级可配置开关
- **WHEN** 消费方在配置中启用或禁用降级
- **THEN** 系统 SHALL 根据配置决定是否启用Fallback
- **AND** 配置项 SHALL 为`feign.circuitbreaker.enabled`（默认为false）

### Requirement: API模块测试支持
API模块 SHALL 提供Mock实现，便于消费方进行单元测试。

**测试支持**：
- 提供Mockito的Mock实现
- 提供测试用的Builder工具类
- 提供测试数据常量

#### Scenario: 消费方Mock Feign Client进行测试
- **WHEN** 消费方编写单元测试
- **THEN** 消费方 SHALL 可以使用@MockBean创建Feign Client的Mock对象
- **AND** Mock对象 SHALL 可以通过Mockito的when/then语法定义行为

#### Scenario: 提供Builder工具类简化测试数据构造
- **WHEN** 开发人员在api模块提供DTO的Builder类
- **THEN** 消费方 SHALL 可以通过Builder快速构造测试数据
- **EXAMPLE**: `UserDTO.builder().id(1L).name("Alice").build()`
