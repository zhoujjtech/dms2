# 如何使用 DDD 开发规范

## 📚 文档导航

本项目的 DDD 开发规范包含以下文档：

| 文档 | 用途 | 适用人群 |
|------|------|---------|
| **DDD-DEVELOPMENT-STANDARD.md** | DDD 开发规范手册，包含完整的设计原则、编码规范 | 所有开发人员 |
| **DDD-OPERATIONS-MANUAL.md** | DDD 操作手册，详细的开发流程和步骤 | 开发人员、技术主管 |
| **CODE-STYLE.md** | 代码格式规范和工具使用指南 | 所有开发人员 |
| **QUICK-START.md** | 快速使用指南 | 新团队成员 |

---

## 🚀 快速开始

### 新手入门（第1-2周）

#### 1. 阅读核心概念
```
必读章节：
- DDD-DEVELOPMENT-STANDARD.md
  - 1. DDD 核心概念
  - 2. 项目分层架构
  - 4. 命名规范

预期目标：
- 理解 DDD 的核心概念
- 掌握项目分层结构
- 熟悉命名规范
```

#### 2. 环境准备
```bash
# 1. 安装开发工具
- JDK 17+
- Maven 3.6+
- IntelliJ IDEA 2023+
- Git

# 2. 配置 IDEA
- 导入代码风格 (idea-codestyle.xml)
- 启用 EditorConfig 支持
- 配置保存时自动格式化

# 3. 克隆项目
git clone <repository-url>
cd dms2-project
mvn clean install
```

#### 3. 运行项目
```bash
# 启动 Nacos（如需要）
# 参考 README.md 中的说明

# 运行应用
cd dms2-interface
mvn spring-boot:run

# 访问应用
# http://localhost:8080
# http://localhost:8080/swagger-ui/index.html
```

---

### 进阶开发（第3-4周）

#### 1. 学习开发流程
```
阅读章节：
- DDD-OPERATIONS-MANUAL.md
  - 3. 领域建模
  - 4. 代码实现
  - 5. 测试编写

实践任务：
- 完成一个简单的 CRUD 功能
- 编写单元测试
- 编写 E2E 测试
```

#### 2. 参与代码审查
```
学习内容：
- 代码审查清单
- 架构测试
- 代码质量检查

参与方式：
- 提交 Pull Request
- 参与同行评审
- 修复审查意见
```

---

### 熟练开发（第5周+）

#### 1. 负责完整功能
```
独立完成：
1. 需求分析
2. 领域建模
3. 代码实现
4. 测试编写
5. 文档更新
```

#### 2. 指导新人
```
分享经验：
- 代码审查
- 技术分享
- 文档维护
```

---

## 📖 文档使用指南

### 场景1: 开发新功能

```
步骤：
1. 阅读 DDD-DEVELOPMENT-STANDARD.md
   - 了解相关的设计原则

2. 阅读 DDD-OPERATIONS-MANUAL.md
   - 按照操作手册实施

3. 参考 CODE-STYLE.md
   - 确保代码格式符合规范

4. 运行代码检查
   mvn spotless:apply
   mvn checkstyle:check
   mvn test
```

### 场景2: 代码审查

```
步骤：
1. 查看代码审查清单
   - DDD-OPERATIONS-MANUAL.md 第6章

2. 检查架构规则
   mvn test -Dtest=ArchitectureTest

3. 检查代码格式
   mvn spotless:check

4. 提供反馈意见
```

### 场景3: 解决问题

```
步骤：
1. 查看故障排查
   - DDD-OPERATIONS-MANUAL.md 附录B

2. 查看常见问题
   - DDD-DEVELOPMENT-STANDARD.md 第8章

3. 寻求帮助
   - 技术支持邮箱
   - GitHub Issues
```

---

## 🎯 学习路径

### 初级开发人员

**学习重点**:
1. DDD 核心概念
2. 分层架构
3. 命名规范
4. 基本编码

