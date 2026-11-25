# HammerCore

HammerCore 为 Paper 服务器提供世界保护、玩家便捷功能和插件联动。服主可以限制重复袭击、恢复经过 AntiVillagerLag 优化的村民、自动化 BreweryX 酿造，也可以为 ProjectKorra 的火宗和水宗调节季节温度。

## 安装与配置

安装 HammerCore、ProtocolLib、PlaceholderAPI 和 Floodgate。需要酿造自动化时加装 BreweryX，需要元素温度联动时加装 ProjectKorra 和 RealisticSeasons。自行打包请参照[构建说明](docs/building.md)。

在服务端的插件目录修改[配置](src/main/resources/config.yml)和[语言文本](src/main/resources/messages.yml)，用 `/hammercore reload` 应用。用 `/ci status` 或 `/elementtemp status` 分别查看联动是否生效。

## 从独立插件切换

若要用 HammerCore 取代 CauldronInteract 或 ElementTemperature，先停服，移除对应的独立 JAR，再迁移自定义配置。把 CauldronInteract 的 `features`、`behaviour` 和 `effects` 放到 `cauldron-interact` 下，或把 ElementTemperature 的设置放到 `element-temperature` 下。独立插件仍在运行时，HammerCore 不会重复启用相应功能。

展示框保护由 [CodFrame](https://modrinth.com/plugin/codframe) 提供，它可以接管旧的 HammerCore 展示框锁。
