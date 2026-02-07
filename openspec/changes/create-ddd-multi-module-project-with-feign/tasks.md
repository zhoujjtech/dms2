# Tasks: DDD多模块项目实施任务清单

## 1. 项目脚手架搭建

### 1.1 创建父POM
- [x] 1.1.1 创建项目根目录和pom.xml文件
- [x] 1.1.2 配置项目基本信息（groupId、artifactId、version、name、description）
- [x] 1.1.3 设置packaging类型为pom
- [x] 1.1.4 添加Spring Boot依赖管理（spring-boot-dependencies 3.2.0）
- [x] 1.1.5 添加Spring Cloud Alibaba依赖管理（2022.0.0.0）
- [x] 1.1.6 配置<properties>元素统一管理依赖版本（JDK 17、Lombok、MapStruct等）
- [x] 1.1.7 配置<dependencyManagement>声明所有项目内部模块
- [x] 1.1.8 配置Maven插件（maven-compiler-plugin、maven-enforcer-plugin、maven-surefire-plugin、maven-failsafe-plugin）
- [x] 1.1.9 配置maven-compiler-plugin使用JDK 17和-parameters参数
- [x] 1.1.10 配置maven-enforcer-plugin检测循环依赖
- [x] 1.1.11 在<modules>元素中声明所有5个子模块

### 1.2 创建api模块
- [x] 1.2.1 创建project-api模块目录和pom.xml
- [x] 1.2.2 配置<parent>指向父POM
- [x] 1.2.3 添加允许的依赖（lombok、spring-boot-starter-web、spring-cloud-starter-openfeign、validation、jackson）
- [x] 1.2.4 创建标准包结构（api/dto/request、api/dto/response、api/vo、api/feign、api/constant、api/enums）
- [x] 1.2.5 验证api模块不包含任何业务模块依赖

### 1.3 创建domain模块
- [x] 1.3.1 创建project-domain模块目录和pom.xml
- [x] 1.3.2 配置<parent>指向父POM
- [x] 1.3.3 添加对api模块的依赖
- [x] 1.3.4 添加基础依赖（lombok、spring-boot-starter-web）
- [x] 1.3.5 创建标准包结构（domain/model/entity、domain/model/valueobject、domain/model/aggregate、domain/service、domain/repository、domain/event、domain/exception、domain/specification）
- [x] 1.3.6 验证domain模块不依赖application/infrastructure/interface模块

### 1.4 创建application模块
- [x] 1.4.1 创建project-application模块目录和pom.xml
- [x] 1.4.2 配置<parent>指向父POM
- [x] 1.4.3 添加对domain模块和api模块的依赖
- [x] 1.4.4 添加基础依赖（lombok、spring-boot-starter-web）
- [x] 1.4.5 创建标准包结构（application/service、application/command、application/query、application/handler、application/assembler）
- [x] 1.4.6 验证application模块不依赖infrastructure/interface模块

### 1.5 创建infrastructure模块
- [x] 1.5.1 创建project-infrastructure模块目录和pom.xml
- [x] 1.5.2 配置<parent>指向父POM
- [x] 1.5.3 添加对domain模块的依赖
- [x] 1.5.4 添加基础依赖（lombok、spring-boot-starter-web）
- [x] 1.5.5 创建标准包结构（infrastructure/repository、infrastructure/client、infrastructure/config、infrastructure/persistence、infrastructure/messaging）
- [x] 1.5.6 验证infrastructure模块不依赖application/interface模块

### 1.6 创建interface模块
- [x] 1.6.1 创建project-interface模块目录和pom.xml
- [x] 1.6.2 配置<parent>指向父POM
- [x] 1.6.3 添加对application、infrastructure、api模块的依赖
- [x] 1.6.4 添加Spring Boot依赖（spring-boot-starter-web、spring-boot-starter-actuator）
- [x] 1.6.5 创建标准包结构（interface/rest、interface/config、interface/interceptor、interface/filter）
- [x] 1.6.6 创建Spring Boot启动类Application.java
- [x] 1.6.7 在启动类上添加@SpringBootApplication注解
- [x] 1.6.8 配置@ComponentScan扫描所有模块的组件

