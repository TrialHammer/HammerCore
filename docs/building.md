# 从源码构建

构建前，将上游的 `BreweryX-3.7.1.jar`、`ProjectKorra-1.12.0.jar`、`CoreProtect-24.1.jar`、`Nexo-1.30.jar` 和 `packetevents-spigot-2.14.0.jar` 放入 `libs/`，文件名须与 [pom.xml](../pom.xml) 一致。这些本地 API 依赖不会打入插件。运行时未安装 Nexo 或 PacketEvents 的服务器仍可使用其他 HammerCore 功能。

安装 Maven，并使用能读取目标 Paper API 的 JDK。在仓库根目录运行：

```bash
bash scripts/build.sh                     # Paper 26.3，默认目标
PAPER_API_VERSION=1.21.11 bash scripts/build.sh
```

脚本把 JAR 写入 `dist/`。选择与服务器对应的产物，不要混用不同 Paper 目标。项目依赖和字节码目标见 [pom.xml](../pom.xml)。仅运行测试可用 `mvn test`。

## Innova 部署

停服后备份旧 HammerCore，并在替换 HammerCore JAR 时移出 `InnovaGlyphCompat.jar` 和 `InnovaNativeCompat.jar`。不要同时运行独立版和内置版。保留 Nexo 及现有的 Nexo 字形和资源包配置。

`/innovacatalog [关键词]` 直接显示有可查看配方的一级分类。每个分类对应一个 Nexo 物品定义文件，点击物品可预览 Nexo 实际注册的配方，并返回原先的物品列表。烧制类配方会显示原料、设备、成品和烧制时间。菜单使用 Nexo 资源包的 `innova_catalog_recipe` 和 `innova_catalog_cooking` 字形，未安装这两个字形时回退为 Nexo 的配方预览背景。HammerCore 保留带旧所有者标记的家具保护，仅桥接 Innova 布局中 Nexo 自带的字形和位移。旧皮肤补恢复、特殊挖掘工具和挖石头掉化石不再由兼容代码提供。

在 Innova 的现有 PacketEvents 和 Nexo 启用时，HammerCore 会在发送给 Java 客户端前为菜单、聊天、ActionBar 和顶部 BossBar 标题中的 `U+EB20–U+EB33` 显式指定 `nexo:shift`，并为 HammerPKAddons 蓝量格指定 `nexo:default`。这不需要改写 CMI、DeluxeTags 或 HammerPKAddons 的 JAR，不改变资源包码位或宽度。物品文案只处理 Tags 和个人信息菜单的上半栏，不修改服务端物品或已签名的聊天正文。
