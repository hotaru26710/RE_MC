# RE_MC

[English](README.md) | [简体中文](README.zh-CN.md) | [日本語](README.ja.md)

RE_MC 是一个 Minecraft Forge 1.20.1 模组，加入了 **486模式（Subaru Mode）** 与死亡回归机制，灵感来自《Re:从零开始的异世界生活》。
486模式在创建世界时选择，是独立模式，不改动原版极限模式，并且可以单独选择是否允许作弊。

当玩家在 486模式中死亡时，世界会恢复到最近一次有效存档点，玩家回到记录状态，触发死亡的玩家会看到回归过渡效果。

## 环境要求

- Minecraft 1.20.1
- Forge 47.x
- Java 17
- 使用 486模式创建的世界

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
- 精神值较低时，附近随机方块的暴露面会短暂显示错乱贴图；错乱会自然消退，不再需要交互才恢复。
- 新增精神草、魔女茶、福音书；不可视之手改为非物品能力。
- 不可视之手默认绑定 V 键。主动触发时锁定 20 格内全部敌怪，从玩家自身向每只敌怪伸出手，无视障碍，以击退 IV 的力度持续捶打并造成伤害，消耗 10% 精神值；每只手持续到对应敌怪死亡或 10 秒结束。
- 不可视之手被动触发：生命低于 3 颗心且 20 格内至少有 4 个敌怪时，消耗 20% 精神值并自动发动。
- 主动与被动共用 5 分钟冷却；死亡回归或睡觉会重置冷却。
- 魔女的余香会随时间下降，不再因不可视之手增加。
- 魔女的余香高于 30% 时会增加附近刷怪；在聊天框说出“死亡回归”“死归”“重生”等词会触发分阶段魔女诅咒，约 2 秒内丝滑清空生命值。
- 睡觉会恢复精神值。回归后紫色余香效果会继续围绕玩家 5 分钟。

## 命令

```text
/remc return status
/remc return log [player]
/remc return checkpoint create
/remc return checkpoint force
/remc return recover
/remc unseenhand debug [player]
/remc spirit full [player]
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
- `witchScentDecayPerMinute`
- `sleepSpiritRestore`

## 构建

```powershell
.\gradlew.bat build --no-daemon
```

输出：

```text
build/libs/re_mc-1.2.0.jar
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
