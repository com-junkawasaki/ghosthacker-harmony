# Changelog

pure `.cljc` Ghost Battle core（`ghosthacker-harmony.battle`）と、それを使う
プロトタイプ実装の変更履歴（ADR-2607023200 / ADR-2607032600）。

## Unreleased

- ブラウザhostアダプタ追加（ADR-2607100900 follow-up (b)、ghosthacker-flowと
  同じ設計）: `web.cljs`（reagent、Web Audioでビートクロック+合成
  メトロノーム音、`:groove`は視覚的TENSE⇄Sky Highクロスフェード、
  ASYMMETRYラッチ時は暗い赤に固定）+ `shadow-cljs.edn`/`package.json`/
  `public/index.html`。headless DOM上で実keydown/click操作による通し
  （START→カウントダウン→12拍judge→ASYMMETRY/HARMONY判定表示→もう一度で
  初期状態に復帰）を手動検証済み。
- 初期実装: `battle.cljc`（ASYMMETRY latch / battle-outcome / battle-run /
  battle-play-run、`ghosthacker.groove.core`をラップ）、
  `terminal.clj`（プレイ可能なターミナルプロトタイプ、ASYMMETRY発生を
  画面表示）。9 tests / 29 assertions。実プロセスとして手動検証済み
  （timeout 15s下でASYMMETRY敗北まで正しく到達、ハング無し）。
