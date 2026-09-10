(ns ghosthacker-harmony.web
  "GHOST HACKER: HARMONY -- browser host adapter (ADR-2607100900 follow-up
  (b)). Same shape as ghosthacker-flow's web.cljs (ClojureScript, since
  real-time beat timing + audio are host-imports neither kotoba wasm nor
  clojurewasm can provide yet, ADR-2607100030 addendum 2), driving
  ghosthacker-harmony.battle instead of the plain groove core so a run
  ends in an explicit ASYMMETRY/HARMONY verdict -- Ghost Battle proper.

  No composed two-layer music exists for :groove to crossfade between,
  so Web Audio drives both the beat clock (AudioContext.currentTime) and
  a synthesized metronome tick, while :groove drives a visual TENSE
  (cool)<->Sky High(warm) crossfade -- once :asymmetry? latches (a
  :miss that drops :groove to exactly 0.0), the background locks to a
  somber red regardless of :groove's later value, mirroring the pure
  core's one-way latch (a battle that broke stays broken even if groove
  recovers). Falls back to performance.now() with no audible tick when
  Web Audio is unavailable."
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [ghosthacker.groove.core :as core]
            [ghosthacker-harmony.battle :as battle]))

;; --- clock / audio ----------------------------------------------------

(defonce ^:private !ctx (atom nil))

(defn- ensure-ctx! []
  (when-not @!ctx
    (when-let [ctor (or (.-AudioContext js/window) (.-webkitAudioContext js/window))]
      (reset! !ctx (new ctor))))
  @!ctx)

(defn- now-ms []
  (if-let [ctx @!ctx]
    (* 1000 (.-currentTime ctx))
    (.now js/performance)))

(defn- schedule-tick! [t-ms freq]
  (when-let [ctx @!ctx]
    (let [t (/ t-ms 1000.0)
          osc (.createOscillator ctx)
          gain (.createGain ctx)]
      (set! (.-value (.-frequency osc)) freq)
      (.setValueAtTime (.-gain gain) 0.001 t)
      (.linearRampToValueAtTime (.-gain gain) 0.25 (+ t 0.005))
      (.exponentialRampToValueAtTime (.-gain gain) 0.001 (+ t 0.09))
      (.connect osc gain)
      (.connect gain (.-destination ctx))
      (.start osc t)
      (.stop osc (+ t 0.1)))))

;; --- chart --------------------------------------------------------------

(defn- default-chart
  "Same 2-section TENSE(default bpm) -> Sky High(1.25x) shape as
  terminal.clj's default-chart."
  [start-time-ms beat-count]
  (let [half (quot beat-count 2)
        rest-count (- beat-count half)]
    (core/chart-beats start-time-ms
                       [{:bpm core/default-bpm :beat-count half}
                        {:bpm (long (* 1.25 core/default-bpm)) :beat-count rest-count}])))

;; --- state ----------------------------------------------------------------

(defonce state
  (r/atom {:phase :idle          ; :idle | :countdown | :playing | :result
           :beat-count 12
           :chart nil
           :battle-state nil
           :beats-done 0
           :last-judgment nil
           :asymmetry-flash false
           :countdown-label "3"}))

(defn- hit! []
  (when (= (:phase @state) :playing)
    (let [t (now-ms)
          {:keys [chart battle-state beats-done]} @state
          was-asymmetry (battle/asymmetry? battle-state)
          next-bs (battle/judge-chart-input battle-state chart t)
          judgment (last (:judgments next-bs))
          done (inc beats-done)
          just-latched (and (not was-asymmetry) (battle/asymmetry? next-bs))]
      (swap! state assoc
             :battle-state next-bs
             :last-judgment judgment
             :beats-done done
             :asymmetry-flash just-latched
             :phase (if (>= done (count chart)) :result :playing)))))

