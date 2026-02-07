# Spec: Feign客户端集成规范

## ADDED Requirements

### Requirement: Feign Client声明式定义
系统 SHALL 使用OpenFeign的声明式HTTP客户端模式，通过接口和注解定义服务调用。

**Feign Client定义**：
- 接口继承业务接口（如UserService）
- 添加@FeignClient注解
- 指定服务名称（name属性）
- 指定API路径前缀（path属性）

#### Scenario: Feign Client继承业务接口
- **WHEN** 开发人员创建Feign Client接口
- **THEN** Feign Client SHALL 继承业务接口（如`public interface UserFeignClient extends UserService`）
- **AND** Feign Client SHALL 不重复定义业务接口的方法

#### Scenario: @FeignClient注解指定服务名和路径
- **WHEN** 开发人员在Feign Client接口上添加@FeignClient注解
- **THEN** 注解 SHALL 包含name属性（服务名，如`user-service`）
- **AND** 注解 SHALL 包含path属性（路径前缀，如`/api/users`）
- **EXAMPLE**: `@FeignClient(name = "user-service", path = "/api/users")`

#### Scenario: 服务名支持配置化
- **WHEN** 开发人员使用占位符指定服务名
- **THEN** @FeignClient的name属性 SHALL 支持SpEL表达式（如`${service.name.user-service:user-service}`）
- **AND** 系统 SHALL 优先使用配置文件中的值，否则使用默认值

### Requirement: Nacos服务发现集成
Feign Client SHALL 通过Nacos实现服务发现，动态解析服务名到实际IP地址和端口。

**服务发现流程**：
1. Feign Client通过服务名（如user-service）发起调用
2. Spring Cloud LoadBalancer从Nacos获取服务实例列表
3. 根据负载均衡策略选择一个实例
4. 发起HTTP请求到选定的实例

#### Scenario: 从Nacos获取服务实例
- **WHEN** Feign Client调用服务接口
- **THEN** 系统 SHALL 从Nacos注册中心查询指定服务名的实例列表
- **AND** 系统 SHALL 过滤掉不健康的实例（健康检查失败）
- **AND** 如果没有可用实例，系统 SHALL 抛出异常

#### Scenario: 负载均衡选择实例
- **WHEN** Nacos返回多个服务实例
- **THEN** 系统 SHALL 使用Spring Cloud LoadBalancer选择一个实例
- **AND** 默认负载均衡策略 SHALL 为轮询（RoundRobin）
- **AND** 负载均衡策略 SHALL 可配置（如随机、权重等）

#### Scenario: 服务实例下线自动切换
- **WHEN** 当前调用的服务实例下线或网络故障
- **THEN** 系统 SHALL 自动从实例列表中移除该实例
- **AND** 系统 SHALL 选择另一个可用实例重试
- **AND** 重试次数 SHALL 可配置（默认为3次）

### Requirement: Feign配置类
系统 SHALL 提供Feign配置类，集中管理Feign Client的行为（超时、日志、拦截器等）。

**配置类要素**：
- RequestInterceptor（请求拦截器）
- Logger.Level（日志级别）
- Options（超时配置）
- Decoder（解码器）
- Encoder（编码器）

#### Scenario: 定义Feign配置类
- **WHEN** 开发人员创建Feign配置类
- **THEN** 配置类 SHALL 使用@Configuration注解
- **AND** 配置类 SHALL 放在api模块的config包下（如`api/config/FeignClientConfiguration`）

#### Scenario: 请求拦截器添加认证头
- **WHEN** 开发人员在配置类中定义RequestInterceptor Bean
- **THEN** 拦截器 SHALL 在每次Feign请求前执行
- **AND** 拦截器 SHALL 添加必要的HTTP头（如Authorization、X-Request-Id）
- **EXAMPLE**: `template.header("Authorization", "Bearer " + token);`

#### Scenario: 配置日志级别
- **WHEN** 开发人员在配置类中定义Logger.Level Bean
- **THEN** 系统 SHALL 使用指定的日志级别记录Feign请求
- **AND** 日志级别 SHALL 支持：NONE（无日志）、BASIC（基本信息）、HEADERS（请求头）、FULL（完整请求和响应）
- **AND** 默认级别 SHALL 为BASIC

#### Scenario: 配置超时时间
- **WHEN** 开发人员在配置类中定义Request.Options Bean
- **THEN** 系统 SHALL 使用指定的超时配置
- **AND** 超时配置 SHALL 包括连接超时（connectTimeout）和读取超时（readTimeout）
- **AND** 默认连接超时 SHALL 为5秒
- **AND** 默认读取超时 SHALL 为30秒

### Requirement: Feign配置外部化
系统 SHALL 支持通过配置文件（application.yml）自定义Feign行为，无需修改代码。

**可配置项**：
- `feign.client.config.default.*`：全局默认配置
- `feign.client.config.<serviceName>.*`：针对特定服务的配置

#### Scenario: 全局默认超时配置
- **WHEN** 开发人员在application.yml中配置`feign.client.config.default.connectTimeout`
- **THEN** 所有Feign Client SHALL 使用配置的超时时间
- **AND** 代码中的超时配置 SHALL 被配置文件覆盖

