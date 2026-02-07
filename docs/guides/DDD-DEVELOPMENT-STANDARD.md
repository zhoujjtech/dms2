# DDD 开发规范手册

## 目录

1. [DDD 核心概念](#1-ddd-核心概念)
2. [项目分层架构](#2-项目分层架构)
3. [模块划分规范](#3-模块划分规范)
4. [命名规范](#4-命名规范)
5. [代码组织规范](#5-代码组织规范)
6. [开发流程规范](#6-开发流程规范)
7. [最佳实践](#7-最佳实践)
8. [常见问题](#8-常见问题)

---

## 1. DDD 核心概念

### 1.1 战略设计

#### 领域 (Domain)
- **定义**: 问题空间，业务逻辑所在的地方
- **作用**: 划定业务边界，聚焦核心业务价值

#### 子域 (Subdomain)
- **核心域**: 核心业务竞争力，需要重点投入
- **支撑域**: 支持核心业务，可以采购或自研
- **通用域**: 通用功能，建议采购或使用开源

#### 限界上下文 (Bounded Context)
- **定义**: 领域模型的边界，一个限界上下文包含完整的领域模型
- **作用**: 隔离不同上下文的领域概念，保持概念一致性
- **实践**: 一个微服务 = 一个限界上下文

#### 上下文映射图 (Context Mapping)
描述不同限界上下文之间的关系：
- **合作关系 (Partnership)**: 两个团队共同制定目标
- **共享内核 (Shared Kernel)**: 共享部分模型和数据
- **客户方-供应方 (Customer-Supplier)**: 上游服务下游
- **防腐层 (ACL)**: 隔离外部系统，转换外部模型

### 1.2 战术设计

#### 实体 (Entity)
```java
/**
 * 用户实体
 * 特征：有唯一标识、有生命周期、可变性
 */
@Entity
public class User {
    private Long id; // 唯一标识
    private String username;
    private String email;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 领域行为
    public void updateEmail(String newEmail) {
        validateEmail(newEmail);
        this.email = newEmail;
        this.updateTime = LocalDateTime.now();
    }

    // 业务规则
    public void validateForCreate() {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
    }

    // 相等性判断基于ID
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User)) return false;
        return id != null && id.equals(((User) obj).getId());
    }
}
```

#### 值对象 (Value Object)
```java
/**
 * 邮箱值对象
 * 特征：不可变、无唯一标识、可替换
 */
@Value
public class Email {
    String value;

    public Email(String value) {
        if (!isValid(value)) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        this.value = value;
    }

    private boolean isValid(String value) {
        return value.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    // 相等性判断基于所有属性
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Email)) return false;
        return value.equals(((Email) obj).value);
    }
}
```

#### 聚合 (Aggregate)
```java
/**
 * 订单聚合
 * 特点：一组相关实体的集合，只有一个聚合根
 */
public class Order {
    private Long id; // 聚合根
    private List<OrderItem> items; // 实体
    private ShippingAddress address; // 值对象
    private Money totalAmount; // 值对象

    // 通过聚合根修改内部对象
    public void addItem(Product product, int quantity) {
        OrderItem item = new OrderItem(product, quantity);
        this.items.add(item);
        recalculateTotal();
    }

    // 保持聚合一致性
    private void recalculateTotal() {
        this.totalAmount = items.stream()
            .map(OrderItem::getSubTotal)
            .reduce(Money.ZERO, Money::add);
    }

    // 外部不能直接访问内部对象
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
```

#### 聚合根 (Aggregate Root)
- 是聚合的唯一入口
- 负责维护聚合内部的一致性边界
- 通过ID引用其他聚合，不持有对象引用

#### 领域服务 (Domain Service)
```java
/**
 * 用户领域服务
 * 场景：当一个业务逻辑不属于某个实体或值对象时
 */
@DomainService
public class UserDomainService {

    /**
     * 检查用户名是否唯一（跨聚合的业务逻辑）
     */
    public boolean isUsernameUnique(UserRepository userRepository, String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * 密码加密（无状态的业务逻辑）
     */
    public String encryptPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }
}
```

#### 仓储 (Repository)
```java
/**
 * 用户仓储接口
 * 特点：定义在领域层，实现在基础设施层
 */
public interface UserRepository {

    /**
     * 保存聚合根
     */
    User save(User user);

    /**
     * 根据ID查找聚合根
     */
    Optional<User> findById(Long id);

    /**
     * 根据业务标识查找
     */
    Optional<User> findByUsername(String username);

    /**
     * 删除聚合根
     */
    void deleteById(Long id);

    /**
     * 判断是否存在
     */
    boolean existsByUsername(String username);
}
```

#### 领域事件 (Domain Event)
```java
/**
 * 用户创建领域事件
 */
public class UserCreatedEvent extends DomainEvent {
    private final Long userId;
    private final String username;
    private final LocalDateTime occurredOn;

    public UserCreatedEvent(Long userId, String username) {
        this.userId = userId;
        this.username = username;
        this.occurredOn = LocalDateTime.now();
    }

    // Getters...
}

/**
 * 发布领域事件
 */
@Service
public class UserApplicationService {

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        // 1. 创建用户
        User user = User.create(...);

        // 2. 保存
        User savedUser = userRepository.save(user);

        // 3. 发布领域事件
        eventPublisher.publishEvent(
            new UserCreatedEvent(savedUser.getId(), savedUser.getUsername())
        );

        return userAssembler.toDTO(savedUser);
    }
}
```

#### 工厂 (Factory)
```java
/**
 * 用户工厂
 * 作用：封装复杂对象的创建逻辑
 */
public class UserFactory {

    /**
     * 创建用户
     */
    public static User create(CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());

        // 初始化创建时间
        user.initCreateTime();

        // 业务校验
        user.validateForCreate();

        return user;
    }

    /**
     * 从数据库实体创建领域对象
     */
    public static User fromEntity(UserEntity entity) {
        // 转换逻辑
    }
}
```

---

## 2. 项目分层架构

### 2.1 分层原则

```
┌─────────────────────────────────────────┐
│         Interface Layer (接口层)          │
│   - REST Controller                     │
│   - Feign Client                        │
│   - Configuration                       │
├─────────────────────────────────────────┤
│      Application Layer (应用层)          │
│   - Application Service                 │
│   - Assembler/Converter                 │
│   - DTO                                 │
├─────────────────────────────────────────┤
│         Domain Layer (领域层)            │
│   - Entity (实体)                       │
│   - Value Object (值对象)                │
│   - Domain Service (领域服务)            │
│   - Repository Interface (仓储接口)      │
│   - Domain Event (领域事件)              │
├─────────────────────────────────────────┤
│    Infrastructure Layer (基础设施层)      │
│   - Repository Implementation           │
│   - External Service Client             │
│   - Persistence Mapping                 │
│   - Technical Components                │
└─────────────────────────────────────────┘
```

### 2.2 依赖规则

**核心原则**: 上层依赖下层，下层不依赖上层

```
Interface → Application → Domain ← Infrastructure
```

**依赖倒置**:
- Domain 层定义接口（如 Repository）
- Infrastructure 层实现接口
- 使用依赖注入解耦

### 2.3 各层职责

#### Interface 层（接口层）
```java
/**
 * 用户控制器
 * 职责：
 * 1. 接收HTTP请求
 * 2. 参数校验（@Valid）
 * 3. 调用应用服务
 * 4. 返回响应
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserAppService userAppService;

    @PostMapping
    public ApiResponse<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDTO userDTO = userAppService.createUser(request);
        return ApiResponse.success("用户创建成功", userDTO);
    }
}
```

#### Application 层（应用层）
```java
/**
 * 用户应用服务
 * 职责：
 * 1. 用例编排
 * 2. DTO转换
 * 3. 事务管理
 * 4. 调用领域服务
 */
@Service
@RequiredArgsConstructor
public class UserAppService {

    private final UserRepository userRepository;
    private final UserAssembler userAssembler;

    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        // 1. 业务校验
        checkUsernameUnique(request.getUsername());

        // 2. 转换为领域对象
        User user = userAssembler.toEntity(request);

        // 3. 调用领域行为
        user.validateForCreate();

        // 4. 保存
        User savedUser = userRepository.save(user);

        // 5. 转换为DTO
        return userAssembler.toDTO(savedUser);
    }
}
```

#### Domain 层（领域层）
```java
/**
 * 用户实体（领域模型）
 * 职责：
 * 1. 封装业务规则
 * 2. 实现领域行为
 * 3. 保持业务不变性
 */
@Entity
public class User {

    private Long id;
    private String username;
    private String email;

    /**
     * 领域行为：修改邮箱
     */
    public void changeEmail(String newEmail) {
        validateEmail(newEmail);
        this.email = newEmail;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 业务规则：邮箱格式校验
     */
    private void validateEmail(String email) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BusinessException("邮箱格式不正确");
        }
    }
}
```

#### Infrastructure 层（基础设施层）
```java
/**
 * 用户仓储实现
 * 职责：
 * 1. 实现仓储接口
 * 2. 数据持久化
 * 3. 数据转换
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity saved = jpaRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id)
            .map(userMapper::toDomain);
    }
}
```

---

## 3. 模块划分规范

### 3.1 项目模块结构

```
dms2-project/
├── dms2-api/              # API模块（可被外部引用）
│   ├── dto/
│   │   ├── request/       # 请求DTO
│   │   ├── response/      # 响应DTO
│   │   └── common/        # 通用DTO（PageRequest, ApiResponse）
│   ├── feign/             # Feign Client定义
│   └── config/            # Feign配置
│
├── dms2-domain/           # 领域模块（核心业务逻辑）
│   ├── model/
│   │   ├── entity/        # 实体
│   │   ├── valueobject/   # 值对象
│   │   └── aggregate/     # 聚合
│   ├── service/           # 领域服务
│   ├── repository/        # 仓储接口
│   ├── event/             # 领域事件
│   └── exception/         # 领域异常
│
├── dms2-application/      # 应用模块（用例编排）
│   ├── service/           # 应用服务
│   ├── assembler/         # DTO转换器
│   └── handler/           # 事件处理器
│
├── dms2-infrastructure/   # 基础设施模块（技术实现）
│   ├── repository/        # 仓储实现
│   ├── client/            # 外部服务客户端
│   ├── mapping/           # 对象映射
│   └── config/            # 技术配置
│
└── dms2-interface/        # 接口模块（对外接口）
    ├── rest/              # REST控制器
    └── config/            # Spring配置
```

### 3.2 模块依赖关系

```
┌─────────────┐
│  interface  │
└──────┬──────┘
       │
       ↓
┌─────────────┐     ┌─────────┐
│ application │ ──→ │   api   │
└──────┬──────┘     └────┬────┘
       │                 │
       ↓                 │
┌─────────────┐          │
│   domain    │ ←────────┘
└──────┬──────┘
       │
       ↓ (依赖倒置)
┌─────────────┐
│infrastructure│
└─────────────┘
```

**依赖规则**:
1. interface → application → domain
2. api → domain (只有依赖，不反向)
3. infrastructure → domain (实现接口)
4. infrastructure → application (如需要)

### 3.3 模块职责

#### dms2-api
**目的**: 定义对外接口契约，可被其他项目依赖

```java
/**
 * API模块示例
 */
package com.example.dms2.api;

// 1. DTO - 数据传输对象
public class UserDTO {
    private Long id;
    private String username;
    private String email;
}

// 2. Request - 请求DTO
public class CreateUserRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;
}

// 3. Response - 响应DTO
public class UserResponse {
    private Long id;
    private String username;
    private LocalDateTime createTime;
}

// 4. Feign Client - 服务调用接口
@FeignClient(name = "dms2-service", path = "/api/users")
public interface UserFeignClient {
    @GetMapping("/{id}")
    ApiResponse<UserDTO> getUserById(@PathVariable("id") Long id);

    @PostMapping
    ApiResponse<UserDTO> createUser(@RequestBody CreateUserRequest request);
}
```

#### dms2-domain
**目的**: 封装核心业务逻辑，与具体技术无关

```java
/**
 * 领域模块示例
 */
package com.example.dms2.domain;

// 1. 实体 - 有唯一标识的领域对象
@Entity
public class User {
    private Long id;
    private String username;
    private String email;

    // 领域行为
    public void changeEmail(String newEmail) {
        validateEmail(newEmail);
        this.email = newEmail;
    }
}

// 2. 值对象 - 不可变的领域对象
@Value
public class Email {
    String value;

    public Email(String value) {
        if (!isValid(value)) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        this.value = value;
    }
}

// 3. 仓储接口 - 定义数据访问契约
public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
}

// 4. 领域服务 - 不属于实体的业务逻辑
@DomainService
public class UserDomainService {
    public boolean isUsernameUnique(String username) {
        // 业务逻辑
    }
}

// 5. 领域事件 - 表示领域内发生的重要事情
public class UserCreatedEvent extends DomainEvent {
    private final Long userId;
    private final String username;
}
```

#### dms2-application
**目的**: 编排用例，协调领域对象

```java
/**
 * 应用模块示例
 */
package com.example.dms2.application;

// 1. 应用服务 - 用例编排
@Service
public class UserAppService {

    private final UserRepository userRepository;
    private final UserAssembler userAssembler;

    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        // 1. 业务校验
        checkUsernameUnique(request.getUsername());

        // 2. 转换为领域对象
        User user = userAssembler.toEntity(request);

        // 3. 执行领域行为
        user.validateForCreate();

        // 4. 持久化
        User savedUser = userRepository.save(user);

        // 5. 转换为DTO返回
        return userAssembler.toDTO(savedUser);
    }
}

// 2. Assembler - DTO转换器
@Component
public class UserAssembler {

    public User toEntity(CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        return user;
    }

    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
```

#### dms2-infrastructure
**目的**: 技术实现，对接外部系统

```java
/**
 * 基础设施模块示例
 */
package com.example.dms2.infrastructure;

// 1. 仓储实现 - 实现领域层定义的接口
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity saved = jpaRepository.save(entity);
        return userMapper.toDomain(saved);
    }
}

// 2. 外部服务客户端
@Component
public class EmailServiceClient {

    private final RestTemplate restTemplate;

    public void sendEmail(String to, String subject, String content) {
        // 调用邮件服务
    }
}

// 3. 持久化映射
@Mapper
public interface UserMapper {
    UserEntity toEntity(User user);
    User toDomain(UserEntity entity);
}
```

#### dms2-interface
**目的**: 对外暴露接口，处理请求响应

```java
/**
 * 接口模块示例
 */
package com.example.dms2.interface;

// 1. REST控制器
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserAppService userAppService;

    @PostMapping
    public ApiResponse<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDTO userDTO = userAppService.createUser(request);
        return ApiResponse.success(userDTO);
    }
}

// 2. 全局异常处理
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        return ApiResponse.error(e.getCode(), e.getMessage());
    }
}
```

---

## 4. 命名规范

### 4.1 类命名

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| 实体 | 业务名词 | `User`, `Order`, `Product` |
| 值对象 | 业务名词 | `Email`, `Money`, `Address` |
| DTO | 业务名词 + DTO | `UserDTO`, `OrderDTO` |
| Request | 动词 + 名词 + Request | `CreateUserRequest` |
| Response | 名词 + Response | `UserResponse` |
| 仓储接口 | 名词 + Repository | `UserRepository` |
| 仓储实现 | 名词 + RepositoryImpl | `UserRepositoryImpl` |
| 应用服务 | 名词 + AppService | `UserAppService` |
| 领域服务 | 名词 + DomainService | `UserDomainService` |
| 控制器 | 名词 + Controller | `UserController` |
| Feign Client | 名词 + FeignClient | `UserFeignClient` |
| Assembler | 名词 + Assembler | `UserAssembler` |
| 异常 | 业务名词 + Exception | `UserNotFoundException` |

### 4.2 方法命名

```java
// 查询方法
User findById(Long id);
Optional<User> findByUsername(String username);
List<User> findAll();
boolean existsByUsername(String username);
long countByStatus(String status);

// 创建方法
User create(CreateUserRequest request);
User save(User user);

// 更新方法
void updateEmail(Long id, String newEmail);
User update(Long id, UpdateUserRequest request);

// 删除方法
void deleteById(Long id);
void remove(User user);

// 业务方法
void changeEmail(String newEmail);
void validateForCreate();
boolean isUsernameUnique(String username);
```

### 4.3 变量命名

```java
// 实体变量 - 小写字母开头
User user;
Order order;

// 集合变量 - 复数形式
List<User> users;
Set<Order> orders;
Map<Long, User> userMap;

// DTO变量 - 以DTO结尾
UserDTO userDTO;
CreateUserRequest request;

// 布尔变量 - is/has/can开头
boolean isActive;
boolean hasPermission;
boolean canDelete;

// 常量 - 全大写，下划线分隔
private static final int MAX_SIZE = 100;
private static final String DEFAULT_ROLE = "USER";
```

### 4.4 包命名

```
com.example.dms2
├── api                    # API层
│   ├── dto               # 数据传输对象
│   │   ├── request       # 请求DTO
│   │   ├── response      # 响应DTO
│   │   └── common        # 通用DTO
│   ├── feign             # Feign客户端
│   └── config            # API配置
│
├── domain                # 领域层
│   ├── model             # 领域模型
│   │   ├── entity        # 实体
│   │   ├── valueobject   # 值对象
│   │   └── aggregate     # 聚合
│   ├── service           # 领域服务
│   ├── repository        # 仓储接口
│   ├── event             # 领域事件
│   └── exception         # 领域异常
│
├── application           # 应用层
│   ├── service           # 应用服务
│   ├── assembler         # 对象转换器
│   └── handler           # 事件处理器
│
├── infrastructure        # 基础设施层
│   ├── repository        # 仓储实现
│   ├── client            # 外部客户端
│   ├── mapping           # 对象映射
│   └── config            # 技术配置
│
└── interface             # 接口层
    ├── rest              # REST控制器
    └── config            # Spring配置
```

---

## 5. 代码组织规范

### 5.1 实体组织

```java
/**
 * 用户实体
 *
 * 结构：
 * 1. 类注释
 * 2. 字段声明
 * 3. 构造方法
 * 4. 领域行为
 * 5. 业务规则
 * 6. Getters/Setters
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // ===== 字段声明 =====

    /**
     * 用户ID（唯一标识）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * 邮箱
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


    // ===== 构造方法 =====
    // Lombok @Builder 会生成


    // ===== 领域行为 =====

    /**
     * 修改邮箱
     *
     * @param newEmail 新邮箱
     */
    public void changeEmail(String newEmail) {
        validateEmail(newEmail);
        this.email = newEmail;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 修改手机号
     *
     * @param newPhone 新手机号
     */
    public void changePhone(String newPhone) {
        validatePhone(newPhone);
        this.phone = newPhone;
        this.updateTime = LocalDateTime.now();
    }


    // ===== 业务规则 =====

    /**
     * 创建时的业务校验
     */
    public void validateForCreate() {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException("邮箱不能为空");
        }
        validateEmail(email);
    }

    /**
     * 邮箱格式校验
     */
    private void validateEmail(String email) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BusinessException("邮箱格式不正确");
        }
    }

    /**
     * 手机号格式校验
     */
    private void validatePhone(String phone) {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }
    }


    // ===== 生命周期方法 =====

    /**
     * 初始化创建时间
     */
    @PrePersist
    public void initCreateTime() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 更新修改时间
     */
    @PreUpdate
    public void updateUpdateTime() {
        this.updateTime = LocalDateTime.now();
    }
}
```

### 5.2 应用服务组织

```java
/**
 * 用户应用服务
 *
 * 职责：
 * 1. 用例编排
 * 2. DTO转换
 * 3. 事务管理
 * 4. 调用领域服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAppService {

    // ===== 依赖注入 =====

    private final UserRepository userRepository;
    private final UserAssembler userAssembler;
    private final UserDomainService userDomainService;


    // ===== 查询方法 =====

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户DTO
     */
    public UserDTO getUserById(Long id) {
        log.info("查询用户: id={}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new BusinessException("用户不存在: id=" + id));

        return userAssembler.toDTO(user);
    }


    // ===== 命令方法 =====

    /**
     * 创建用户
     *
     * @param request 创建请求
     * @return 用户DTO
     */
    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        log.info("创建用户: request={}", request);

        // 1. 业务校验
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在: " + request.getUsername());
        }

        // 2. 转换为领域对象
        User user = userAssembler.toEntity(request);

        // 3. 执行领域行为
        user.validateForCreate();

        // 4. 持久化
        User savedUser = userRepository.save(user);

        // 5. 发布领域事件（可选）
        // eventPublisher.publishEvent(new UserCreatedEvent(savedUser.getId()));

        log.info("用户创建成功: id={}", savedUser.getId());
        return userAssembler.toDTO(savedUser);
    }


    // ===== 更新方法 =====

    /**
     * 更新用户邮箱
     *
     * @param id 用户ID
     * @param newEmail 新邮箱
     */
    @Transactional
    public void updateEmail(Long id, String newEmail) {
        log.info("更新用户邮箱: id={}, newEmail={}", id, newEmail);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new BusinessException("用户不存在: id=" + id));

        // 调用领域行为
        user.changeEmail(newEmail);

        // 持久化
        userRepository.save(user);

        log.info("用户邮箱更新成功: id={}", id);
    }


    // ===== 删除方法 =====

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("删除用户: id={}", id);

        if (!userRepository.findById(id).isPresent()) {
            throw new BusinessException("用户不存在: id=" + id);
        }

        userRepository.deleteById(id);

        log.info("用户删除成功: id={}", id);
    }
}
```

### 5.3 控制器组织

```java
/**
 * 用户REST控制器
 *
 * 职责：
 * 1. 接收HTTP请求
 * 2. 参数校验
 * 3. 调用应用服务
 * 4. 返回响应
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController implements UserService {

    // ===== 依赖注入 =====

    private final UserAppService userAppService;


    // ===== 查询接口 =====

    @Override
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户")
    public ApiResponse<UserDTO> getUserById(
        @PathVariable("id") Long id
    ) {
        log.info("REST请求: 查询用户, id={}", id);

        UserDTO userDTO = userAppService.getUserById(id);

        return ApiResponse.success(userDTO);
    }


    // ===== 命令接口 =====

    @Override
    @PostMapping
    @Operation(summary = "创建用户")
    public ApiResponse<UserDTO> createUser(
        @Valid @RequestBody CreateUserRequest request
    ) {
        log.info("REST请求: 创建用户, request={}", request);

        UserDTO userDTO = userAppService.createUser(request);

        return ApiResponse.success("用户创建成功", userDTO);
    }


    // ===== 更新接口 =====

    @PutMapping("/{id}/email")
    @Operation(summary = "更新用户邮箱")
    public ApiResponse<Void> updateEmail(
        @PathVariable("id") Long id,
        @RequestBody @Valid UpdateEmailRequest request
    ) {
        log.info("REST请求: 更新用户邮箱, id={}, email={}", id, request.getEmail());

        userAppService.updateEmail(id, request.getEmail());

        return ApiResponse.success("邮箱更新成功");
    }


    // ===== 删除接口 =====

    @Override
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public ApiResponse<Void> deleteUser(
        @PathVariable("id") Long id
    ) {
        log.info("REST请求: 删除用户, id={}", id);

        userAppService.deleteUser(id);

        return ApiResponse.success("用户删除成功");
    }
}
```

---

## 6. 开发流程规范

### 6.1 需求分析流程

```
1. 需求收集
   ↓
2. 识别领域和子域
   ↓
3. 划定限界上下文
   ↓
4. 定义上下文映射
   ↓
5. 建立领域模型
   ↓
6. 设计聚合边界
```

### 6.2 设计阶段

#### 步骤1: 识别聚合
```
问题：哪些业务概念应该组织在一起？
方法：
- 找出强关联的对象
- 确定一致性边界
- 选择聚合根

示例：
- 订单聚合：Order(根) + OrderItem + ShippingAddress
- 用户聚合：User(根) + UserProfile + Preference
```

#### 步骤2: 设计仓储接口
```
位置：domain/repository
原则：
- 只定义在领域层
- 接口不依赖技术细节
- 以聚合根为单位

示例：
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findByUserId(Long userId);
}
```

#### 步骤3: 定义应用服务
```
位置：application/service
原则：
- 一个用例 = 一个方法
- 方法名表达业务意图
- 负责事务边界

示例：
public class OrderAppService {
    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        // 用例编排
    }
}
```

### 6.3 开发阶段

#### 步骤1: 创建领域模型
```java
// 1. 在 domain/model/entity 创建实体
@Entity
public class Order {
    private Long id;
    private List<OrderItem> items;
}

// 2. 在 domain/repository 创建仓储接口
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
}
```

#### 步骤2: 实现基础设施
```java
// 1. 在 infrastructure/repository 创建实现
@Repository
public class OrderRepositoryImpl implements OrderRepository {
    // 实现细节
}

// 2. 在 infrastructure/mapping 创建映射
@Mapper
public interface OrderMapper {
    OrderEntity toEntity(Order order);
    Order toDomain(OrderEntity entity);
}
```

#### 步骤3: 实现应用服务
```java
// 在 application/service 创建应用服务
@Service
public class OrderAppService {
    private final OrderRepository orderRepository;

    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        // 编排用例
    }
}
```

#### 步骤4: 暴露接口
```java
// 在 interface/rest 创建控制器
@RestController
public class OrderController {
    private final OrderAppService orderAppService;

    @PostMapping
    public ApiResponse<OrderDTO> createOrder(@RequestBody CreateOrderRequest request) {
        // 处理请求
    }
}
```

### 6.4 测试阶段

#### 单元测试
```java
@ExtendWith(MockitoExtension.class)
class OrderAppServiceTest {

    @Mock private OrderRepository orderRepository;

    @InjectMocks private OrderAppService orderAppService;

    @Test
    void createOrder_Success() {
        // Given
        CreateOrderRequest request = new CreateOrderRequest();
        // ...

        // When
        OrderDTO result = orderAppService.createOrder(request);

        // Then
        assertThat(result).isNotNull();
        verify(orderRepository).save(any(Order.class));
    }
}
```

#### 集成测试
```java
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerE2ETest {

    @Autowired private MockMvc mockMvc;

    @Test
    void createOrder_Success() throws Exception {
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }
}
```

---

## 7. 最佳实践

### 7.1 聚合设计原则

#### 原则1: 小聚合
```
✅ 推荐：Order + OrderItem（一个订单及订单项）
❌ 避免：Order + OrderItem + Product + User + Payment（聚合过大）

原因：
- 减少并发冲突
- 提高性能
- 简化一致性维护
```

#### 原则2: 聚合之间通过ID引用
```
✅ 推荐：
class Order {
    private Long userId;  // 引用用户ID
}

❌ 避免：
class Order {
    private User user;  // 直接引用用户对象
}

原因：
- 避免聚合边界模糊
- 减少加载开销
- 便于分布式部署
```

#### 原则3: 保持聚合一致性
```
✅ 推荐：通过聚合根修改
Order order = orderRepository.findById(1L);
order.addItem(product, 2);  // 通过聚合根操作
orderRepository.save(order);

❌ 避免：直接操作内部对象
Order order = orderRepository.findById(1L);
List<OrderItem> items = order.getItems();
items.add(new OrderItem(...));  // 绕过聚合根
```

### 7.2 事务设计原则

#### 原则1: 一个事务 = 一个聚合
```
✅ 推荐：
@Transactional
public void createOrder(CreateOrderRequest request) {
    Order order = Order.create(request);
    orderRepository.save(order);  // 只保存一个聚合
}

❌ 避免：
@Transactional
public void createOrderAndUser(...) {
    User user = new User();
    userRepository.save(user);  // 保存用户聚合

    Order order = new Order();
    orderRepository.save(order);  // 保存订单聚合
}
```

#### 原则2: 事务边界 = 应用服务边界
```
✅ 推荐：
@Service
public class OrderAppService {
    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        // 整个用例在一个事务中
    }
}

❌ 避免：
@Service
public class OrderAppService {
    public OrderDTO createOrder(CreateOrderRequest request) {
        createOrderHeader(request);  // 独立事务
        addOrderItems(request);     // 独立事务
    }
}
```

### 7.3 DTO转换原则

#### 原则1: 隔离层转换
```
✅ 推荐：使用Assembler
@Component
public class UserAssembler {
    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }
}

❌ 避免：在实体中添加转换方法
@Entity
public class User {
    public UserDTO toDTO() {
        // 实体不应该知道DTO的存在
    }
}
```

#### 原则2: 使用映射工具
```java
// 使用 MapStruct
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);
    User toEntity(CreateUserRequest request);
}

// 使用 Mapstruct + MapStruct
```

### 7.4 异常处理原则

#### 原则1: 使用领域异常
```
✅ 推荐：
public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(Long userId) {
        super(404, "用户不存在: " + userId);
    }
}

// 使用
throw new UserNotFoundException(userId);

❌ 避免：
throw new RuntimeException("用户不存在");
throw new IllegalArgumentException("用户不存在");
```

#### 原则2: 异常转换为HTTP响应
```
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleUserNotFoundException(UserNotFoundException e) {
        return ApiResponse.error(404, e.getMessage());
    }
}
```

---

## 8. 常见问题

### Q1: 什么时候使用领域服务？

**场景1: 跨实体的业务逻辑**
```java
@DomainService
public class OrderDomainService {
    /**
     * 计算订单折扣（涉及用户、商品、优惠券等多个聚合）
     */
    public Money calculateDiscount(Order order, User user, List<Coupon> coupons) {
        // 业务逻辑
    }
}
```

**场景2: 无状态的业务逻辑**
```java
@DomainService
public class PasswordDomainService {
    /**
     * 密码加密
     */
    public String encrypt(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }
}
```

**场景3: 领域逻辑的编排**
```java
@DomainService
public class OrderFulfillmentService {
    /**
     * 订单履约流程
     */
    public void fulfillOrder(Order order) {
        reserveInventory(order);
        processPayment(order);
        arrangeShipment(order);
    }
}
```

### Q2: 实体和值对象如何选择？

**选择实体的场景**:
```java
// ✅ 有唯一标识
User user = new User(1L);

// ✅ 有生命周期
user.setStatus(Status.ACTIVE);
user.setUpdateTime(now);

// ✅ 可变性
user.changeEmail("new@example.com");
```

**选择值对象的场景**:
```java
// ✅ 不可变
@Value
class Email {
    String value;
}

// ✅ 替换而不是修改
user.setEmail(new Email("new@example.com"));

// ✅ 相等性基于所有属性
Email e1 = new Email("test@example.com");
Email e2 = new Email("test@example.com");
e1.equals(e2);  // true
```

### Q3: 如何处理跨聚合查询？

**方案1: 通过应用服务编排**
```java
@Service
public class OrderAppService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderDetailDTO getOrderDetail(Long orderId) {
        // 查询订单聚合
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        // 查询用户聚合
        User user = userRepository.findById(order.getUserId())
            .orElse(null);

        // 组装DTO
        return OrderDetailDTO.builder()
            .order(order)
            .user(user)
            .build();
    }
}
```

**方案2: 使用CQRS（读写分离）**
```java
// 写模型：聚合
@Entity
public class Order {
    private Long id;
    private Long userId;
    // ...
}

// 读模型：专门查询
@Projection
public class OrderSummary {
    private Long orderId;
    private String userName;
    private BigDecimal totalAmount;
    // 联合查询结果
}
```

### Q4: 如何处理性能问题？

**方案1: 懒加载**
```java
@Entity
public class Order {
    @OneToMany(fetch = FetchType.LAZY)
    private List<OrderItem> items;
}
```

**方案2: 缓存**
```java
@Cacheable("users")
public User findById(Long id) {
    return userRepository.findById(id);
}
```

**方案3: 异步处理**
```java
@Service
public class OrderAppService {

    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        Order order = Order.create(request);
        Order savedOrder = orderRepository.save(order);

        // 异步发布事件
        eventPublisher.publishEvent(new OrderCreatedEvent(savedOrder.getId()));

        return orderAssembler.toDTO(savedOrder);
    }
}

@EventListener
@Async
public void handleOrderCreated(OrderCreatedEvent event) {
    // 发送通知、统计等非核心逻辑
}
```

---

## 附录

### A. 参考资源

- **领域驱动设计** - Eric Evans
- **实现领域驱动设计** - Vaughn Vernon
- **DDD实战** - 基于《实现领域驱动设计》的示例项目

### B. 工具推荐

- **建模工具**: PlantUML, Mermaid
- **代码生成**: MapStruct, Lombok
- **测试**: JUnit 5, Mockito, ArchUnit
- **文档**: Swagger/OpenAPI, SpringDoc

### C. 项目模板

本项目可作为DDD开发的参考模板，包含：
- 完整的分层结构
- 示例代码
- 测试用例
- 配置文件

### D. 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-02-07 | 初始版本 |

---

**文档维护**: DMS2 Team
**最后更新**: 2026-02-07
**版本**: 1.0.0
