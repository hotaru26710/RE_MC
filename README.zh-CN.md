# RE_MC

[English](README.md) | [简体中文](README.zh-CN.md) | [日本語](README.ja.md)

RE_MC 是一个 Minecraft Forge 1.20.1 模组，加入了 **486模式（Subaru Mode）** 与死亡回归机制，灵感来自《Re:从零开始的异世界生活》。
486模式在创建世界时选择，是独立模式，不改动原版极限模式，并且可以单独选择是否允许作弊。

当玩家在极限模式中死亡时，世界会恢复到最近一次有效存档点，玩家回到记录状态，触发死亡的玩家会看到回归过渡效果。

## 环境要求

- Minecraft 1.20.1
- Forge 47.x
- Java 17
- 已启用极限模式世界

## 功能

- 486模式启用死亡回归，不改动原版极限模式。
- 486模式在创建世界时选择，并可独立选择是否允许作弊。
- 世界启动后创建初始存档点。
- 每 15 分钟尝试自动创建存档点。
- 支持权限等级控制的手动存档点命令。
- 候选存档点会经过安全确认后转正。
- 当前版本实现完整世界快照与回滚流程。
- 按玩家记录死亡日志和回归次数。
- 回归成功后随机播放死亡回归音效。
- 客户端包含黑幕与紫色魔女余香过渡效果。
- 生命值条上方新增十段式精神值 HUD 与压力动画。
- 死亡回归会根据死法扣除 5%-30% 精神值。
- 精神值仅在线时每分钟恢复 1%，并在低压时提供分档负面效果。
- 精神值越低，画面色彩越暗淡；最低时接近黑白，但不会完全失去颜色。
- 精神值较低时，附近随机方块的暴露面会显示错乱贴图；部分错乱会一直保留，直到玩家尝试与那个方块交互。
- 精神值极低时会出现仅客户端可见的幻觉僵尸向玩家走来，但不会造成伤害。
- 新增精神草、魔女茶、福音书、不可视之手物品。
- 不可视之手会拉拽并攻击单个敌对目标，消耗精神值并增加魔女的余香。
- 新增“魔女的余香”长期数值，会提高幻觉和错乱的出现频率。
- 回归后紫色余香效果会继续围绕玩家 5 分钟。

## 命令

```text
/remc return status
/remc return log [player]
/remc return checkpoint create
/remc return checkpoint force
/remc return recover
```

权限：

- `status`、`log`：玩家可用；查看其他玩家需要权限等级 2。
- `checkpoint create`：权限等级 2。
- `checkpoint force`、`recover`：权限等级 4。

## 配置

服务端配置会写入 `serverconfig/re_mc-server.toml`。

主要配置项：

- `enabled`
- `autoIntervalMinutes`
- `safeHealthPercent`
- `damageFreeSeconds`
- `candidateGraceSeconds`
- `captureTimeoutSeconds`
- `allowBossAndRaid`
- `showTransition`
- `spiritEnabled`
- `spiritRecoveryPerMinute`
- `spiritHudEnabled`

## 构建

```powershell
.\gradlew.bat build --no-daemon
```

输出：

```text
build/libs/re_mc-1.1.0.jar
```

## 开发说明

- 当前死亡回归快照系统采用完整目录快照。
- 原地重建世界使用 Forge Access Transformer 和 Minecraft 服务端生命周期内部接口。
- 建议先使用专用极限模式测试世界验证，再用于重要长期存档。
- 请始终备份重要世界。

## 第三方音频说明

`src/main/resources/assets/re_mc/sounds/deathreturn/` 下的音频为用户提供的第三方素材，**不包含在本项目的 MIT 许可证范围内**。公开或商业再分发前，请替换音频或取得相应授权。

## 许可证

代码使用 MIT License，详情见 `LICENSE`。