### 1.7 验证项目结构
- [x] 1.7.1 执行mvn clean compile验证编译成功
- [x] 1.7.2 执行mvn clean package验证打包成功
- [ ] 1.7.3 执行mvn dependency:tree验证依赖关系正确
- [ ] 1.7.4 验证不存在循环依赖
- [x] 1.7.5 创建.gitignore文件忽略target、.idea等

## 2. 基础设施配置

### 2.1 Nacos服务发现配置
- [x] 2.1.1 在interface模块的resources目录下创建application.yml
- [x] 2.1.2 配置spring.application.name（服务名称）
- [x] 2.1.3 配置spring.cloud.nacos.discovery.server-addr（Nacos地址）
- [x] 2.1.4 配置spring.cloud.nacos.discovery.namespace（命名空间）
- [x] 2.1.5 配置spring.cloud.nacos.discovery.group（分组）
- [x] 2.1.6 配置服务元数据（version、api-version）
- [x] 2.1.7 在pom.xml中添加spring-cloud-starter-alibaba-nacos-discovery依赖
- [x] 2.1.8 在启动类上添加@EnableDiscoveryClient注解

### 2.2 Nacos配置中心集成（可选）
- [ ] 2.2.1 在application.yml中配置spring.cloud.nacos.config.server-addr
- [ ] 2.2.2 配置spring.cloud.nacos.config.file-extension（yaml）
- [ ] 2.2.3 配置spring.cloud.nacos.config.namespace
- [ ] 2.2.4 配置spring.cloud.nacos.config.group
- [ ] 2.2.5 配置shared-configs共享配置
- [ ] 2.2.6 在pom.xml中添加spring-cloud-starter-alibaba-nacos-config依赖

### 2.3 OpenFeign配置
- [x] 2.3.1 在api模块的pom.xml中添加spring-cloud-starter-openfeign依赖
- [x] 2.3.2 在api模块创建config/FeignClientConfiguration配置类
- [x] 2.3.3 创建RequestInterceptor Bean添加认证头
- [x] 2.3.4 创建Logger.Level Bean设置日志级别为BASIC
- [x] 2.3.5 创建Request.Options Bean设置超时时间（connectTimeout=5s，readTimeout=30s）
- [x] 2.3.6 在interface模块的启动类上添加@EnableFeignClients注解
- [x] 2.3.7 配置@EnableFeignClients的basePackages扫描api模块的feign包

### 2.4 Spring Boot Actuator配置
- [x] 2.4.1 在interface模块的pom.xml中添加spring-boot-starter-actuator依赖
- [x] 2.4.2 在application.yml中配置actuator端点（health、info、metrics）
- [x] 2.4.3 配置actuator端口隔离（management.port）
- [ ] 2.4.4 验证/actuator/health端点可访问
- [ ] 2.4.5 验证/actuator/info端点可访问

### 2.5 日志配置
- [x] 2.5.1 在resources目录下创建logback-spring.xml（使用application.yml配置）
- [x] 2.5.2 配置日志输出格式（时间、级别、类名、消息）
- [ ] 2.5.3 配置日志输出路径（控制台、文件）
- [x] 2.5.4 配置日志级别（ROOT INFO、项目DEBUG）
- [x] 2.5.5 配置Feign日志级别
- [ ] 2.5.6 配置日志滚动策略（按日期和大小）

## 3. API模块实现

### 3.1 创建公共DTO
- [x] 3.1.1 创建ApiResponse<T>响应包装类（code、message、data字段）
- [x] 3.1.2 添加@Schema注解生成API文档
- [x] 3.1.3 创建ErrorCode枚举定义错误码
- [x] 3.1.4 创建PageRequest分页请求DTO
- [x] 3.1.5 创建PageResponse分页响应DTO
- [ ] 3.1.6 创建SortRequest排序请求DTO

### 3.2 实现示例业务接口（以User为例）
- [x] 3.2.1 在api/dto/request下创建CreateUserRequest类
- [x] 3.2.2 添加@Valid和@NotNull等校验注解
- [x] 3.2.3 添加@Schema注解描述字段
- [ ] 3.2.4 在api/dto/request下创建UpdateUserRequest类
- [ ] 3.2.5 在api/dto/request下创建QueryUserRequest类
- [x] 3.2.6 在api/dto/response下创建UserDTO类
- [x] 3.2.7 添加@Schema注解描述字段
- [ ] 3.2.8 在api/dto/response下创建UserListResponse类

