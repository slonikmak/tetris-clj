(ns experiments.experiments
  (:require [clojure.core.async :as async]))




(defn start []
  (let [ch (async/chan 1)]

    (async/go-loop [state {:val 0}]
      (if (< (:val state) 10)
        (do
          (println (str "Send: " (:val state)))
          (async/>! ch (:val state))
          (async/<! (async/timeout 500))
          (recur (assoc state :val (inc (:val state)))))
        (async/close! ch)))

    (async/go-loop []
      (if-let [v (async/<! ch)]
        (do
          (println "Consumer received:" v)
          (recur))
        (println "Consumer: Channel closed.")))))

(comment
  (start))
