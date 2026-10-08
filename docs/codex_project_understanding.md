# ObjectVerse 阶段记录

## 当前阶段

第 9.5 阶段：UML 文本规范化与生成代码可读性增强。

## 创建或修改的文件

1. `src/main/java/com/example/objectverse/service/impl/UmlServiceImpl.java`
   - 修复 UML 文本中属性和方法的可见性符号。
   - 属性不再使用 `*`。
   - 方法不再固定使用 `-`。

2. `src/main/resources/templates/uml/class-diagram.html`
   - 将底部文本区域标题改为“由当前项目模型生成的 UML 文本表示”。
   - 增加说明：该文本由 ObjectVerse 根据类、属性、方法和关系自动生成，可用于软件设计文档。
   - 明确不把 UML 文本称为 Java 代码。

3. `src/main/java/com/example/objectverse/strategy/BasicJavaClassGenerateStrategy.java`
   - 增加文件头注释、类注释、字段注释和方法注释。
   - 根据属性类型、方法返回值和方法参数自动生成 import。
   - 支持 `LocalDate`、`LocalDateTime`、`BigDecimal`、`List`、`Map` 的 import。
   - 对空方法体按返回类型生成安全默认返回值。

4. `src/main/java/com/example/objectverse/strategy/EncapsulationCodeGenerateStrategy.java`
   - 复用基础策略的 import、注释和默认方法体能力。
   - 保留封装策略特征：字段统一生成 `private`，通过 getter/setter 和公开方法访问对象状态。
   - 文件头标注生成策略为“封装代码生成策略”。

5. `src/main/resources/templates/code/preview.html`
   - 在代码预览上方增加“以下 Java 代码由 ObjectVerse 根据当前项目模型自动生成”说明。
   - 增加 AI 应用模型说明。
   - 代码块标题改为“生成的 Java 源代码”。

6. `src/main/resources/templates/ai/requirement.html`
   - 将结构化结果标题改为“本轮 AI 生成的结构化建模建议”。
   - 增加说明：这些内容尚未写入项目模型，点击“应用到当前项目模型”后才会进入设计器并在 UML 中显示。

7. `src/main/java/com/example/objectverse/service/impl/RequirementAiServiceImpl.java`
   - 防重复应用逻辑继续保留。
   - 增加 `[AI-APPLY]` 控制台日志，区分跳过重复类、属性、方法、关系和新增模型元素。

8. `docs/codex_project_understanding.md`
   - 覆盖更新为本阶段完成记录。

## UML 文本可见性符号如何修复

属性符号规则：

- `PUBLIC` -> `+`
- `PRIVATE` -> `-`
- `PROTECTED` -> `#`
- `PACKAGE`、`DEFAULT`、`PACKAGE_PRIVATE` -> `~`
- 其他或为空 -> `-`

方法符号规则：

- `PUBLIC` -> `+`
- `PRIVATE` -> `-`
- `PROTECTED` -> `#`
- `PACKAGE`、`DEFAULT`、`PACKAGE_PRIVATE` -> `~`
- 其他或为空 -> `+`

## Java 生成代码如何增加注释

生成代码现在包含：

- 文件头注释：说明由 ObjectVerse 自动生成、来源模型和生成策略。
- 类注释：说明类名、类说明和面向对象含义。
- 字段注释：使用属性说明或属性名。
- 方法注释：使用方法说明，并根据参数和返回值生成基础 Javadoc。

## LocalDateTime 等类型如何自动 import

代码生成策略会扫描属性类型、方法返回类型和方法参数类型，自动去重生成 import：

- `LocalDate` -> `java.time.LocalDate`
- `LocalDateTime` -> `java.time.LocalDateTime`
- `BigDecimal` -> `java.math.BigDecimal`
- `List`、`List<T>` -> `java.util.List`
- `Map`、`Map<K,V>` -> `java.util.Map`

`String`、`Integer`、`Double`、`Boolean`、`Long`、`Void` 等 `java.lang` 类型不会生成 import。

## 页面如何区分 AI 建议、已应用模型和生成代码

- AI 需求分析页面将结果区标注为“本轮 AI 生成的结构化建模建议”，并说明这些内容在点击应用前尚未写入项目模型。
- UML 页面标注为“由当前项目模型生成的 UML 文本表示”，表示内容来自已应用或手动维护后的项目模型。
- 代码预览页面标注为“生成的 Java 源代码”，说明代码由 ObjectVerse 根据当前项目模型自动生成。

## 编译验证

已运行：

```bash
.\mvnw.cmd -DskipTests compile
```

第一次普通运行时本机 JVM native memory 分配失败；随后使用较小 Maven JVM 参数重新运行并通过：

```bash
$env:MAVEN_OPTS='-Xms64m -Xmx192m -XX:ReservedCodeCacheSize=32m -XX:MaxMetaspaceSize=128m'
.\mvnw.cmd -DskipTests compile
```

结果：`BUILD SUCCESS`。