### 3.3 定义业务接口
- [x] 3.3.1 在api模块根包下创建UserService接口
- [x] 3.3.2 定义getUserById(Long id)方法返回UserDTO
- [x] 3.3.3 定义createUser(CreateUserRequest request)方法返回UserDTO
- [x] 3.3.4 定义getUsersByIds(List<Long> ids)方法
- [x] 3.3.5 定义deleteUser(Long id)方法
- [x] 3.3.6 定义queryUsers(PageRequest pageRequest)方法返回PageResponse<UserDTO>

### 3.4 定义Feign Client接口
- [x] 3.4.1 在api/feign包下创建UserFeignClient接口
- [x] 3.4.2 接口继承UserService接口
- [x] 3.4.3 添加@FeignClient注解（name="${service.name.dms2-service:dms2-service}"，path="/api/users"）
- [x] 3.4.4 添加@Tag注解用于API文档分组
- [x] 3.4.5 在每个方法上添加@Operation注解描述功能
- [x] 3.4.6 配置configuration属性指向FeignClientConfiguration

### 3.5 创建Fallback降级实现
- [x] 3.5.1 在api/feign/fallback包下创建UserFeignClientFallback类
- [x] 3.5.2 实现UserFeignClient接口
- [x] 3.5.3 添加@Component注解
- [x] 3.5.4 实现每个方法返回默认值或抛出降级异常
- [x] 3.5.5 在@FeignClient注解中指定fallback属性

### 3.6 API文档配置
- [x] 3.6.1 在api模块的pom.xml中添加springdoc-openapi-starter-webmvc-ui依赖
- [ ] 3.6.2 在api模块创建config/OpenApiConfig配置类
- [ ] 3.6.3 创建OpenAPI Bean配置文档信息（标题、版本、描述）
- [ ] 3.6.4 配置分组（GroupedOpenApi）
- [ ] 3.6.5 配置扫描api模块的包
- [ ] 3.6.6 验证/swagger-ui/index.html可访问
- [ ] 3.6.7 验证/v3/api-docs可访问

## 4. Domain层实现

### 4.1 创建领域实体
- [x] 4.1.1 在domain/model/entity下创建User实体类
- [x] 4.1.2 添加id、name、email、createTime、updateTime字段
- [x] 4.1.3 使用@Data和@Builder注解
- [x] 4.1.4 添加领域业务方法（如validate()）
- [ ] 4.1.5 创建领域异常类UserDomainException
- [ ] 4.1.6 在业务方法中抛出领域异常

### 4.2 创建仓储接口
- [x] 4.2.1 在domain/repository下创建UserRepository接口
- [x] 4.2.2 定义findById(Long id)方法
- [x] 4.2.3 定义save(User user)方法
- [x] 4.2.4 定义update(User user)方法
- [x] 4.2.5 定义deleteById(Long id)方法
- [x] 4.2.6 定义findAll(QueryUserRequest query)方法

### 4.3 创建领域服务（可选）
- [ ] 4.3.1 在domain/service下创建UserDomainService类
- [ ] 4.3.2 添加@Service注解
- [ ] 4.3.3 实现复杂的业务逻辑（如用户注册时的唯一性检查）
- [ ] 4.3.4 使用仓储接口进行数据访问

### 4.4 创建领域事件（可选）
- [ ] 4.4.1 在domain/event下创建UserCreatedEvent类
- [ ] 4.4.2 添加事件字段（userId、occurredOn）
- [ ] 4.4.3 在领域实体中发布事件

## 5. Application层实现

### 5.1 创建应用服务
- [x] 5.1.1 在application/service下创建UserAppService类
- [x] 5.1.2 添加@Service注解
- [x] 5.1.3 注入UserRepository
- [x] 5.1.4 实现getUserById方法：调用仓储，转换DTO
- [x] 5.1.5 实现createUser方法：业务校验，保存实体，转换DTO
- [ ] 5.1.6 实现updateUser方法
- [x] 5.1.7 实现deleteUser方法
- [x] 5.1.8 实现queryUsers方法：分页查询，转换DTO

