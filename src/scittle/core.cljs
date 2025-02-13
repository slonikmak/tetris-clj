(ns scittle.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [scittle.game :as g]
            [scittle.shapes :as shape]
            [scittle.field :as field]))


(def cell-size (str (/ g/width g/v-count) "px"))

(defonce state (r/atom nil))
(defonce game-interval (atom nil))
(defonce shape-down-interval (atom nil))

(defn key->action [key]
  (case key
    "ArrowDown" :down
    "ArrowUp" :up
    "ArrowLeft" :left
    "ArrowRight" :right
    "Shift" :rotate
    nil))

(defn keydown-handler [e]
  (when-let [action (key->action (.-key e))]
    (println "Key pressed:" action)
    (swap! state update :actions conj action)))

(defn game-step []
  (when (:running @state)
    (swap! state
           (fn [s]
             (let [action (first (:actions s))
                   new-state (if action
                               (g/update-state s action)
                               s)
                   new-field (field/clear-full-rows (:field new-state))]
               (-> new-state
                   (assoc :field new-field)
                   (update :actions disj action)))))))

(defn shape-down []
  (swap! state update :actions conj :down))

(defn start-game []
  (println "Start the game")
  (reset! state (g/init))
  (.addEventListener js/window "keydown" keydown-handler)
  (swap! state assoc :running true :game-over false)
  (reset! game-interval (js/setInterval game-step 100))
  (reset! shape-down-interval (js/setInterval shape-down 300)))

(defn stop-game []
  (println "Stop the game")
  (.removeEventListener js/window "keydown" keydown-handler)
  (swap! state assoc :running false)
  (when @game-interval
    (js/clearInterval @game-interval)
    (js/clearInterval @shape-down-interval)
    (reset! game-interval nil)
    (reset! shape-down-interval nil)))


(defn game-grid []
  (let [board (r/track #(get @state :field))
        shape-coords (r/track #(set (when (:shape @state) (shape/get-coords (:shape @state)))))]
    [:div {:style {:display               "grid"
                   :grid-template-columns (apply str (repeat 10 (str cell-size " ")))
                   :grid-template-rows    (apply str (repeat 20 (str cell-size " ")))
                   :background            "#c7c7c7"
                   :border                "1px solid black"
                   :box-sizing            "border-box"}}
     (for [r (range 20) c (range 10)]
       ^{:key (str r "-" c)}
       [:div {:style {:width      cell-size
                      :height     cell-size
                      :background (cond
                                    (contains? @shape-coords [c r]) "blue"
                                    (= 1 (get-in @board [r c])) "gray"
                                    :else "white")
                      :border     "1px solid #c7c7c7"
                      :box-sizing "border-box"}}])]))

(defn down-coords [coords]
  (map (fn [[x y]] [x (inc y)]) coords))

(defn next-piece-grid []
  (let [shape-coords (r/track #(set (when (:shape @state) (down-coords (shape/get-coords (:next-shape @state))))))]
    [:div {:style {:display               "grid"
                   :grid-template-columns (apply str (repeat 4 (str cell-size " ")))
                   :grid-template-rows    (apply str (repeat 4 (str cell-size " ")))
                   :background            "black"
                   :margin-left cell-size}}
     (for [r (range 4) c (range 4)]
       ^{:key (str r "-" c)}
       [:div {:style {:width      cell-size
                      :height     cell-size
                      :background (if (contains? @shape-coords [c r]) "blue" "white")}}])]))

(defn game-component []
  [:div {:style {:display         "flex"
                 :justify-content "center"
                 :align-items     "center"}}
   [:div {:style {:display "flex" :padding cell-size}}
    [game-grid]
    ;; Боковая панель
    [:div {:style {:display        "flex"
                   :flex-direction "column"
                   :align-items    "center"
                   :gap            "10px"}}
     [next-piece-grid]
     [:button {:id "start-btn" :on-click start-game} "Start"]
     [:button {:id "stop-btn" :on-click stop-game} "Stop"]
     [:div {:id    "game-over"
            :style {:color      "red"
                    :font-size  "18"
                    :margin-top "10px"
                    :display    (if (:game-over @state) "block" "none")}}
      "Game over!"]]]])



(rdom/render [game-component] (.getElementById js/document "app"))