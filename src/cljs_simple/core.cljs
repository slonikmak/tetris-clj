(ns cljs_simple.core
  (:require [cljs.core.async :refer [chan <! >! go-loop go timeout close!]]
            [goog.events :as events]
            [hiccups.runtime :as hr]
            [cljs-simple.layout :as l]
            [common.game :as g]
            [common.field :as field]))


(defn key->action [key]
  (case key
    "ArrowDown" :down
    "ArrowUp" :up
    "ArrowLeft" :left
    "ArrowRight" :right
    "Shift" :rotate
    nil))


(defn keydown-handler [events]
  (fn [e]
    (when-let [action (key->action (.-key e))]
      (println "Key pressed:" action)
      (go (>! events action)))))

(defn game-step [state action]
  (if (= :stop action)
    (assoc state :running false :game-over true)
    (let [new-state (g/update-state state action)
          new-field (field/clear-full-rows (:field new-state))]
      (assoc new-state :field new-field))))

(defn render [state ctx next-ctx]
  (l/draw-grid ctx g/v-count g/h-count state g/width g/height g/v-spacing g/h-spacing)
  (l/draw-next-shape next-ctx state g/v-spacing g/h-spacing "red"))

(defn start-game [ctx next-ctx]
  (println "Start the game")
  (let [events (chan 1)
        key-handler (keydown-handler events)
        stop-fn (fn []
                  (println "Stop the game")
                  (.removeEventListener js/window "keydown" key-handler)
                  (close! events))]

    (.addEventListener js/window "keydown" key-handler)

    (go-loop []
             (do
               (>! events :down)
               (<! (timeout g/game-speed))
               (recur)))

    (go-loop [state (assoc (g/init) :events events)]
             (if (:running state)
               (when-let [action (<! events)]
                 (let [new-state (game-step state action)]
                   (render new-state ctx next-ctx)
                   (recur new-state)))
               (stop-fn)))

    stop-fn))


(defn add-btn-listeners []
  (let [start-btn (.getElementById js/document "start-btn")
        stop-btn (.getElementById js/document "stop-btn")
        game-board (.getElementById js/document "game-board")
        next-canvas (.getElementById js/document "next-canvas")
        stop-game-atom (atom nil)]

    (.addEventListener start-btn "click"
                       (fn [_]
                         (reset! stop-game-atom (start-game (.getContext game-board "2d") (.getContext next-canvas "2d")))))
    (.addEventListener stop-btn "click"
                       (fn [_]
                         (when-let [stop-fn @stop-game-atom]
                           (stop-fn)
                           (reset! stop-game-atom nil))))
    (l/draw-grid (.getContext game-board "2d") g/v-count g/h-count {} g/width g/height g/v-spacing g/h-spacing))

  )


(defn init []
  (let [app (.getElementById js/document "app")
        html (hr/render-html (l/markup g/width g/height))]
    (set! (.-innerHTML app) html)
    (add-btn-listeners)))

(set! (.-onload js/window) init)