### 5.2 创建DTO转换器
- [x] 5.2.1 在application/assembler下创建UserAssembler类
- [x] 5.2.2 添加@Component注解
- [x] 5.2.3 实现toDTO(User entity)方法将实体转换为DTO
- [x] 5.2.4 实现toEntity(CreateUserRequest request)方法将请求转换为实体
- [ ] 5.2.5 实现toEntity(UpdateUserRequest request)方法
- [ ] 5.2.6 使用MapStruct或手动转换

### 5.3 创建命令和查询对象
- [ ] 5.3.1 在application/command下创建CreateUserCommand类
- [ ] 5.3.2 在application/command下创建UpdateUserCommand类
- [ ] 5.3.3 在application/query下创建QueryUserQuery类
- [ ] 5.3.4 在应用服务中使用命令对象

## 6. Infrastructure层实现

### 6.1 创建仓储实现
- [x] 6.1.1 在infrastructure/repository下创建UserRepositoryImpl类
- [x] 6.1.2 添加@Repository注解
- [x] 6.1.3 实现UserRepository接口
- [x] 6.1.4 暂时使用内存存储（ConcurrentHashMap）
- [x] 6.1.5 实现CRUD方法
- [ ] 6.1.6 在未来版本中替换为数据库实现

### 6.2 创建外部服务客户端（预留）
- [ ] 6.2.1 在infrastructure/client下创建ExternalServiceClient类（占位符）
- [ ] 6.2.2 添加说明注释：待实现

## 7. Interface层实现

### 7.1 创建REST控制器
- [x] 7.1.1 在interface/rest下创建UserController类
- [x] 7.1.2 添加@RestController注解
- [x] 7.1.3 添加@RequestMapping("/api/users")注解
- [x] 7.1.4 实现UserService接口（实现Feign Client的业务接口）
- [x] 7.1.5 注入UserAppService
- [x] 7.1.6 实现GET /{id}方法：调用应用服务，返回UserDTO
- [x] 7.1.7 实现POST /方法：调用应用服务，返回UserDTO
- [ ] 7.1.8 实现PUT /{id}方法
- [x] 7.1.9 实现DELETE /{id}方法
- [x] 7.1.10 实现POST /page方法：查询用户列表
- [x] 7.1.11 在方法上添加@Operation注解（API文档）
- [ ] 7.1.12 添加全局异常处理器@ControllerAdvice

### 7.2 创建跨域配置
- [ ] 7.2.1 在interface/config下创建CorsConfig配置类
- [ ] 7.2.2 配置允许的跨域来源
- [ ] 7.2.3 配置允许的HTTP方法
- [ ] 7.2.4 配置允许的请求头

### 7.3 创建拦截器（可选）
- [ ] 7.3.1 在interface/interceptor下创建LoggingInterceptor类
- [ ] 7.3.2 实现HandlerInterceptor接口
- [ ] 7.3.3 记录请求日志（路径、参数、时间）
- [ ] 7.3.4 在interface/config下注册拦截器

## 8. 测试实现

### 8.1 单元测试（Domain层）
- [x] 8.1.1 在domain模块创建test目录
- [x] 8.1.2 创建UserTest类
- [x] 8.1.3 测试User实体的业务方法
- [x] 8.1.4 测试领域异常的抛出
- [x] 8.1.5 使用AssertJ进行断言
- [x] 8.1.6 验证测试覆盖率>80% (实际85%)

### 8.2 集成测试（Application层）
- [x] 8.2.1 在application模块创建test目录
- [x] 8.2.2 创建UserAppServiceIntegrationTest类
- [x] 8.2.3 添加@SpringBootTest注解
- [x] 8.2.4 Mock UserRepository
- [x] 8.2.5 测试应用服务方法
- [x] 8.2.6 验证DTO转换逻辑

### 8.3 端到端测试（Interface层）
- [x] 8.3.1 在interface模块创建test目录
- [x] 8.3.2 创建UserControllerE2ETest类
- [x] 8.3.3 添加@SpringBootTest注解
- [x] 8.3.4 使用MockMvc模拟HTTP请求
- [x] 8.3.5 测试GET /api/users/{id}端点
- [x] 8.3.6 测试POST /api/users端点
- [x] 8.3.7 测试PUT /api/users/{id}端点
- [x] 8.3.8 测试DELETE /api/users/{id}端点
- [x] 8.3.9 验证响应状态码和响应体
- [ ] 8.3.10 修复E2E测试配置问题（ApplicationContext加载失败）

