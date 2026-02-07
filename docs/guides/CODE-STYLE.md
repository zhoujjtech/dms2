# 代码规范指南

## 概述

本项目使用以下工具来确保代码质量和格式一致性：

- **EditorConfig**: 统一编辑器配置
- **Spotless**: 自动代码格式化（基于 Google Java Format）
- **Checkstyle**: 代码质量检查

## 代码格式规范

### 基本规范（基于 Google Java Style）

- **缩进**: 4 个空格
- **行宽**: 最大 120 字符
- **编码**: UTF-8
- **换行符**: LF (Unix 风格)
- **文件末尾**: 必须有空行
- **尾随空格**: 禁止

### 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | PascalCase | `UserService`, `UserDTO` |
| 方法名 | camelCase | `getUserById()`, `createUser()` |
| 变量名 | camelCase | `userName`, `userId` |
| 常量名 | UPPER_SNAKE_CASE | `MAX_SIZE`, `DEFAULT_VALUE` |
| 包名 | 全小写 | `com.example.dms2.domain` |

### 导入规范

导入顺序：
1. `java.*`
2. `javax.*`
3. 第三方库（`org.*`, `com.*`）
4. 项目内部包
5. 静态导入

每组之间用空行分隔。

## 工具使用

### 1. 自动格式化代码

#### 使用 Spotless（推荐）

```bash
# 检查代码格式（不修改文件）
mvn spotless:check

# 自动格式化代码
mvn spotless:apply

# 只格式化特定模块
mvn spotless:apply -pl dms2-domain
```

#### 在 IDEA 中使用

1. 安装插件：
   - `Settings/Preferences` → `Plugins` → 搜索 `Spotless`
   - 安装 `Spotless` 插件（可选）

2. 导入代码风格：
   - `Settings/Preferences` → `Editor` → `Code Style` → `Java`
   - 点击齿轮图标 → `Import Scheme` → 选择 `idea-codestyle.xml`

3. 格式化代码：
   - 选中文件或目录
   - `Ctrl+Alt+L` (Windows/Linux) 或 `Cmd+Option+L` (Mac)

### 2. 代码质量检查

```bash
# 运行 Checkstyle 检查
mvn checkstyle:check

# 生成 Checkstyle 报告
mvn checkstyle:checkstyle

# 在浏览器中查看报告
open dms2-interface/target/site/checkstyle.html
```

### 3. Git 提交前自动检查

配置 Git Hooks（可选）：

```bash
# 在项目根目录执行
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/sh
mvn spotless:check
if [ $? -ne 0 ]; then
    echo "代码格式不符合规范，请运行: mvn spotless:apply"
    exit 1
fi
EOF

chmod +x .git/hooks/pre-commit
```

## IDEA 配置建议

### 1. 启用 EditorConfig 支持

`Settings/Preferences` → `Editor` → `Code Style` → 勾选 `Enable EditorConfig support`

### 2. 配置保存时动作

`Settings/Preferences` → `Tools` → `Actions on Save`:
- 勾选 `Reformat code`
- 勾选 `Optimize imports`
- 勾选 `Remove unused imports`

### 3. 配置代码检查

`Settings/Preferences` → `Editor` → `Inspections` → `Java`:
- 启用 `Declaration redundancy` → `Unused declaration`
- 启用 `Code maturity` → `Raw use of parameterized class`

### 4. 配置自动导入

`Settings/Preferences` → `Editor` → `General` → `Auto Import`:
- 勾选 `Optimize imports on the fly`
- 勾选 `Add unambiguous imports on the fly`

## 编码最佳实践

### 1. 类和方法结构

```java
// 1. 常量
private static final int MAX_SIZE = 100;

// 2. 静态变量
private static String instanceName;

// 3. 实例变量
private final UserRepository userRepository;

// 4. 构造方法
public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
}

// 5. 公共方法
public UserDTO getUserById(Long id) {
    // ...
}

// 6. 私有方法
private void validateUser(User user) {
    // ...
}
```

### 2. 异常处理

```java
// 推荐：使用具体的异常类型
if (user == null) {
    throw new BusinessException("用户不存在: id=" + id);
}

// 不推荐：使用通用的异常
if (user == null) {
    throw new RuntimeException("用户不存在");
}
```

### 3. 日志规范

```java
// 推荐：使用占位符
log.info("查询用户: id={}", id);
log.warn("业务异常: {}", e.getMessage());

// 不推荐：字符串拼接
log.info("查询用户: id=" + id);
```

### 4. 集合处理

```java
// 推荐：使用 Stream API
List<String> names = users.stream()
    .map(User::getName)
    .filter(Objects::nonNull)
    .collect(Collectors.toList());

// 不推荐：使用传统循环
List<String> names = new ArrayList<>();
for (User user : users) {
    if (user.getName() != null) {
        names.add(user.getName());
    }
}
```

### 5. Optional 使用

```java
// 推荐：使用 Optional
Optional<User> user = userRepository.findById(id);
return user.map(UserDTO::fromEntity)
           .orElseThrow(() -> new BusinessException("用户不存在"));

// 不推荐：直接使用 null
User user = userRepository.findById(id);
if (user == null) {
    throw new BusinessException("用户不存在");
}
return UserDTO.fromEntity(user);
```

## CI/CD 集成

在 Jenkins/GitHub Actions/GitLab CI 中添加代码检查步骤：

```yaml
# .github/workflows/ci.yml 示例
- name: Check code format
  run: mvn spotless:check

- name: Run Checkstyle
  run: mvn checkstyle:check

- name: Build
  run: mvn clean package
```

## 常见问题

### Q: 如何跳过代码检查？

```bash
# 跳过 Spotless 检查
mvn clean package -Dspotless.check.skip

# 跳过 Checkstyle 检查
mvn clean package -Dcheckstyle.skip
```

### Q: IDEA 格式化和 Spotless 格式化不一致？

确保导入了 `idea-codestyle.xml` 配置文件：
1. `Settings/Preferences` → `Editor` → `Code Style` → `Java`
2. 点击齿轮图标 → `Import Scheme` → `Import IntelliJ IDEA code style`
3. 选择项目根目录的 `idea-codestyle.xml`

### Q: 如何处理第三方库的格式问题？

在 `pom.xml` 的 Spotless 配置中添加：
```xml
<java>
    <googleJavaFormat />
    <licenseHeaderFile>${basedir}/LICENSE HEADER</licenseHeaderFile>
    <!-- 跳过某些文件 -->
    <excludes>
        <exclude>**/generated/**</exclude>
    </excludes>
</java>
```

## 参考资料

- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Spotless 官方文档](https://github.com/diffplug/spotless)
- [Checkstyle 官方文档](https://checkstyle.org/)
- [EditorConfig 官方网站](https://editorconfig.org/)
