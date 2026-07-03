(ns ghosthacker-harmony.battle-test
  (:require [clojure.test :refer [deftest is testing]]
            [ghosthacker.groove.core :as groove]
            [ghosthacker-harmony.battle :as battle]))

(deftest battle-outcome-test
  (testing "初期状態(groove 0.0、asymmetry未経験)はASYMMETRY扱い"
    (is (= :asymmetry (battle/battle-outcome battle/initial-battle-state))))
  (testing "grooveが閾値以上ならHARMONY(asymmetryを経験していない場合)"
    (is (= :harmony (battle/battle-outcome (assoc battle/initial-battle-state
                                                  :groove battle/victory-groove-threshold)))))
  (testing "grooveが閾値未満ならASYMMETRY"
    (is (= :asymmetry (battle/battle-outcome (assoc battle/initial-battle-state
                                                    :groove (- battle/victory-groove-threshold 0.01))))))
  (testing "asymmetryを一度でも経験していれば、その後grooveが持ち直しても無条件でASYMMETRY"
    (is (= :asymmetry (battle/battle-outcome (assoc battle/initial-battle-state
                                                    :groove 1.0 :asymmetry? true))))))

(deftest judge-chart-input-asymmetry-latch-boundary-test
  (testing "perfectの連打ではasymmetry?はfalseのまま"
    ;; grooveは+0.08/perfect。victory-groove-threshold(0.6)に届くには
    ;; 8拍以上必要(8 * 0.08 = 0.64 >= 0.6)。
    (let [chart (groove/chart-beats 0 [{:bpm 124 :beat-count 10}])
          state (battle/judge-chart-sequence battle/initial-battle-state chart chart)]
      (is (false? (battle/asymmetry? state)))
      (is (= :harmony (battle/battle-outcome state)))))
  (testing "missでgrooveがちょうど0.0まで落ちるとasymmetry?がtrueにラッチされる"
    (let [chart (groove/chart-beats 0 [{:bpm 124 :beat-count 1}])
          ;; 1発目は大きく外してmiss、groove: 0.0 + (-0.15) -> clamp -> 0.0
          state (battle/judge-chart-input battle/initial-battle-state chart 100000.0)]
      (is (= :miss (last (:judgments state))))
      (is (zero? (:groove state)))
      (is (true? (battle/asymmetry? state)))
      (is (= :asymmetry (battle/battle-outcome state)))))
  (testing "一度asymmetryになった後、その後perfectでgrooveが持ち直しても
            asymmetry?はtrueのまま(ラッチは戻らない)"
    (let [chart (groove/chart-beats 0 [{:bpm 124 :beat-count 6}])
          collapsed (battle/judge-chart-input battle/initial-battle-state chart 100000.0)
          recovered (reduce (fn [s t] (battle/judge-chart-input s chart t))
                            collapsed
                            (rest chart))]
      (is (true? (battle/asymmetry? collapsed)))
      (is (> (:groove recovered) 0.0) "grooveは実際に上がっている")
      (is (true? (battle/asymmetry? recovered)) "しかしasymmetry?は戻らない")
      (is (= :asymmetry (battle/battle-outcome recovered))))))

(deftest battle-run-and-battle-play-run-test
  (testing "全拍ジャストで戦えばHARMONY(勝利)"
    (let [chart (groove/chart-beats 0 [{:bpm 124 :beat-count 20}])
          result (battle/battle-play-run chart chart)]
      (is (= :harmony (:outcome result)))
      (is (== 1.0 (:accuracy result)))))
  (testing "空振り(何も入力しない)はbattle-runでもmiss計上され、
            groove 0.0でASYMMETRY(敗北)"
    (let [chart (groove/chart-beats 0 [{:bpm 124 :beat-count 4}])
          result (battle/battle-play-run chart [])]
      (is (= :asymmetry (:outcome result)))
      (is (== 0.0 (:accuracy result))))))