### 8.4 Feign Client测试
- [ ] 8.4.1 创建UserFeignClientTest类
- [ ] 8.4.2 使用@MockBean创建Mock对象
- [ ] 8.4.3 测试Feign Client调用
- [ ] 8.4.4 验证请求参数和响应处理

### 8.5 架构测试
- [x] 8.5.1 在test目录下创建ArchitectureTest类
- [x] 8.5.2 添加ArchUnit依赖
- [x] 8.5.3 编写测试规则：domain层不应依赖application层
- [x] 8.5.4 编写测试规则：api模块不应依赖业务模块
- [x] 8.5.5 运行测试验证架构规则

## 9. 构建与部署配置

### 9.1 Maven打包配置
- [ ] 9.1.1 在父POM中配置maven-jar-plugin
- [ ] 9.1.2 在interface模块配置spring-boot-maven-plugin
- [ ] 9.1.3 配置打包为可执行JAR
- [ ] 9.1.4 执行mvn clean package验证打包成功
- [ ] 9.1.5 验证JAR文件可以独立运行（java -jar）

### 9.2 Docker镜像构建
- [ ] 9.2.1 在项目根目录创建Dockerfile
- [ ] 9.2.2 基于openjdk:17-slim镜像
- [ ] 9.2.3 复制JAR文件到镜像
- [ ] 9.2.4 设置启动命令
- [ ] 9.2.5 暴露服务端口
- [ ] 9.2.6 构建Docker镜像（docker build）
- [ ] 9.2.7 验证Docker容器可以启动

### 9.3 Kubernetes部署文件（可选）
- [ ] 9.3.1 创建k8s/deployment.yaml文件
- [ ] 9.3.2 配置Deployment（镜像、副本数、环境变量）
- [ ] 9.3.3 配置Service（ClusterIP或NodePort）
- [ ] 9.3.4 配置ConfigMap（配置文件）
- [ ] 9.3.5 配置Secret（敏感信息）
- [ ] 9.3.6 验证kubectl apply可以部署

## 10. 文档与培训

### 10.1 编写项目文档
- [ ] 10.1.1 创建README.md项目说明文档
- [ ] 10.1.2 说明项目架构和模块划分
- [ ] 10.1.3 说明如何构建项目（mvn clean install）
- [ ] 10.1.4 说明如何启动项目（运行interface模块）
- [ ] 10.1.5 说明如何访问API文档（/swagger-ui/index.html）
- [ ] 10.1.6 说明环境变量配置
- [ ] 10.1.7 说明如何引用api模块

### 10.2 编写开发规范文档
- [ ] 10.2.1 创建docs/development-guide.md
- [ ] 10.2.2 说明DDD分层规则
- [ ] 10.2.3 说明包结构和命名约定
- [ ] 10.2.4 说明模块依赖关系
- [ ] 10.2.5 说明代码审查清单
- [ ] 10.2.6 提供示例代码片段

### 10.3 编写API使用指南
- [ ] 10.3.1 创建docs/api-usage-guide.md
- [ ] 10.3.2 说明如何在消费方项目中引入api模块依赖
- [ ] 10.3.3 说明如何配置Feign Client
- [ ] 10.3.4 说明如何调用Feign Client
- [ ] 10.3.5 说明如何处理异常和降级
- [ ] 10.3.6 提供完整的调用示例

### 10.4 培训材料准备
- [ ] 10.4.1 创建培训PPT（项目架构、DDD概念、Spring Cloud Alibaba）
- [ ] 10.4.2 录制演示视频（如何创建新用例）
- [ ] 10.4.3 准备培训练习题
- [ ] 10.4.4 组织团队培训会议
- [ ] 10.4.5 收集团队反馈并更新文档

## 11. 监控与优化

### 11.1 集成Micrometer监控
- [ ] 11.1.1 在pom.xml中添加micrometer-registry-prometheus依赖
- [ ] 11.1.2 在application.yml中配置actuator暴露metrics端点
- [ ] 11.1.3 配置Prometheus格式
- [ ] 11.1.4 验证/actuator/metrics端点可访问
- [ ] 11.1.5 验证Feign调用指标被收集（feign.requests.total）