#### Scenario: 特定服务超时配置
- **WHEN** 开发人员配置`feign.client.config.user-service.readTimeout`
- **THEN** 只有名为user-service的Feign Client SHALL 使用该超时配置
- **AND** 其他Feign Client SHALL 使用全局默认配置

#### Scenario: 日志级别配置
- **WHEN** 开发人员配置`feign.client.config.default.logLevel`
- **THEN** 所有Feign Client SHALL 使用配置的日志级别
- **AND** 日志级别 SHALL 支持：none、basic、headers、full

### Requirement: Feign请求与响应处理
系统 SHALL 正确处理Feign的请求序列化和响应反序列化。

**序列化要求**：
- 请求对象 SHALL 使用JSON格式
- 响应对象 SHALL 使用JSON格式
- 日期类型 SHALL 使用ISO 8601格式
- 时区 SHALL 使用UTC

#### Scenario: 请求对象序列化为JSON
- **WHEN** 开发人员调用Feign Client方法并传递请求DTO
- **THEN** 系统 SHALL 使用Jackson将请求对象序列化为JSON
- **AND** HTTP Content-Type头 SHALL 设置为`application/json`

#### Scenario: 响应JSON反序列化为对象
- **WHEN** Feign Client收到HTTP响应
- **THEN** 系统 SHALL 使用Jackson将JSON响应反序列化为响应DTO
- **AND** 如果JSON格式错误，系统 SHALL 抛出解码异常

#### Scenario: 日期字段序列化格式
- **WHEN** 请求DTO包含LocalDateTime类型字段
- **THEN** 系统 SHALL 将日期序列化为ISO 8601格式（如`2024-01-01T12:00:00Z`）
- **AND** 时区 SHALL 使用UTC

#### Scenario: 泛型响应处理
- **WHEN** Feign Client方法返回`ApiResponse<UserDTO>`类型
- **THEN** 系统 SHALL 正确反序列化泛型类型
- **AND** data字段 SHALL 包含UserDTO对象

### Requirement: Feign异常处理
系统 SHALL 提供统一的异常处理机制，将Feign调用异常转换为业务异常。

**异常类型**：
- **FeignException.BadRequest**：HTTP 400错误
- **FeignException.Unauthorized**：HTTP 401错误
- **FeignException.NotFound**：HTTP 404错误
- **FeignException.InternalServerError**：HTTP 500错误
- **FeignException.FeignTimeoutException**：超时异常

#### Scenario: HTTP错误码映射到业务异常
- **WHEN** Feign Client调用返回HTTP 404
- **THEN** 系统 SHALL 抛出FeignException.NotFound
- **AND** 异常 SHALL 包含响应状态码和错误信息

#### Scenario: 超时异常处理
- **WHEN** Feign Client调用超时（连接超时或读取超时）
- **THEN** 系统 SHALL 抛出FeignTimeoutException
- **AND** 消费方 SHALL 可以捕获异常并执行降级逻辑

#### Scenario: 自定义错误解码器
- **WHEN** 开发人员实现ErrorDecoder接口
- **THEN** 系统 SHALL 使用自定义解码器处理Feign异常
- **AND** 解码器 SHALL 根据HTTP状态码返回不同类型的异常
- **AND** 解码器 SHALL 从响应体中提取错误信息

### Requirement: Feign熔断降级
系统 SHALL 集成Sentinel实现熔断降级，防止服务雪崩。

**熔断机制**：
- 当失败率达到阈值时，触发熔断
- 熔断后直接返回降级结果，不发起远程调用
- 经过一段时间后，尝试半开状态（允许少量请求通过）
- 如果半开状态成功，恢复到关闭状态

#### Scenario: 启用Sentinel熔断
- **WHEN** 开发人员在配置中设置`feign.circuitbreaker.enabled=true`
- **THEN** 系统 SHALL 集成Sentinel作为熔断器
- **AND** Feign调用 SHALL 受到熔断规则保护

#### Scenario: 熔断规则配置
- **WHEN** 开发人员在Sentinel控制台配置熔断规则
- **THEN** 系统 SHALL 根据规则触发熔断
- **AND** 规则 SHALL 包括：失败率阈值、最小请求数、熔断时长

#### Scenario: 熔断后执行降级逻辑
- **WHEN** Feign Client触发熔断
- **THEN** 系统 SHALL 执行@FeignClient的fallback方法
- **AND** fallback方法 SHALL 返回默认值或抛出降级异常
- **AND** 系统 SHALL 记录熔断日志

#### Scenario: 熔断器自动恢复
- **WHEN** 熔断器处于打开状态且经过熔断时长
- **THEN** 系统 SHALL 进入半开状态
- **AND** 系统 SHALL 允许少量请求通过
- **AND** 如果请求成功，熔断器 SHALL 恢复到关闭状态

### Requirement: Feign性能监控
系统 SHALL 记录Feign调用的性能指标，包括请求次数、响应时间、成功率。

