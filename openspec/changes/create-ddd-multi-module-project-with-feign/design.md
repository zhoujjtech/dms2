# Design: DDD多模块项目架构设计

## Context

### 背景
当前需要从零开始构建一个新的企业级Java项目，该项目需要支持两种部署模式：
1. **独立部署模式**：作为Spring Boot微服务独立运行，通过Nacos注册中心提供服务
2. **嵌入式依赖模式**：API模块作为Maven依赖被其他项目引用，通过OpenFeign实现远程调用

### 当前状态
- 新项目，无历史遗留代码
- 团队熟悉Spring Boot但可能对DDD和Spring Cloud Alibaba经验有限
- 需要建立标准化的项目架构模板供后续项目参考

### 约束条件
- **JDK版本**：必须使用JDK 17
- **框架选型**：Spring Boot 3.x + Spring Cloud Alibaba 2022.x+
- **服务注册**：必须使用Nacos
- **构建工具**：使用Maven（团队现有经验）
- **架构模式**：严格遵循DDD分层架构
- **API兼容性**：API模块变更必须保持向后兼容

### 利益相关方
- **架构团队**：确保架构符合企业标准
- **开发团队**：需要清晰的开发规范和文档
- **运维团队**：关注部署、监控、配置管理
- **消费方团队**：其他业务系统将引用API模块

## Goals / Non-Goals

### Goals
1. **建立标准化DDD多模块架构**：提供清晰的项目结构模板，包含5个核心模块
2. **实现API模块双重使用模式**：既可独立部署，也可作为Maven依赖引用
3. **集成OpenFeign实现服务间调用**：提供声明式HTTP客户端，简化服务调用
4. **确保模块依赖隔离**：API模块不依赖业务实现，仅依赖基础库
5. **提供完整的配置管理方案**：支持本地配置和Nacos配置中心
6. **建立开发规范**：包括包结构、命名约定、分层规范

### Non-Goals
- 具体业务领域实现（用户、订单、商品等）
- 数据库持久化方案（MyBatis/JPA/Redis等在后续变更中确定）
- 安全认证授权机制（OAuth2/JWT等）
- API网关集成
- 分布式事务（Seata）
- 消息队列（RocketMQ/Kafka）
- 性能优化和缓存策略
- 国际化支持

## Decisions

### 决策1: Maven多模块结构设计

**选择**：采用父POM + 5个子模块的层级结构

**模块划分**：
```
project-parent (父POM)
├── project-api           (API接口定义模块)
├── project-domain        (领域层模块)
├── project-application   (应用服务层模块)
├── project-infrastructure (基础设施层模块)
└── project-interface     (接口层模块，包含启动类)
```

**理由**：
- **清晰的分层边界**：每个模块对应DDD的一个层次，职责明确
- **依赖方向可控**：强制执行依赖倒置（domain不依赖任何下层模块）
- **独立演进**：API模块可以独立发版，不牵连业务实现
- **灵活组合**：其他项目可以只依赖api模块，也可以依赖多个模块

**替代方案**：
- **方案A：3层架构（api/domain/app）**
  - ❌ 不符合DDD标准分层，基础设施层和应用层混在一起
- **方案B：单体应用（不分模块）**
  - ❌ 无法实现API模块独立引用
  - ❌ 分层边界不清晰，容易越界调用

### 决策2: 模块间依赖关系设计

**依赖方向**（上层依赖下层）：
```
interface → application → domain
                          ↘ infrastructure (通过依赖倒置)
interface → infrastructure (直接依赖，启动类需要)

api (独立模块，无业务依赖)
  其他项目 → api (通过Maven依赖)
  interface → api (内部实现)
```

**依赖规则**：
1. **api模块**：仅依赖基础库（lombok、spring-web、commons-lang3等），**不依赖任何业务模块**
2. **domain模块**：仅依赖api模块和基础库，**不依赖application/infrastructure/interface**
3. **application模块**：依赖domain模块和api模块
4. **infrastructure模块**：依赖domain模块（实现仓储接口），可选依赖api模块
5. **interface模块**：依赖application、infrastructure、api模块，包含启动类

