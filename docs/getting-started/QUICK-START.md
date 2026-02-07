# 代码规范快速使用指南

## 常用命令

```bash
# 检查代码格式（不修改文件）
mvn spotless:check

# 自动格式化代码
mvn spotless:apply

# 检查代码质量
mvn checkstyle:check

# 跳过代码检查进行构建
mvn clean package -Dspotless.check.skip -Dcheckstyle.skip
```

## IDEA 配置步骤

### 1. 导入代码风格

1. 打开 `Settings/Preferences` → `Editor` → `Code Style` → `Java`
2. 点击齿轮图标 → `Import Scheme`
3. 选择项目根目录的 `idea-codestyle.xml`

### 2. 启用自动格式化

1. `Settings/Preferences` → `Tools` → `Actions on Save`
2. 勾选：
   - ✅ Reformat code
   - ✅ Optimize imports
   - ✅ Remove unused imports

### 3. 启用 EditorConfig

1. `Settings/Preferences` → `Editor` → `Code Style`
2. 勾选 ✅ `Enable EditorConfig support`

## 编码规范要点

### 格式规范
- **缩进**: 4 空格
- **行宽**: 最大 120 字符
- **换行符**: LF (Unix)

### 命名规范
- **类名**: `PascalCase` - `UserService`
- **方法名**: `camelCase` - `getUserById`
- **常量名**: `UPPER_SNAKE_CASE` - `MAX_SIZE`

### 导入顺序
```java
// 1. java.*
import java.util.List;

// 2. javax.*
import javax.annotation.Resource;

// 3. org.* (第三方库)
import org.springframework.stereotype.Service;

// 4. com.* (第三方库)
import com.google.common.collect.Lists;

// 5. 项目内部包
import com.example.dms2.domain.model.User;

// 6. 静态导入
import static org.junit.jupiter.api.Assertions.*;
```

## Git 提交前检查

每次提交代码前，建议执行：

```bash
# 1. 格式化代码
mvn spotless:apply

# 2. 运行测试
mvn test

# 3. 提交代码
git add .
git commit -m "your message"
```

## 详细文档

查看完整的代码规范文档：[CODE-STYLE.md](./CODE-STYLE.md)
