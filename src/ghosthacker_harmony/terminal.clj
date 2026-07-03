(ns ghosthacker-harmony.terminal
  "GHOST HACKER: HARMONY — minimal terminal host adapter (playable prototype).

  Same shape as com-junkawasaki/ghosthacker-flow's terminal.clj (a background
  `future` ticks to each beat's wall-clock time while the main thread judges
  `read-line` timing against the real elapsed time), but drives
  `ghosthacker-harmony.battle` instead of the plain groove core, so a run
  ends in an explicit ASYMMETRY/HARMONY verdict — Ghost Battle proper.

  Run: clojure -M -m ghosthacker-harmony.terminal [beat-count]"
  (:require [ghosthacker.groove.core :as groove]
            [ghosthacker-harmony.battle :as battle]))

(defn- print-tick! []
  (print "♪ ")
  (flush))

(defn- run-ticker!
  "chart(絶対ms時刻の列)どおりにtickを印字するfutureを起動する。
   呼び出し側はゲーム終了時にfuture-cancelで止めること。"
  [chart]
  (future
    (doseq [t chart]
      (let [wait (- t (System/currentTimeMillis))]
        (when (pos? wait)
          (Thread/sleep wait)))
      (print-tick!))))

(defn- countdown!
  "「3, 2, 1, GO!」を1拍分の間隔で表示する。"
  [bpm]
  (let [interval-ms (long (groove/beat-interval-ms bpm))]
    (doseq [n [3 2 1]]
      (println n)
      (Thread/sleep interval-ms))
    (println "GO!")))

(defn- read-beats!
  "chart(拍の絶対時刻ms列)ぶんread-lineで入力を待ち、
   battle/judge-chart-inputで都度判定して進行状況(ASYMMETRY latch含む)を
   印字する。標準入力がEOF(nil)になったら、そこまでのbattle-stateで
   打ち切る。"
  [chart]
  (loop [state battle/initial-battle-state i 0]
    (if (>= i (count chart))
      state
      (let [line (read-line)]
        (if (nil? line)
          state
          (let [now (System/currentTimeMillis)
                next-state (battle/judge-chart-input state chart now)
                judgment (last (:judgments next-state))]
            (println (format " -> %s (combo %d)%s"
                              (name judgment)
                              (:combo next-state)
                              (if (and (battle/asymmetry? next-state)
                                       (not (battle/asymmetry? state)))
                                " — ASYMMETRY..." "")))
            (recur next-state (inc i))))))))

(defn- default-chart
  "既定の曲構成: 前半(既定bpm)→後半(1.25倍速)へ加速する2セクション。"
  [start-time-ms beat-count]
  (let [half (quot beat-count 2)
        rest-count (- beat-count half)]
    (groove/chart-beats start-time-ms
                        [{:bpm groove/default-bpm :beat-count half}
                         {:bpm (long (* 1.25 groove/default-bpm)) :beat-count rest-count}])))

(defn -main
  "Entry point for `clojure -M -m ghosthacker-harmony.terminal [beat-count]`.
  See the ns docstring."
  [& args]
  (let [beat-count (if-let [a (first args)] (Integer/parseInt a) 12)]
    (println (format "GHOST HACKER: HARMONY — Ghost Battle (%d beats)" beat-count))
    (println "Enterキーで各拍を叩いてください。準備ができたらEnterで開始:")
    (read-line)
    (countdown! groove/default-bpm)
    (let [start-time-ms (System/currentTimeMillis)
          chart (default-chart start-time-ms beat-count)
          ticker (run-ticker! chart)
          state (read-beats! chart)]
      (future-cancel ticker)
      (println)
      (println "=== BATTLE RESULT ===")
      (let [result (battle/battle-summary state)]
        (println (format "outcome=%s score=%d max-combo=%d accuracy=%.2f groove=%.2f"
                          (name (:outcome result))
                          (:score result)
                          (:max-combo result)
                          (double (:accuracy result))
                          (double (:groove result))))
        (println (case (:outcome result)
                   :harmony "HARMONY — 心が通じた。"
                   :asymmetry "ASYMMETRY — 同期が崩れた。")))
      ;; futureはclojure.lang.Agentの非daemonスレッドプールを使うため、
      ;; これを呼ばないとロジック完了後もJVMプロセスが終了せずハングする。
      (shutdown-agents))))
