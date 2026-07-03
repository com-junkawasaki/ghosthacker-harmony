# GHOST HACKER: HARMONY

![test](https://github.com/com-junkawasaki/ghosthacker-harmony/actions/workflows/test.yml/badge.svg)

Ghost Hacker ゲームポートフォリオ第2弾（旗艦）。設計は
[ADR-2607023200](../../../90-docs/adr/2607023200-ghosthacker-game-portfolio-flow.md)
（超project `com-junkawasaki/root`）を参照。判定/score/combo/groove核は
[com-junkawasaki/ghosthacker-groove-core](https://github.com/com-junkawasaki/ghosthacker-groove-core)
（[ADR-2607032600](../../../90-docs/adr/2607032600-ghosthacker-groove-core-extraction.md)）
を共有し、このリポジトリは**Ghost Battle本編**というHARMONY固有の勝敗
フレーミングとホストアダプタだけを持つ。

## コンセプト

- **ジャンル**: 音ゲー（旗艦）
- **主人公**: Ren単独
- **コアループ**: Ghost Battle本編。四つ打ちに同期し続ける精度そのものが
  戦いの成否になる — ズレる（groove崩壊）= **ASYMMETRY**（敗北）、
  噛み合い続ける = **HARMONY**（勝利）。FLOWの`:groove`crossfade
  パラメータが、単なる演出ではなく勝敗の実体になっている

## 実装範囲

`src/ghosthacker_harmony/battle.cljc` — pure、host-free。
`ghosthacker.groove.core`をラップし、以下を追加する:

- **ASYMMETRY latch**（`asymmetry?`/`judge-chart-input`）— `:miss`判定で
  `:groove`がちょうど0.0まで落ちた瞬間にラッチされ、以降`:groove`が
  持ち直しても戻らない（一度崩壊した戦いは崩壊したまま）
- **battle-outcome** — `:asymmetry?`が一度でもtrueなら無条件でASYMMETRY。
  それ以外は終了時点の`:groove`が`victory-groove-threshold`（既定0.6）以上
  ならHARMONY（勝利）
- `battle-run`/`battle-play-run` — `chart-run`/`chart-play-run`のHARMONY版。
  空振り拍も明示的に`:miss`として積み増しつつASYMMETRY判定を通しで追跡する
- `battle-summary` — `groove/summary`に`:outcome`（`:harmony`/`:asymmetry`）
  を足したもの。ホストアダプタのリザルト画面にそのまま渡せる

**プレイ可能な最小プロトタイプ**として `src/ghosthacker_harmony/terminal.clj`
がある（ghosthacker-flowのterminal.cljと同じ構成: 新規依存ゼロ、
背景`future`が実時刻でtickを刻み、`read-line`で実際の経過時間を判定）。
ASYMMETRYにラッチした瞬間を画面に表示し、最後にHARMONY/ASYMMETRYの
勝敗を表示する。既定の曲構成は前半→後半で1.25倍速に加速する2セクション。

本格的なレンダリング/入力/音声ホストアダプタは依然として別レイヤーの課題。

## 開発

```bash
clojure -M:test
```

Lint（clj-kondo、Clojars経由でHomebrew等の別インストール不要）:

```bash
clojure -M:lint
```

遊んでみる:

```bash
clojure -M -m ghosthacker-harmony.terminal 16
```

変更履歴は [CHANGELOG.md](CHANGELOG.md)。
