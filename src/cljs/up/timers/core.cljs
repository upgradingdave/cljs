(ns up.timers.core
  (:require [up.webworkers.core :as ww]
            [up.datetime        :refer [unparse-millis unparse-local]]
            [up.common          :refer [o->map]]
            [cljs-time.core     :as t]
            [cljs-time.format   :as tf]
            [cljs-time.local    :as tl]
            [cljs-time.coerce   :as tc]
            [cljs-time.predicates :as tp]
            [goog.date.duration :as duration]))

(defn calc-elapsed [total-millis elapsed-millis]
  (unparse-millis (if elapsed-millis 
                    (- total-millis (t/in-millis elapsed-millis)) 
                    total-millis)))

(defn advance
  "Update internal state of timer"
  [{:keys [now millis started running] :as timer-state}]
  (if running
    (let [elapsed (t/interval started now)
          remain  (- millis (t/in-millis (or elapsed 0)))]
      (if (<= remain 0)
        (-> timer-state 
            (assoc-in [:now] (t/now))
            (assoc-in [:elapsed] elapsed)
            (assoc-in [:completed] (t/now))
            (assoc-in [:running] false))
        (-> timer-state
              (assoc-in [:now] (t/now))
              (assoc-in [:elapsed] elapsed))))
    (assoc-in timer-state [:now] (t/now))))

(defn init-timer [timer-state]
  (-> timer-state
      (assoc :now       (t/now))
      (assoc :started   nil)
      (assoc :completed nil)
      (assoc :running   false)
      (assoc :millis    10000)))

(defn init-timer! [data path]
  (let [worker-path (conj path :worker)
        worker (get-in @data worker-path)]

    ;; create timer
    (swap! data update-in path init-timer)
    
    ;; create web worker
    (when (not worker)
      (let [worker (ww/webworker-create! data worker-path "timer.js")]
        (set! (.-onmessage worker) 
              (fn [e] 
                (swap! data update-in path advance)))))))

(defn restart-timer [timer-state]
  (-> timer-state
      (assoc :now      (t/now))
      (assoc :started  (t/now))
      (assoc :completed nil)
      (assoc :running   true)))

(defn pause-timer [timer-state]
  (-> timer-state
      (assoc :now     (t/now))
      (assoc :running false)))

(defn pause-timer! [data path]
  (swap! data update-in data path pause-timer))

(defn start-timer [timer-state]
  (-> timer-state
      (assoc :started (:now timer-state))
      (assoc :running true)))

(defn start-timer! [data path]
  (swap! data update-in path start-timer))

