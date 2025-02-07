(ns tetris-clj.execution
  (:require [clojure.core.async :as async]))


(defn start-task [task delay running?]
  (when (running?)
    (async/go-loop []
      (when (running?)                                       ; Check if the flag is true
        (task)
        (async/<! (async/timeout delay))                    ; Wait for 5 seconds
        (recur)))))