**监控指标**：
- 请求总数（按服务名、接口名分组）
- 响应时间（P50、P95、P99）
- 错误率（按错误码分组）
- 超时次数

#### Scenario: 记录请求日志
- **WHEN** Feign Client发起调用
- **THEN** 系统 SHALL 记录请求日志，包括：服务名、接口方法、请求参数、时间戳
- **AND** 日志级别 SHALL 根据配置（NONE/BASIC/HEADERS/FULL）

#### Scenario: 记录响应时间和状态码
- **WHEN** Feign Client收到响应
- **THEN** 系统 SHALL 记录响应时间（毫秒）
- **AND** 系统 SHALL 记录HTTP状态码
- **AND** 系统 SHALL 记录是否成功

#### Scenario: 集成Micrometer指标
- **WHEN** 项目集成了Micrometer和Actuator
- **THEN** 系统 SHALL 自动暴露Feign调用指标到`/actuator/metrics`端点
- **AND** 指标 SHALL 包括：`feign.requests.total`、`feign.requests.duration`、`feign.errors.total`

#### Scenario: 集成分布式链路追踪
- **WHEN** 项目集成了SkyWalking或Zipkin
- **THEN** Feign调用 SHALL 自动生成Trace Span
- **AND** Span SHALL 包含：服务名、接口名、请求耗时
- **AND** 调用链 SHALL 可以在追踪平台查看

### Requirement: Feign重试机制
系统 SHALL 支持可配置的重试策略，在临时故障时自动重试。

**重试配置**：
- 最大重试次数
- 重试间隔
- 可重试的HTTP状态码（如503、504）

#### Scenario: 读取超时自动重试
- **WHEN** Feign Client调用因读取超时失败
- **THEN** 系统 SHALL 自动重试
- **AND** 重试次数 SHALL 不超过配置的最大值（默认为1次）
- **AND** 重试间隔 SHALL 可配置（默认为0，立即重试）

#### Scenario: 服务不可用自动重试
- **WHEN** Feign Client调用返回HTTP 503（Service Unavailable）
- **THEN** 系统 SHALL 判断为可重试的错误
- **AND** 系统 SHALL 自动重试

#### Scenario: 客户端错误不重试
- **WHEN** Feign Client调用返回HTTP 400（Bad Request）
- **THEN** 系统 SHALL 判断为客户端错误
- **AND** 系统 SHALL NOT 重试
- **AND** 系统 SHALL 直接抛出异常

#### Scenario: 重试次数配置
- **WHEN** 开发人员配置`feign.client.config.default.retryer`
- **THEN** 系统 SHALL 使用配置的重试器
- **AND** 重试器 SHALL 支持自定义重试次数和间隔

### Requirement: Feign压缩配置
系统 SHALL 支持请求和响应压缩，减少网络传输数据量。

**压缩配置**：
- 请求压缩（GZIP）
- 响应压缩（GZIP）
- 压缩阈值（最小压缩字节数）

#### Scenario: 启用请求压缩
- **WHEN** 开发人员配置`feign.compression.request.enabled=true`
- **THEN** Feign Client SHALL 在请求头中添加`Content-Encoding: gzip`
- **AND** 请求体 SHALL 被压缩为GZIP格式

#### Scenario: 启用响应压缩
- **WHEN** 开发人员配置`feign.compression.response.enabled=true`
- **THEN** Feign Client SHALL 在请求头中添加`Accept-Encoding: gzip`
- **AND** 系统 SHALL 自动解压响应体

#### Scenario: 配置压缩阈值
- **WHEN** 开发人员配置`feign.compression.request.mime-types`和`min-request-size`
- **THEN** 系统 SHALL 只压缩指定MIME类型的请求（如application/json）
- **AND** 系统 SHALL 只压缩超过阈值字节数的请求（默认为2048字节）

### Requirement: Feign Mock测试支持
系统 SHALL 支持在测试环境中Mock Feign Client，避免实际调用远程服务。

**Mock方案**：
- 使用@MockBean创建Mock对象
- 使用WireMock模拟HTTP服务
- 使用Testcontainers启动真实服务（集成测试）

#### Scenario: 单元测试中使用Mock
- **WHEN** 开发人员编写应用服务的单元测试
- **THEN** 开发人员 SHALL 可以使用@MockBean创建Feign Client的Mock对象
- **AND** Mock对象 SHALL 使用Mockito的when/then语法定义返回值
- **AND** 测试 SHALL NOT 发起HTTP请求

#### Scenario: 集成测试中使用WireMock
- **WHEN** 开发人员编写集成测试
- **THEN** 开发人员 SHALL 可以使用WireMock模拟HTTP服务
- **AND** WireMock SHALL 根据配置的请求路径返回预设的响应
- **AND** 测试 SHALL 验证Feign Client的请求参数和响应处理

#### Scenario: 端到端测试中启动真实服务
- **WHEN** 开发人员编写端到端测试
- **THEN** 开发人员 SHALL 可以使用Testcontainers启动被调用的服务
- **AND** 测试 SHALL 验证完整的调用链路
- **AND** 测试 SHALL 在结束后自动清理容器
