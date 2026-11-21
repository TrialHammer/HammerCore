# HammerCore

HammerCore 是 TrialHammer 服务器的核心插件，整合头顶信息栏、世界保护、快捷操作、
矿车加速、酿造扩展、ProjectKorra 联动与 PlaceholderAPI 变量。

## 环境

- Paper 26.3 或 Paper 1.21.11（兼容整个 Paper 1.21.x 系列）
- Java 21+
- 运行依赖：ProtocolLib、PlaceholderAPI、floodgate
- 可选集成：ProjectKorra 1.12、BreweryX 3.7.1、CoreProtect

## 构建

```bash
bash scripts/build.sh                     # Paper 26.3，默认
PAPER_API_VERSION=1.21.11 bash scripts/build.sh
```

产物写入 `dist/HammerCore-1.4.0-<Paper版本>.jar`。1.21.x 产物声明
`api-version: 1.21`，可通用于 1.21.1 至 1.21.11；26.3 产物声明
`api-version: 26.3`。直接执行 `mvn clean package` 仍会构建默认的 Paper 26.3 版本。

`libs/` 中的本地 jar 不纳入版本控制，编译前需自行放入：

| 文件 | 来源 |
| --- | --- |
| `BreweryX-3.7.1.jar` | BreweryX 3.7.1 官方发布（Modrinth） |
| `ProjectKorra-1.12.0.jar` | ProjectKorra v1.12.0 GitHub Release |
| `CoreProtect-24.1.jar` | CoreProtect 24.1 官方发布 |

这些 API 没有公开的 Maven 坐标，因此以 `system` scope 引用本地文件。

## 配置

`src/main/resources/config.yml` 和 `messages.yml` 分别为功能配置和语言文件：

- `info-bar.*` / `action-bar.*`：信息栏显示、刷新周期与内容。
- `world-protection.*`：爆炸、实体破坏、耕地保护、保留物品、传送门边界。
- `util.*`：去皮恢复、自动重生、快捷菜单、主城掉落清理、纪念品传送。
- `anti-raid-farm.*`：每位玩家的袭击冷却，防止反复触发袭击农场（默认开启，`cooldown-seconds: 180`）。
- `villager-restore.*`：`/hammercore restorevillagers [radius]` 把被 AntiVillagerLag 优化过的村民
  恢复为原始状态（清除插件标记、恢复 AI 与名称；默认半径 50，玩家默认拥有权限）。
- `brewery.*`：孟婆汤/红酒的特殊效果。`recipes.meng-po` / `recipes.wine` 填 BreweryX
  `recipes.yml` 的配方 ID，`quality-names.*` 填显示名称；两者任一命中即可，比较时忽略颜色代码与大小写。
- `projectkorra.*`、`minecart.*`、`integrations.*`：各联动功能。
- `messages.yml`：所有命令、提示、警告和广播文本。

> 展示框保护已迁移到 [CodFrame](https://modrinth.com/plugin/codframe)，由 CodFrame 自动导入
> HammerCore 旧版的 `hammercore:owneruuid` / `ownerusername` 数据；HammerCore 的
> `item-frame-lock` 配置项会在升级时自动移除。

`/hammercore reload` 可重载 YAML 并重新注册监听器。

## 版本与迁移

旧 Gradle 构建已替换为 Maven。配置路径已从旧版扁平键整理为分组结构；
旧服务器如保留旧配置，请按新版 `config.yml` 的键名迁移，尤其是
`info-bar.content`、`world-protection.*`、`util.*` 和各集成段。
