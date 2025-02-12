(ns cljfx-stateless.core
  (:require [common.shapes :as s]
            [cljfx.api :as fx]
            [common.field :as field]
            [common.game :as g]
            [clojure.core.async :as async])
  (:import [javafx.scene.canvas Canvas GraphicsContext]
           (javafx.scene.input KeyCode KeyEvent)
           [javafx.scene.paint Color]))


(defn game-step [state action]
  (if (= :stop action)
    (assoc state :running false :game-over true)
    (let [new-state (g/update-state state action)
          new-field (field/clear-full-rows (:field new-state))]
      (assoc new-state :field new-field))))


(defn stop-game [state]
  (println "Stop the game")
  (async/go
    (async/>! (:events state) :stop)))



(defn code->action [e]
  (-> e
      str
      .toLowerCase
      keyword))

(defn handle-event [state event]
  (let [^KeyCode code (.getCode event)
        action (code->action code)
        processed (if (= action :shift) :rotate action)]
    (async/go
      (async/>! (:events state) processed))))


(defn draw-shape
  [^GraphicsContext ctx coords cell-width cell-height color]
  (.setFill ctx color)
  (doseq [[sx sy] coords]
    (let [rect-x (* sx cell-width)
          rect-y (* sy cell-height)]
      (.fillRect ctx rect-x rect-y cell-width cell-height))))

(defn draw-field
  [^GraphicsContext ctx field cell-width cell-height color]
  (.setFill ctx color)
  (doseq [y (range (count field))]
    (doseq [x (range (count (field y)))]
      (when (= 1 ((field y) x))
        (let [rect-x (* x cell-width)
              rect-y (* y cell-height)]
          (.fillRect ctx rect-x rect-y cell-width cell-height))))))

(defn next-shape-view [{:keys [state]}]
  {:fx/type  :v-box
   :padding  20
   :spacing  10
   :children [{:fx/type :label
               :text    "Next figure:"}
              {:fx/type :canvas
               :width   (* g/v-spacing 4)
               :height  (* g/h-spacing 4)
               :draw    (fn [^Canvas canvas]
                          (when (:next-shape state)
                            (let [ctx (.getGraphicsContext2D canvas)]
                              (doto ctx
                                (.clearRect 0 0 (.getWidth canvas) (.getHeight canvas))
                                (.setFill Color/RED))
                              (draw-shape ctx (g/next-shape state) g/v-spacing g/h-spacing Color/RED))))}]})

(defn canvas-grid [{:keys [v-count h-count state]}]
  {:fx/type :canvas
   :width   g/width
   :height  g/height
   :draw    (fn [^Canvas canvas]
              (let [ctx (.getGraphicsContext2D canvas)]
                (doto ctx
                  (.clearRect 0 0 g/width g/height)
                  (.setStroke Color/LIGHTGRAY))
                (doseq [i (range (+ 2 v-count))] (let [x (* i g/v-spacing)] (.strokeLine ctx x 0 x g/height)))
                (doseq [i (range (+ 2 h-count))] (let [y (* i g/h-spacing)] (.strokeLine ctx 0 y g/width y)))

                (when (:shape state) (draw-shape ctx (g/current-shape state) g/v-spacing g/h-spacing Color/RED))
                (draw-field ctx (:field state) g/v-spacing g/h-spacing Color/BLUE)))})

(defn start [render]
  (println "Start the game")

  (let [events (async/chan 1)]

    (async/go-loop []
      (if (async/>! events :down)
        (do
          (async/<! (async/timeout g/game-speed))
          (recur))))

    (async/go-loop [state (assoc (g/init) :events events)]
      (if (:running state)
        (when-let [action (async/<! events)]
          (let [new-state (game-step state action)]
            (render new-state)
            (recur new-state)))
        (async/close! events)))

    ))


(def renderer
  (fx/create-renderer))

(defn root [{:keys [state]}]
  {:fx/type :stage
   :showing true
   :scene   {:fx/type        :scene
             :on-key-pressed #(handle-event state %)
             :root           {:fx/type  :h-box
                              :children [{:fx/type  :v-box
                                          :padding  20
                                          :spacing  20
                                          :children [{:fx/type canvas-grid
                                                      :v-count g/v-count
                                                      :h-count g/h-count
                                                      :state   state}
                                                     {:fx/type  :h-box
                                                      :spacing  10
                                                      :children [{:fx/type           :button
                                                                  :text              "Start"
                                                                  :on-action         (fn [_] (start #(renderer {:fx/type root
                                                                                                                :state   %})))
                                                                  :focus-traversable false}
                                                                 {:fx/type           :button
                                                                  :text              "Stop"
                                                                  :on-action         (fn [_] (stop-game state))
                                                                  :focus-traversable false}
                                                                 {:fx/type   :label
                                                                  :text      "Game over!"
                                                                  :text-fill Color/RED
                                                                  :visible   (:game-over state)}]}
                                                     ]}
                                         {:fx/type next-shape-view
                                          :state   state}]}}})




;; Run in REPL
(comment
  (renderer {:fx/type root
             :state   {:game-over false}}))


(defn -main [& args]
  (renderer {:fx/type root
             :state   {:game-over false}}))
