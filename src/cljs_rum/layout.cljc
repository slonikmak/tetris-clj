(ns cljs-rum.layout
  (:require [common.game :as g]))

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
  (when (:next-shape state)
    (let [next-fig (g/next-shape state)]
      (draw-shape ctx next-fig cell-width cell-height color))))

(defn draw-game-board
  [ctx state v-count h-count width height v-spacing h-spacing]
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
  (when (:shape state)
    (draw-shape ctx (g/get-coords state) v-spacing h-spacing "red")))