(defn- start-game! []
  (ensure-ctx!)
  (let [beat-count (:beat-count @state)
        interval (core/beat-interval-ms core/default-bpm)
        go-time-ms (+ (now-ms) (* 3 interval))
        chart (default-chart go-time-ms beat-count)]
    (swap! state assoc
           :phase :countdown
           :chart chart
           :battle-state battle/initial-battle-state
           :beats-done 0
           :last-judgment nil
           :asymmetry-flash false
           :countdown-label "3")
    (schedule-tick! (- go-time-ms (* 3 interval)) 440)
    (schedule-tick! (- go-time-ms (* 2 interval)) 440)
    (schedule-tick! (- go-time-ms interval) 440)
    (doseq [t chart] (schedule-tick! t 880))
    (doseq [[i label] (map-indexed vector ["3" "2" "1"])]
      (js/setTimeout #(swap! state assoc :countdown-label label) (* i interval)))
    (js/setTimeout #(swap! state assoc :countdown-label "GO!" :phase :playing) (* 3 interval))))

(defn- restart! [] (swap! state assoc :phase :idle))

;; --- keyboard input ---------------------------------------------------

(defn- on-keydown [e]
  (when (= (.-code e) "Space")
    (.preventDefault e)
    (hit!)))

;; --- views ------------------------------------------------------------

(defn- groove-bg
  "Same visual crossfade as FLOW, except a latched ASYMMETRY locks the
  background to a somber red regardless of the current :groove value --
  the pure core's one-way latch, made visible."
  [g asymmetry?]
  (if asymmetry?
    {:background "linear-gradient(135deg, hsl(0,45%,14%), hsl(0,45%,20%))"}
    (let [hue (- 220 (* g 180))]
      {:background (str "linear-gradient(135deg, hsl(" hue ",70%,14%), hsl(" hue ",70%,24%))")})))

(defn- start-screen []
  [:div.harmony-app
   [:h1 "GHOST HACKER: HARMONY"]
   [:p.harmony-sub "Ghost Battle — 四つ打ちに同期し続けろ。Space で入力。"]
   [:button {:on-click start-game!} "START"]])

(defn- countdown-screen []
  [:div.harmony-app
   [:h1 "GHOST HACKER: HARMONY"]
   [:div.harmony-countdown (:countdown-label @state)]])

(defn- playing-screen []
  (let [{:keys [battle-state last-judgment beats-done chart asymmetry-flash]} @state
        asymmetry? (battle/asymmetry? battle-state)]
    [:div.harmony-app {:style (groove-bg (:groove battle-state) asymmetry?)}
     [:h1 "GHOST HACKER: HARMONY"]
     [:div.harmony-hud
      [:span (str "beat " beats-done "/" (count chart))]
      [:span (str "combo " (:combo battle-state))]
      [:span (str "groove " (.toFixed (:groove battle-state) 2))]]
     [:div.harmony-judgment (when last-judgment (name last-judgment))]
     (when asymmetry-flash [:div.harmony-asymmetry-flash "ASYMMETRY..."])
     [:p.harmony-hint "Space で入力"]]))

(defn- result-screen []
  (let [summary (battle/battle-summary (:battle-state @state))
        harmony? (= (:outcome summary) :harmony)]
    [:div.harmony-app
     [:h1 "GHOST HACKER: HARMONY"]
     [:h2 {:class (if harmony? "harmony-win" "harmony-lose")}
      (if harmony? "HARMONY — 心が通じた。" "ASYMMETRY — 同期が崩れた。")]
     [:p (str "score " (:score summary) " / max-combo " (:max-combo summary))]
     [:p (str "accuracy " (.toFixed (* 100 (:accuracy summary)) 0) "% / groove " (.toFixed (:groove summary) 2))]
     [:button {:on-click restart!} "もう一度"]]))

(defn app []
  (case (:phase @state)
    :countdown [countdown-screen]
    :playing [playing-screen]
    :result [result-screen]
    [start-screen]))

(defn ^:export mount []
  (when-let [el (.getElementById js/document "app")]
    (.addEventListener js/window "keydown" on-keydown)
    (rdom/render [app] el)))

(defn ^:export init [] (mount))
