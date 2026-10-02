# RE_MC

[English](README.md) | [简体中文](README.zh-CN.md) | [日本語](README.ja.md)

RE_MC は、『Re:Zero から始める異世界生活』にインスパイアされた **スバルモード** と死亡回帰機能を追加する Minecraft Forge 1.20.1 用 MOD です。
スバルモードはワールド作成時に選択する独立モードで、バニラのハードコア設定は変更しません。チート許可は個別に選択できます。

ハードコアモードでプレイヤーが死亡すると、世界は最新の有効なチェックポイントへ復元され、プレイヤーは記録された状態へ戻り、死亡したプレイヤーには復帰用の演出が表示されます。

## 必要環境

- Minecraft 1.20.1
- Forge 47.x
- Java 17
- ハードコアワールドが有効

## 機能

- スバルモードで死亡回帰を有効化し、バニラのハードコア設定は変更しません。
- ワールド作成時にスバルモードを選択し、チート許可を個別に設定できます。
- ワールド起動後に初期チェックポイントを作成。
- 15 分ごとに自動チェックポイント作成を試行。
- 権限レベル付きの手動チェックポイントコマンド。
- 候補チェックポイントは猶予時間を経て昇格。
- 現行版は完全なワールドスナップショットとロールバック処理を実装。
- プレイヤーごとの死亡ログと回帰回数を保存。
- 回帰成功後に死亡回帰用サウンドをランダム再生。
- クライアント側に暗転と紫色の魔女の残り香エフェクトを追加。
- 体力バーの上に 10 分割の精神値 HUD と圧力アニメーションを表示。
- 死亡回帰時に死因に応じて精神値を 5%～30% 減少。
- 精神値はオンライン中のみ毎分 1% 回復し、低値では段階的なデバフを付与。
- 精神値が低いほど画面の彩度と明るさが下がり、最低でも完全な白黒にはなりません。
- 精神値が低いと、周囲の露出したブロック面が別のテクスチャへ乱れることがあり、一部はそのブロックを操作しようとするまで残ります。
- 精神値が極端に低いと、ダメージを与えないクライアント専用の幻覚ゾンビが近づいてくることがあります。
- 精神草、魔女茶、福音書、不可視の手のアイテムを追加。
- 不可視の手は敵対対象を 1 体引き寄せて攻撃し、精神値を消費して魔女の残り香を増やします。
- 幻覚の発生率を高める長期ステータス「魔女の残り香」を追加。
- 回帰後、紫色の残り香エフェクトがプレイヤー周辺に 5 分間残る。

## コマンド

```text
/remc return status
/remc return log [player]
/remc return checkpoint create
/remc return checkpoint force
/remc return recover
```

権限:

- `status`、`log`: プレイヤーが使用可能。他プレイヤーの参照には権限レベル 2 が必要。
- `checkpoint create`: 権限レベル 2。
- `checkpoint force`、`recover`: 権限レベル 4。

## 設定

サーバー設定は `serverconfig/re_mc-server.toml` に保存されます。

主な設定項目:

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

## ビルド

```powershell
.\gradlew.bat build --no-daemon
```

出力:

```text
build/libs/re_mc-1.1.0.jar
```

## 開発メモ

- 現在の死亡回帰スナップショット方式は完全ディレクトリコピーです。
- その場でのワールド再構築には Forge Access Transformer と Minecraft サーバー内部ライフサイクルを使用しています。
- 長期運用するワールドで使う前に、専用のハードコアテストワールドで検証してください。
- 重要なワールドは必ずバックアップしてください。

## 第三者音声について

`src/main/resources/assets/re_mc/sounds/deathreturn/` 以下の音声ファイルはユーザー提供の第三者素材であり、**本プロジェクトの MIT License には含まれません**。公開配布または商用配布の前に、音声を差し替えるか適切なライセンスを取得してください。

## ライセンス

コードは MIT License の下で公開されています。詳細は `LICENSE` を参照してください。
