(ns cljs-simple.layout
  (:require [common.game :as g]))


(defn markup [width height]
  [:div {:style "display: flex; justify-content: center; align-items: center; height: 100vh;"}
   [:div {:style "display: flex; padding: 20px;"}
    ;; Игровое поле
    [:canvas {:id "game-board"
              :width width
              :height height
              :style "border: 1px solid black;"}]
    ;; Боковая панель
    [:div {:style "display: flex; flex-direction: column; align-items: center; gap: 10px;"}
     [:canvas {:id "next-canvas"
               :width 150
               :height 150
               :style "padding: 20px;"}]
     [:button {:id "start-btn"} "Start"]
     [:button {:id "stop-btn"} "Stop"]
     [:div {:id "game-over"
            :style "color: red; font-size: 24px; margin-top: 10px; display: none;"}
      "Game over!"]]]])

(defn draw-field
  [ctx field cell-width cell-height color]
  (set! (.-fillStyle ctx) color)
  (doseq [y (range (count field))]
    (doseq [x (range (count (nth field y)))]
      (when (= 1 (get-in field [y x]))
        (let [rect-x (* x cell-width)
              rect-y (* y cell-height)]
          (.fillRect ctx rect-x rect-y cell-width cell-height))))))

(defn draw-shape
  [ctx coords cell-width cell-height color]
  (println coords)
  (set! (.-fillStyle ctx) color)
  (doseq [[x y] coords]
    (let [rect-x (* x cell-width)
          rect-y (* y cell-height)]
      (.fillRect ctx rect-x rect-y cell-width cell-height))))


(defn draw-next-shape
  [ctx state cell-width cell-height color]
  (.clearRect ctx 0 0 150 150)
  (when-let [next-fig (g/next-shape state)]
    (draw-shape ctx next-fig cell-width cell-height color)))

(defn draw-grid
  [ctx v-count h-count state width height v-spacing h-spacing]
  (println ctx)
  (.clearRect ctx 0 0 width height)
  (set! (.-strokeStyle ctx) "lightgray")
  (doseq [i (range (+ 2 v-count))]
    (let [x (* i v-spacing)]
      (.beginPath ctx)
      (.moveTo ctx x 0)
      (.lineTo ctx x height)
      (.stroke ctx)))
  (doseq [i (range (+ 2 h-count))]
    (let [y (* i h-spacing)]
      (.beginPath ctx)
      (.moveTo ctx 0 y)
      (.lineTo ctx width y)
      (.stroke ctx)))
  (draw-field ctx (:field state) v-spacing h-spacing "blue")
  (when (g/get-coords state)
    (draw-shape ctx (g/get-coords state) v-spacing h-spacing "red")))

