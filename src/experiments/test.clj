(ns experiments.test
  (:require [cljfx.api :as fx]
            [clojure.core.async :as async]))

(def renderer
  (fx/create-renderer))

(def events (atom (async/chan 1)))

(defn stop []
  (do
    (println "Producer: Closing channel")
    (async/close! @events)))

(defn producer []
  (async/go-loop []
    (when (async/>! @events "eeeeeeee")
      (println "Sent!")
      (async/<! (async/timeout 1000))                       ; delay of 1 second
      (recur))))

(defn consumer []
  (async/go-loop []
    (if-let [v (async/<! @events)]
      (do
        (println "Consumer received:" v)
        (renderer {:fx/type root
                   :showing true
                   :text    (str v)})
        (recur))
      (println "Consumer: Channel closed."))))


(defn start []
  (reset! events (async/chan 1))
  (producer)
  (consumer))

(defn send-event [e]
  (async/go
    (async/>! @events e)))



(defn root [{:keys [showing text]}]
  {:fx/type :stage
   :showing showing
   :scene   {:fx/type :scene
             :root    {:fx/type  :v-box
                       :padding  50
                       :children [{:fx/type :label
                                   :text    text}
                                  {:fx/type   :button
                                   :text      "Start"
                                   :on-action (fn [_]
                                                (start))}
                                  {:fx/type   :button
                                   :text      "Stop"
                                   :on-action (fn [_]
                                                (stop))}
                                  {:fx/type   :button
                                   :text      "Send"
                                   :on-action (fn [_]
                                                (send-event (System/currentTimeMillis)))}]}}})

(renderer {:fx/type root
           :showing true
           :text    "32"})