**Maven依赖配置示例**：
```xml
<!-- project-api/pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- ❌ 不依赖 project-domain/application/infrastructure/interface -->
</dependencies>

<!-- project-domain/pom.xml -->
<dependencies>
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>project-api</artifactId>
    </dependency>
    <!-- ❌ 不依赖下层模块 -->
</dependencies>
```

**理由**：
- **强制分层**：通过Maven依赖机制在编译期防止越界调用
- **依赖倒置原则**：domain定义仓储接口，infrastructure实现具体实现
- **API独立性**：api模块可被外部项目零耦合引用

**替代方案**：
- **方案A：所有模块互相依赖**
  - ❌ 破坏分层架构，导致循环依赖
  - ❌ 无法保证领域层纯净

### 决策3: API模块Feign Client集成方案

**选择**：API模块中同时定义**服务接口**和**Feign Client接口**

**设计模式**：
```java
// project-api模块
public interface UserService {
    UserDTO getUserById(Long id);
    CreateUserResponse createUser(CreateUserRequest request);
}

// project-api模块 (Feign Client)
@FeignClient(
    name = "${service.name.user-service:user-service}", // 服务名可配置
    path = "/api/users"
)
public interface UserFeignClient extends UserService {
    // 继承业务接口，无需重复定义方法
}

// project-interface模块 (REST实现)
@RestController
@RequestMapping("/api/users")
public class UserController implements UserService {
    // 实现业务接口
}
```

**Feign配置类**（在api模块提供）：
```java
@Configuration
public class FeignClientConfiguration {
    @Bean
    public RequestInterceptor authInterceptor() {
        return template -> {
            // 添加认证头等
        };
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
```

**理由**：
- **接口复用**：Feign Client直接继承业务接口，避免重复定义
- **类型安全**：消费方通过接口调用，编译期检查
- **统一契约**：服务提供方和消费方共享接口定义，减少同步成本
- **可测试性**：消费方可以Mock接口进行单元测试

**替代方案**：
- **方案A：只在api模块定义Feign Client，不定义业务接口**
  - ❌ 服务实现方无法利用接口定义，需要重复编写Controller方法签名
  - ❌ 不符合面向接口编程原则
- **方案B：消费方自行编写Feign Client**
  - ❌ 接口定义分散，难以维护
  - ❌ 容易出现不一致（服务方改了接口，消费方未同步）

### 决策4: Nacos服务发现与配置管理

**选择**：使用Nacos同时实现**服务注册发现**和**配置中心**

**服务注册配置**（bootstrap.yml）：
```yaml
spring:
  application:
    name: ${projectName}-service
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        metadata:
          version: @project.version@
          api-version: 1.0.0
```

**配置中心集成**（可选）：
```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_SERVER:localhost:8848}
        file-extension: yaml
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        shared-configs:
          - dataId: common-db.yaml
            refresh: true
```

**理由**：
- **统一生态**：Nacos同时提供注册中心和配置中心，减少组件
- **动态配置**：支持配置热更新，无需重启服务
- **多环境支持**：通过namespace隔离开发/测试/生产环境
- **元数据管理**：通过服务元数据记录版本信息

**替代方案**：
- **方案A：Nacos（注册中心）+ Spring Cloud Config（配置中心）**
  - ❌ 需要维护两套系统，增加复杂度
  - ❌ Spring Cloud Config已不推荐使用（官方推荐迁移到Nacos）
- **方案B：Eureka + Spring Cloud Config**
  - ❌ Eureka 2.x已闭源，不再维护
  - ❌ 不符合Spring Cloud Alibaba生态

### 决策5: 包结构设计

**选择**：严格按照DDD分层组织包结构

