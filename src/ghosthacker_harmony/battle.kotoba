(ns ghosthacker-harmony.battle
  "GHOST HACKER: HARMONY — Ghost Battle core (ADR-2607023200).

  Wraps ghosthacker.groove.core's judgment engine (score/combo/`:groove`
  crossfade, chart-based multi-section timing) with the win/lose framing
  that makes this the flagship: syncing to the beat isn't just for score,
  it's the battle itself. `:groove` collapsing to 0.0 mid-battle (via a
  `:miss`) is ASYMMETRY — a genuine defeat, not just a bad combo — and
  sustaining `:groove` through to the end of the chart is HARMONY: victory.
  No rendering, input, or audio I/O lives here, same split as
  ghosthacker-groove-core / ghosthacker-flow."
  (:require [ghosthacker.groove.core :as groove]))

(def victory-groove-threshold
  "battle-outcomeがHARMONY(勝利)と判定する、chart終了時点でのgroove下限。
   0.6 = groove-delta(+0.08 perfect/+0.03 good/-0.15 miss)からして、
   ミスをほぼ挟まずperfect中心で運ばないと届かない水準。"
  0.6)

(def initial-battle-state
  "groove/initial-stateに:asymmetry?(一度でも完全同期崩壊したか)を足したもの。"
  (assoc groove/initial-state :asymmetry? false))

(defn asymmetry?
  "battle-stateが(一度でも)ASYMMETRY(完全同期崩壊)を経験したか。"
  [battle-state]
  (boolean (:asymmetry? battle-state)))

(defn- note-asymmetry
  "judgmentの適用後、そのjudgmentが:missでgrooveが0.0まで落ちたなら
   :asymmetry?をtrueにラッチする（以降falseに戻らない — 一度崩壊した
   戦いは、途中でgrooveが持ち直しても『崩壊しなかったこと』にはならない）。"
  [prev-asymmetry? judgment next-groove]
  (or prev-asymmetry?
      (and (= judgment :miss) (zero? next-groove))))

(defn judge-chart-input
  "groove/judge-chart-inputのHARMONY版。battle-state
  （groove-coreの状態 + :asymmetry?）を受け取り、判定を適用したうえで
  ASYMMETRY判定を更新する。"
  [battle-state chart input-time-ms]
  (let [next-groove-state (groove/judge-chart-input battle-state chart input-time-ms)
        judgment (last (:judgments next-groove-state))]
    (assoc next-groove-state
           :asymmetry?
           (note-asymmetry (:asymmetry? battle-state) judgment (:groove next-groove-state)))))

(defn judge-chart-sequence
  "input-times（時系列順）をまとめてjudge-chart-inputで畳み込む。"
  [battle-state chart input-times]
  (reduce (fn [s t] (judge-chart-input s chart t)) battle-state input-times))

(defn battle-run
  "chart-runのHARMONY版: chart全体のうち空振りぶんも:missとして積み増し
   つつ、ASYMMETRY判定を通しで追跡する。"
  [chart input-times]
  (let [after-inputs (judge-chart-sequence initial-battle-state chart input-times)
        hit? (:hit-beat-indices after-inputs)
        missed-count (count (remove hit? (range (count chart))))]
    (reduce (fn [s _]
              (let [next (groove/apply-judgment s :miss)]
                (assoc next :asymmetry? (note-asymmetry (:asymmetry? s) :miss (:groove next)))))
            after-inputs
            (range missed-count))))

(defn battle-outcome
  "battle-state（バトル終了時点）から勝敗を返す。:asymmetry?が一度でも
   trueになっていれば無条件で:asymmetry。それ以外はgrooveが
   victory-groove-threshold以上ならHARMONY(勝利)、届かなければ
   ASYMMETRY(まだ心が通っていない、敗北扱い)。"
  [battle-state]
  (cond
    (asymmetry? battle-state) :asymmetry
    (>= (:groove battle-state) victory-groove-threshold) :harmony
    :else :asymmetry))

(defn battle-summary
  "groove/summaryにHARMONY固有の:outcome(:harmony/:asymmetry)を足したもの。
   ホストアダプタのリザルト画面にそのまま渡せる形。"
  [battle-state]
  (assoc (groove/summary battle-state) :outcome (battle-outcome battle-state)))

(defn battle-play-run
  "battle-runの結果をbattle-summaryにして返す。"
  [chart input-times]
  (battle-summary (battle-run chart input-times)))