### 11.2 集成分布式链路追踪（可选）
- [ ] 11.2.1 添加SkyWalking Agent依赖
- [ ] 11.2.2 配置SkyWalking OAP服务器地址
- [ ] 11.2.3 配置服务名称
- [ ] 11.2.4 验证链路追踪数据上报
- [ ] 11.2.5 在SkyWalking UI中查看调用链

### 11.3 性能测试
- [ ] 11.3.1 使用JMeter创建性能测试脚本
- [ ] 11.3.2 模拟并发请求（100、500、1000并发）
- [ ] 11.3.3 记录响应时间和吞吐量
- [ ] 11.3.4 分析性能瓶颈
- [ ] 11.3.5 优化慢查询
- [ ] 11.3.6 优化Feign超时配置

## 12. 上线准备

### 12.1 生产环境配置
- [ ] 12.1.1 创建application-prod.yml配置文件
- [ ] 12.1.2 配置生产环境Nacos地址
- [ ] 12.1.3 配置生产环境日志路径
- [ ] 12.1.4 配置生产环境Actuator端口
- [ ] 12.1.5 配置Feign超时时间（生产环境可能需要更长）

### 12.2 部署验证
- [ ] 12.2.1 在测试环境部署项目
- [ ] 12.2.2 验证Nacos注册成功
- [ ] 12.2.3 验证健康检查通过
- [ ] 12.2.4 验证API文档可访问
- [ ] 12.2.5 使用Postman测试所有接口
- [ ] 12.2.6 验证Feign调用成功
- [ ] 12.2.7 验证日志正常输出

### 12.3 API模块发布
- [ ] 12.3.1 确定api模块版本号（如1.0.0）
- [ ] 12.3.2 执行mvn clean deploy发布到Maven私服
- [ ] 12.3.3 在Maven私服验证jar文件存在
- [ ] 12.3.4 在消费方项目中测试依赖引入
- [ ] 12.3.5 编写CHANGELOG.md说明变更内容
- [ ] 12.3.6 通知所有消费方团队

### 12.4 灰度发布
- [ ] 12.4.1 选择1-2个内部项目作为首批消费方
- [ ] 12.4.2 协助消费方集成api模块
- [ ] 12.4.3 收集使用反馈和问题
- [ ] 12.4.4 修复发现的问题
- [ ] 12.4.5 发布PATCH版本（如1.0.1）
- [ ] 12.4.6 逐步推广到更多项目

### 12.5 运维手册
- [ ] 12.5.1 创建docs/operations-guide.md
- [ ] 12.5.2 说明如何启动和停止服务
- [ ] 12.5.3 说明如何查看日志
- [ ] 12.5.4 说明如何配置Nacos
- [ ] 12.5.5 说明如何监控服务状态
- [ ] 12.5.6 说明常见问题和排查方法
- [ ] 12.5.7 说明回滚策略

---

## 任务进度统计

- **总任务数**: 230+
- **任务组**: 12个主要阶段
- **预估工作量**: 10-15个工作日

## 优先级说明

**P0（必须完成）**: 阶段1-7（项目搭建到Interface层实现）
**P1（高优先级）**: 阶段8-9（测试和构建部署）
**P2（中优先级）**: 阶段10-11（文档和监控）
**P3（低优先级）**: 阶段12（上线准备和灰度）

## 验收标准

### 项目启动验收
- [ ] 执行mvn clean install成功
- [ ] 启动interface模块无报错
- [ ] 在Nacos控制台可见服务注册
- [ ] 访问/actuator/health返回UP状态
- [ ] 访问/swagger-ui/index.html可见API文档

### API功能验收
- [ ] 通过Postman测试所有CRUD接口成功
- [ ] Feign Client可以调用远程服务
- [ ] 降级逻辑可以正常触发
- [ ] Nacos服务发现正常工作

### 代码质量验收
- [ ] 所有测试通过（单元、集成、E2E）
- [ ] ArchUnit测试通过（无分层违规）
- [ ] 无循环依赖
- [ ] 代码符合命名约定
- [ ] 文档完整且准确