**标准包结构**：
```
com.example.projectname
├── api/                          (api模块)
│   ├── dto/                      # 数据传输对象
│   │   ├── request/              # 请求DTO
│   │   └── response/             # 响应DTO
│   ├── vo/                       # 视图对象（可选，用于前端展示）
│   ├── feign/                    # Feign Client接口
│   │   └── fallback/             # 降级实现
│   ├── constant/                 # 常量定义
│   └── enums/                    # 枚举类型
│
├── domain/                       (domain模块)
│   ├── model/                    # 领域模型
│   │   ├── entity/               # 实体
│   │   ├── valueobject/          # 值对象
│   │   └── aggregate/            # 聚合根
│   ├── service/                  # 领域服务
│   ├── repository/               # 仓储接口
│   ├── event/                    # 领域事件
│   ├── exception/                # 领域异常
│   └── specification/            # 规约（可选，复杂业务规则）
│
├── application/                  (application模块)
│   ├── service/                  # 应用服务
│   ├── command/                  # 命令对象（用例输入）
│   ├── query/                    # 查询对象（用例输入）
│   ├── handler/                  # 命令/查询处理器
│   └── assembler/                # DTO转换器（Entity ↔ DTO）
│
├── infrastructure/               (infrastructure模块)
│   ├── repository/               # 仓储实现
│   ├── client/                   # 外部服务客户端
│   ├── config/                   # 基础设施配置
│   ├── persistence/              # 持久化相关
│   └── messaging/                # 消息队列（预留）
│
└── interface/                    (interface模块)
    ├── rest/                     # REST控制器
    ├── config/                   # 配置类
    ├── interceptor/              # 拦截器
    ├── filter/                   # 过滤器
    └── Application.java          # 启动类
```

**命名约定**：
- **实体**：`XxxEntity` 或直接 `Xxx`（如User、Order）
- **DTO**：`XxxRequest`、`XxxResponse`、`XxxDTO`
- **Feign Client**：`XxxFeignClient`
- **应用服务**：`XxxAppService` 或 `XxxService`
- **领域服务**：`XxxDomainService`
- **仓储接口**：`XxxRepository`
- **仓储实现**：`XxxRepositoryImpl`
- **控制器**：`XxxController`

**理由**：
- **清晰的分层**：包名直接对应DDD层次，易于理解
- **职责隔离**：不同层次的对象放在不同包中，避免混乱
- **可扩展性**：预留了event、specification等包，支持复杂业务场景

**替代方案**：
- **方案A：按功能模块分包（如user、order、product）**
  - ❌ 每个功能模块内部仍有分层，会导致包结构冗余
  - ❌ 跨功能模块的复用困难（如通用领域服务）
- **方案B：扁平化包结构（所有类在根包下）**
  - ❌ 无法体现分层架构
  - ❌ 大中型项目难以维护

### 决策6: 依赖管理策略

**选择**：父POM统一管理依赖版本，子模块通过BOM引入

**父POM配置**：
```xml
<dependencyManagement>
    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>3.2.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <!-- Spring Cloud Alibaba -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-dependencies</artifactId>
            <version>2022.0.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <!-- 项目内部模块 -->
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>project-api</artifactId>
            <version>${project.version}</version>
        </dependency>
        <!-- ... 其他模块 -->
    </dependencies>
</dependencyManagement>
```

**子模块引用**：
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <!-- 不需要指定版本，由父POM管理 -->
    </dependency>
</dependencies>
```

**理由**：
- **版本一致性**：确保所有子模块使用相同版本的依赖
- **简化升级**：升级依赖版本只需修改父POM
- **避免冲突**：通过dependencyManagement预防版本冲突

**替代方案**：
- **方案A：每个子模块独立管理依赖版本**
  - ❌ 容易出现版本不一致
  - ❌ 升级时需要修改多个文件

### 决策7: API版本管理策略

**选择**：采用Semantic Versioning（语义化版本）+ URL路径版本控制

**版本号格式**：`MAJOR.MINOR.PATCH`（如1.2.3）
- **MAJOR**：不兼容的API变更
- **MINOR**：向后兼容的功能新增
- **PATCH**：向后兼容的问题修复

**URL版本控制**：
```java
@RequestMapping("/api/v1/users")
public class UserControllerV1 {
    // v1版本接口
}

