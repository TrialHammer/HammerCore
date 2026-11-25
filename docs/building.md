# 从源码构建

构建前，将上游的 `BreweryX-3.7.1.jar`、`ProjectKorra-1.12.0.jar` 和 `CoreProtect-24.1.jar` 放入 `libs/`，文件名须与 [pom.xml](../pom.xml) 一致。这些本地 API 依赖不会打入插件。

安装 Maven，并使用能读取目标 Paper API 的 JDK。在仓库根目录运行：

```bash
bash scripts/build.sh                     # Paper 26.3，默认目标
PAPER_API_VERSION=1.21.11 bash scripts/build.sh
```

脚本把 JAR 写入 `dist/`。选择与服务器对应的产物，不要混用不同 Paper 目标。项目依赖和字节码目标见 [pom.xml](../pom.xml)。仅运行测试可用 `mvn test`。
