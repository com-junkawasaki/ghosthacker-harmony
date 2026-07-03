# Changelog

pure `.cljc` Ghost Battle core（`ghosthacker-harmony.battle`）と、それを使う
プロトタイプ実装の変更履歴（ADR-2607023200 / ADR-2607032600）。

## Unreleased

- 初期実装: `battle.cljc`（ASYMMETRY latch / battle-outcome / battle-run /
  battle-play-run、`ghosthacker.groove.core`をラップ）、
  `terminal.clj`（プレイ可能なターミナルプロトタイプ、ASYMMETRY発生を
  画面表示）。9 tests / 29 assertions。実プロセスとして手動検証済み
  （timeout 15s下でASYMMETRY敗北まで正しく到達、ハング無し）。
