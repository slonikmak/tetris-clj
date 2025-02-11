(ns cljs-rum.core
  (:require [rum.core :as rum]
            [cljs.core.async :refer [chan <! >! go-loop go timeout close!]]
            [cljs-rum.layout :as l]
            [common.game :as g]
            [common.field :as field]))

(defonce *game-state (atom {}))

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

(defn start-game []
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
                   (reset! *game-state new-state)
                   (recur new-state)))
               (stop-fn)))
    stop-fn))


(rum/defc game-board < rum/reactive
                       {:did-mount   (fn [state]
                                       (let [node (rum/dom-node state)
                                             ctx (.getContext node "2d")]
                                         (l/draw-game-board ctx @*game-state g/v-count g/h-count g/width g/height g/v-spacing g/h-spacing)
                                         state))
                        :will-update (fn [state]
                                       (let [node (rum/dom-node state)
                                             ctx (.getContext node "2d")]
                                         (l/draw-game-board ctx @*game-state g/v-count g/h-count g/width g/height g/v-spacing g/h-spacing)
                                         state))}
  [width height]
  (rum/react *game-state)
  [:canvas {:id     "game-board"
            :width  width
            :height height
            :style  {:border "1px solid black"}}])

(rum/defc next-shape < rum/reactive
                       {:did-mount   (fn [state]
                                       (let [node (rum/dom-node state)
                                             ctx (.getContext node "2d")]
                                         (l/draw-next-shape ctx @*game-state g/v-spacing g/h-spacing "red")
                                         state))
                        :will-update (fn [state]
                                       (let [node (rum/dom-node state)
                                             ctx (.getContext node "2d")]
                                         (l/draw-next-shape ctx @*game-state g/v-spacing g/h-spacing "red")
                                         state))}
  []
  (rum/react *game-state)
  [:canvas {:id     "next-canvas"
            :width  150
            :height 150
            :style  {:padding "20px"}}])


(rum/defcs markup < (rum/local nil ::stop-fn)

  [state width height]
  [:div {:style {:display "flex"
                 :justifyContent "center"
                 :alignItems "center"
                 :height "100vh"}}
   [:div {:style {:display "flex"
                  :padding "20px"}}
    ;; Игровое поле
    (game-board width height)
    ;; Боковая панель
    [:div {:style {:display "flex"
                   :flexDirection "column"
                   :alignItems "center"
                   :gap "10px"}}
     (next-shape)
     [:button {:on-click #(reset! (::stop-fn state) (start-game))} "Start"]
     [:button {:on-click (fn [_]
                           (when-let [stop-fn @(::stop-fn state)]
                             (stop-fn)
                             (reset! (::stop-fn state) nil)))} "Stop"]
     [:div {:id "game-over"
            :style {:color "red"
                    :fontSize "24px"
                    :marginTop "10px"
                    :display "none"}}
      "Game over!"]]]])

(defn init []
  (rum/mount (markup g/width g/height) (.getElementById js/document "app")))

(set! (.-onload js/window) init)