@RequestMapping("/api/v2/users")
public class UserControllerV2 {
    // v2版本接口（同时保留v1以兼容旧客户端）
}
```

**API模块版本发布流程**：
1. **变更评估**：判断变更类型（MAJOR/MINOR/PATCH）
2. **接口变更**：新增接口不影响旧版本，修改接口需升级MINOR
3. **发布新版本**：通过Maven发布到私服
4. **通知消费方**：通过CHANGELOG.md说明变更内容
5. **灰度迁移**：保留旧版本一段时间，让消费方逐步迁移

**理由**：
- **明确的兼容性**：通过版本号快速判断兼容性
- **平滑升级**：支持多版本共存，避免强制中断
- **行业标准**：语义化版本是业界通用实践

**替代方案**：
- **方案A：只用URL版本控制，Maven版本一直用1.0.0**
  - ❌ Maven依赖无法区分兼容性
  - ❌ 消费方无法感知API变更
- **方案B：每次变更都升级MAJOR版本**
  - ❌ 版本号增长过快，失去意义

### 决策8: 测试策略

**选择**：三层测试金字塔 + 集成测试

**测试层次**：
1. **单元测试（Unit Test）**：针对domain层的实体、值对象、领域服务
   - **框架**：JUnit 5 + AssertJ + Mockito
   - **覆盖率**：domain层 > 80%

2. **集成测试（Integration Test）**：针对application层和infrastructure层
   - **框架**：Spring Boot Test + @SpringBootTest
   - **范围**：应用服务、仓储实现、外部客户端
   - **隔离**：使用H2内存数据库或Testcontainers

3. **端到端测试（E2E Test）**：针对interface层的REST API
   - **框架**：MockMvc + RestAssured
   - **场景**：覆盖核心业务流程

**测试命名约定**：
- 单元测试：`XxxTest.java`
- 集成测试：`XxxIntegrationTest.java`
- E2E测试：`XxxE2ETest.java`

**理由**：
- **快速反馈**：单元测试运行快，便于TDD开发
- **真实场景**：集成测试验证模块协作
- **端到端验证**：E2E测试确保系统可用

**替代方案**：
- **方案A：只写端到端测试**
  - ❌ 运行慢，难以定位问题
  - ❌ 不适合TDD

## Risks / Trade-offs

### 风险1: API模块向后兼容性难以保证

**风险描述**：
API模块一旦发布给消费方，后续变更可能破坏兼容性，导致所有消费方需要升级。

**缓解措施**：
1. **严格的API设计原则**：
   - 新增字段：向后兼容（消费方可选升级）
   - 修改字段：通过版本控制（新增v2接口，保留v1）
   - 删除字段：先标记为`@Deprecated`，至少保留一个大版本
2. **自动化兼容性检测**：使用工具（如AssertJ、JSON Schema）验证API变更
3. **灰度发布**：先在内部项目验证API变更，再对外发布
4. **消费方通知机制**：建立发布通知渠道（邮件、IM群组）

### 风险2: 模块依赖关系复杂化

**风险描述**：
5个模块的依赖关系可能导致循环依赖或编译错误，增加维护成本。

**缓解措施**：
1. **Maven Enforcer Plugin**：在构建期检测循环依赖
   ```xml
   <plugin>
       <groupId>org.apache.maven.plugins</groupId>
       <artifactId>maven-enforcer-plugin</artifactId>
       <executions>
           <execution>
               <id>enforce-no-cycles</id>
               <goals>
                   <goal>enforce</goal>
               </goals>
               <configuration>
                   <rules>
                       <banCircularDependencies/>
                   </rules>
               </configuration>
           </execution>
       </executions>
   </plugin>
   ```
2. **架构守护规则**：使用ArchUnit编写测试，强制分层规则
   ```java
   @AnalyzeClasses(packages = "com.example")
   public class ArchitectureTest {
       @ArchTest
       static final ArchRule domain_layer_should_not_depend_on_application =
           noClasses()
               .that().resideInAPackage("..domain..")
               .should().dependOnClassesThat()
               .resideInAPackage("..application..");
   }
   ```
3. **清晰的文档**：在README中说明模块依赖关系图

### 风险3: 团队DDD经验不足导致架构腐化

**风险描述**：
开发团队如果不理解DDD原则，可能在domain层编写基础设施代码，破坏分层。

**缓解措施**：
1. **培训与知识分享**：组织DDD培训，推荐阅读《领域驱动设计》
2. **代码审查清单**：在Code Review中检查分层规则
3. **脚手架工具**：提供Maven Archetype，自动生成标准包结构
4. **示例代码**：在项目中提供标准实现的示例（如一个完整的用例）

### 风险4: Nacos单点故障

**风险描述**：
如果Nacos Server宕机，服务无法注册和发现，导致服务不可用。

**缓解措施**：
1. **Nacos集群部署**：生产环境必须使用Nacos集群模式（≥3节点）
2. **本地缓存**：Spring Cloud Alibaba会缓存服务列表，Nacos短时间宕机不影响已缓存的服务调用
3. **健康检查**：监控Nacos Server健康状态，及时告警
4. **多数据中心**：跨机房部署Nacos集群，提高可用性

### 风险5: Maven依赖版本冲突

**风险描述**：
Spring Boot 3.x和Spring Cloud Alibaba 2022.x的版本兼容性问题，可能导致启动失败。

**缓解措施**：
1. **官方兼容性验证**：参考[Spring Cloud Alibaba官方文档](https://sca.aliyun.com/docs/2022/overview/version-explain/)的版本对应表
2. **依赖分析工具**：使用`mvn dependency:tree`分析依赖树
3. **版本锁定**：在父POM的`<dependencyManagement>`中显式声明所有依赖版本
4. **测试验证**：在CI/CD流程中增加启动验证步骤

### 权衡1: 启动速度 vs 模块化程度

**现状**：
- 5个模块会增加Maven构建时间和Spring Boot启动时间
- 但模块化带来清晰的架构边界

**权衡选择**：
- **接受稍慢的启动速度**（增加1-3秒），换取更好的可维护性
- **开发环境优化**：在IDE中通过Spring Boot DevTools加速热重载

### 权衡2: API模块粒度

**现状**：
- 单一API模块包含所有接口
- 项目变大后，API模块可能过于臃肿

**权衡选择**：
- **初期采用单API模块**，简化依赖管理
- **未来按业务领域拆分**：当接口数量>50时，拆分为`project-api-user`、`project-api-order`等

## Migration Plan

### 阶段1: 项目脚手架搭建（1-2天）

**步骤**：
1. **创建父POM**：配置Spring Boot、Spring Cloud Alibaba依赖管理
2. **创建5个子模块**：生成标准的pom.xml和包结构
3. **配置Maven插件**：添加编译、测试、打包插件
4. **编写README.md**：说明项目结构、构建命令、启动方式

**验收标准**：
- `mvn clean install`成功构建
- 所有模块可以独立编译
- 依赖关系正确（无循环依赖）

### 阶段2: 基础设施配置（2-3天）

**步骤**：
1. **配置Nacos连接**：在`application.yml`中配置服务注册地址
2. **编写Feign配置类**：创建超时、日志、拦截器配置
3. **创建启动类**：在`interface`模块编写`Application.java`
4. **验证启动**：启动interface模块，确认在Nacos控制台可见

**验收标准**：
- 项目启动成功，无报错
- 在Nacos控制台的服务列表中可见
- 健康检查端点（`/actuator/health`）正常

### 阶段3: 示例用例实现（3-5天）

**步骤**：
1. **选择简单业务场景**：如"用户管理"（User CRUD）
2. **实现domain层**：创建User实体、UserRepository接口
3. **实现application层**：创建UserAppService、DTO转换器
4. **实现infrastructure层**：创建UserRepositoryImpl（先用内存存储，后续接入数据库）
5. **实现interface层**：创建UserController，实现REST API
6. **实现api模块**：创建UserDTO、UserService接口、UserFeignClient
7. **编写测试**：单元测试、集成测试、E2E测试

**验收标准**：
- 所有测试通过
- 通过Postman/curl可以调用API
- 其他项目引入api模块后，可以通过Feign调用

### 阶段4: 文档与培训（1-2天）

**步骤**：
1. **编写开发规范文档**：说明包结构、命名约定、分层规则
2. **编写API使用指南**：说明如何引入api模块、如何使用Feign Client
3. **录制培训视频**：演示如何创建新的用例
4. **组织培训会议**：解答团队疑问

**验收标准**：
- 文档完整、易懂
- 团队成员能独立开发新功能

### 阶段5: 生产部署准备（2-3天）

**步骤**：
1. **配置打包插件**：生成可执行的JAR文件
2. **编写Dockerfile**：支持容器化部署
3. **配置Kubernetes部署文件**（可选）：支持K8s部署
4. **编写部署文档**：说明环境变量、配置项、启动命令
5. **配置监控**：集成Spring Boot Actuator、Prometheus

**验收标准**：
- 可以通过Docker/K8s成功部署
- 监控指标可以正常采集
- 健康检查和就绪探针正常

### 阶段6: 灰度发布与监控（持续）

**步骤**：
1. **选择内部项目作为首个消费方**：验证API模块可用性
2. **收集反馈**：记录使用中的问题和改进建议
3. **优化性能**：根据监控数据优化接口响应时间
4. **逐步推广**：向更多项目开放API模块引用

**回滚策略**：
如果发现严重问题：
1. **立即停止发布**：通知所有消费方暂停升级
2. **修复问题**：在单独的分支上修复
3. **发布新版本**：通过PATCH版本号发布修复
4. **紧急通知**：通过邮件/IM群组通知所有消费方升级

## Open Questions

### 问题1: 是否需要支持API模块的独立启动？

**背景**：
当前设计中，api模块只是一个库（JAR），无法独立运行。

**待决策**：
是否需要让api模块也可以作为独立服务运行（提供Swagger文档、Mock接口等）？

**建议**：
- **初期不支持**：增加复杂度，收益不明显
- **未来考虑**：如果消费方需要离线查看API文档，可以提供单独的"API文档服务"

### 问题2: 是否需要集成API网关？

**背景**：
如果有多个微服务，通常需要API网关统一入口。

**待决策**：
是否在本项目中集成Spring Cloud Gateway？

**建议**：
- **本项目不集成**：Gateway应该是独立的基础设施服务
- **由平台团队提供**：统一管理所有服务的路由、限流、认证

### 问题3: 数据库持久化方案选择

**背景**：
当前设计未确定使用MyBatis、JPA还是其他ORM框架。

**待决策**：
在infrastructure层使用什么持久化框架？

**建议**：
- **由业务复杂度决定**：
  - 简单CRUD：Spring Data JPA
  - 复杂查询/性能敏感：MyBatis
  - 混合模式：JPA + MyBatis（复杂查询用MyBatis）

### 问题4: 是否需要支持多租户？

**背景**：
如果系统需要服务多个客户/组织，可能需要多租户架构。

**待决策**：
是否在初始设计中支持多租户？

**建议**：
- **初期不支持**：避免过度设计
- **预留扩展点**：在domain层的实体中预留`tenantId`字段

### 问题5: 如何处理跨模块的领域事件？

**背景**：
domain层的领域事件如何传递到其他模块或外部系统？

**待决策**：
事件总线实现方式（内存消息队列、RocketMQ、Redis Pub/Sub等）

**建议**：
- **初期使用Spring Events**（ApplicationEventPublisher）：简单可靠
- **后续升级到消息队列**：当需要异步解耦或跨服务通信时

---

## 附录：技术选型版本清单

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 17 | LTS版本，长期支持 |
| Spring Boot | 3.2.0 | 稳定版本 |
| Spring Cloud Alibaba | 2022.0.0.0 | 与Spring Boot 3.x兼容 |
| Nacos Client | 2.2.3 | 服务注册与配置中心 |
| OpenFeign | 4.0.3 | 声明式HTTP客户端 |
| Spring Cloud LoadBalancer | 4.0.3 | 客户端负载均衡 |
| Lombok | 1.18.30 | 简化代码 |
| MapStruct | 1.5.5.Final | DTO转换（推荐） |
| Maven | 3.9+ | 构建工具 |
| Maven Compiler Plugin | 3.11.0 | 编译插件 |
| Maven Surefire Plugin | 3.0.0 | 单元测试插件 |
| Maven Failsafe Plugin | 3.0.0 | 集成测试插件 |
| ArchUnit | 1.2.1 | 架构测试 |
| JUnit | 5.10.1 | 测试框架 |
| Mockito | 5.7.0 | Mock框架 |
| AssertJ | 3.24.2 | 断言库 |
| Testcontainers | 1.19.3 | 集成测试容器化（可选） |
| SpringDoc | 2.2.0 | OpenAPI 3.0文档生成（可选） |