**推荐章节**:
- DDD-DEVELOPMENT-STANDARD.md: 第1-5章
- DDD-OPERATIONS-MANUAL.md: 第1-5章
- CODE-STYLE.md: 全部

### 中级开发人员

**学习重点**:
1. 复杂领域建模
2. 聚合设计
3. 事务管理
4. 性能优化

**推荐章节**:
- DDD-DEVELOPMENT-STANDARD.md: 第6-8章
- DDD-OPERATIONS-MANUAL.md: 第3-7章

### 高级开发人员/架构师

**学习重点**:
1. 战略设计
2. 上下文映射
3. 微服务拆分
4. 技术选型

**推荐章节**:
- DDD-DEVELOPMENT-STANDARD.md: 全部
- DDD-OPERATIONS-MANUAL.md: 全部
- 独立研究最佳实践

---

## 📋 日常开发检查清单

### 编码前

```
□ 理解需求
□ 识别聚合和实体
□ 设计领域模型
□ 定义仓储接口
□ 设计应用服务接口
```

### 编码中

```
□ 遵循分层架构
□ 使用正确的命名
□ 添加适当的注释
□ 处理异常情况
□ 编写业务逻辑
```

### 编码后

```
□ 格式化代码 (mvn spotless:apply)
□ 编写单元测试
□ 编写集成测试
□ 运行所有测试 (mvn test)
□ 代码自查
```

### 提交前

```
□ 代码格式检查 (mvn spotless:check)
□ 代码质量检查 (mvn checkstyle:check)
□ 所有测试通过 (mvn test)
□ 更新文档
□ 提交代码
```

---

## 🔧 常用命令速查

```bash
# === 项目构建 ===
mvn clean compile           # 编译
mvn clean test              # 运行测试
mvn clean package           # 打包
mvn clean install           # 安装到本地仓库

# === 代码格式化 ===
mvn spotless:check          # 检查格式
mvn spotless:apply          # 自动格式化

# === 代码质量 ===
mvn checkstyle:check        # 检查代码质量
mvn checkstyle:checkstyle   # 生成报告

# === 运行应用 ===
mvn spring-boot:run         # 启动应用

# === 跳过检查 ===
mvn clean package -Dspotless.check.skip -Dcheckstyle.skip
```

---

## 🤝 团队协作

### 代码提交流程

```
1. 创建功能分支
   git checkout -b feature/xxx

2. 开发功能
   - 按照规范编码
   - 编写测试
   - 本地验证

3. 提交代码
   git add .
   git commit -m "feat: xxx"

4. 推送分支
   git push origin feature/xxx

5. 创建 Pull Request
   - 填写 PR 模板
   - 关联相关 Issue

6. 代码审查
   - 至少一人评审
   - 修改反馈意见

7. 合并分支
   - 评审通过后合并
   - 删除功能分支
```

### 分支命名规范

```
feature/xxx     # 新功能
fix/xxx         # 修复bug
hotfix/xxx      # 紧急修复
refactor/xxx    # 重构
docs/xxx        # 文档更新
test/xxx        # 测试相关
```

### 提交信息规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 重构
test: 测试相关
chore: 构建/工具链相关

示例：
feat: 添加订单管理功能
fix: 修复用户创建时的邮箱校验问题
docs: 更新DDD开发规范文档
```

---

## 📞 获取帮助

### 遇到问题时

1. **查看文档**
   - 搜索相关章节
   - 查看常见问题

2. **查看示例代码**
   - 项目中的示例
   - 参考实现

3. **询问团队**
   - 技术讨论群
   - 代码审查时提问

4. **提交 Issue**
   - GitHub Issues
   - 详细描述问题

### 技术支持

- **邮箱**: support@example.com
- **文档**: DMS2 Team
- **GitHub**: https://github.com/example/dms2-project

---

## 📝 更新记录

| 日期 | 版本 | 说明 |
|------|------|------|
| 2026-02-07 | 1.0.0 | 初始版本 |

---

**维护团队**: DMS2 Team
**文档版本**: 1.0.0
**最后更新**: 2026-02-07
