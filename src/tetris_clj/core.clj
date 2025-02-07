(ns tetris-clj.core
  (:require [tetris-clj.shapes :as s]
            [cljfx.api :as fx]
            [tetris-clj.field :as field]
            [tetris-clj.game :as g]
            [tetris-clj.execution :as exec]
            [clojure.core.async :as async])
  (:import [javafx.scene.canvas Canvas GraphicsContext]
           (javafx.scene.input KeyCode KeyEvent)
           [javafx.scene.paint Color]))

(def *state
  (atom {:game-over false}))

(defn running? []
  (:running @*state))

(defn append-action [action]
  (swap! *state update-in [:actions] (fn [actions] (conj actions action))))


(defn process-actions []
  (let [actions (:actions @*state)]
    (let [new-state (reduce g/update-state @*state actions)
          new-field (field/clear-full-rows (:field new-state))]
      (reset! *state (assoc new-state :actions #{} :field new-field)))))


(defn start-game []
  (println "Start the game")
  (reset! *state (g/init))
  (exec/start-task #(append-action :down) 200 running?)
  (exec/start-task process-actions 100 running?))


(defn stop-game []
  (println "Stop the game")
  (swap! *state assoc :running false :game-over true))

(defn current-shape [state]
  (s/get-coords (:shape state)))

(defn next-shape [state]
  (map (fn [[x y]] [x (inc y)]) (s/get-coords (:next-shape state))))

(defn code->action [e]
  (-> e
      str
      .toLowerCase
      keyword))

(defn event-handler [event]
  (case (:event/type event)
    ::bot-arrow
    (let [^KeyEvent e (:fx/event event)
          ^KeyCode code (.getCode e)
          action (code->action code)]
      (append-action (if (= action :shift) :rotate action)))))


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
  {:fx/type :canvas
   :width   (* g/v-spacing 4)
   :height  (* g/h-spacing 4)
   :draw    (fn [^Canvas canvas]
              (when (:next-shape state)
                (let [ctx (.getGraphicsContext2D canvas)]
                  (doto ctx
                    (.clearRect 0 0 (.getWidth canvas) (.getHeight canvas))
                    (.setFill Color/RED))
                  (draw-shape ctx (next-shape state) g/v-spacing g/h-spacing Color/RED))))})

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

                (when (:shape state) (draw-shape ctx (current-shape state) g/v-spacing g/h-spacing Color/RED))
                (draw-field ctx (:field state) g/v-spacing g/h-spacing Color/BLUE)))})

(def renderer
  (fx/create-renderer
    :middleware
    (fx/wrap-map-desc
      (fn [state]
        {:fx/type :stage
         :showing true
         :scene   {:fx/type        :scene
                   :on-key-pressed {:event/type ::bot-arrow}
                   :root           {:fx/type  :v-box
                                    :padding  20
                                    :spacing  20
                                    :children [{:fx/type next-shape-view
                                                :state   state}
                                               {:fx/type canvas-grid
                                                :v-count g/v-count
                                                :h-count g/h-count
                                                :state   state}
                                               {:fx/type  :h-box
                                                :spacing  10
                                                :children [{:fx/type           :button
                                                            :text              "Start"
                                                            :on-action         (fn [_] (start-game))
                                                            :focus-traversable false}
                                                           {:fx/type           :button
                                                            :text              "Stop"
                                                            :on-action         (fn [_] (stop-game))
                                                            :focus-traversable false}
                                                           {:fx/type   :label
                                                            :text      "Game over!"
                                                            :text-fill Color/RED
                                                            :visible   (:game-over state)}]}
                                               ]}}}))
    :opts {:fx.opt/map-event-handler event-handler}))

(fx/mount-renderer *state renderer)

(renderer)
