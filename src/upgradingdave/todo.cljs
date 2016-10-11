(ns upgradingdave.todo
  (:require
   [cljs-time.core   :as t]
   [cljs-time.format :as tf]
   [reagent.core :as r]
   [clojure.test.check.generators :as gen]
   
   [goog.date.duration :as duration]
   [goog.string :as gstr]
   [goog.string.format]))

(comment
  ;; todo

  ;; able to stop and start

  ;; create task

  ;; delete task

  ;; time a task
)

(defn unparse-millis
  "Unparse seconds into minutes, seconds, days, hours.
  `goog.date.duration.format` almost does what I needed, but it doesn't
  provide seconds"
  [millis]
  (if (<= millis 0) 
    {:seconds 0}
    (let [days   (js/Math.floor (/ millis duration/DAY_MS_))
          millis (mod millis duration/DAY_MS_)
          hours  (js/Math.floor (/ millis duration/HOUR_MS_))
          millis (mod millis duration/HOUR_MS_)
          mins   (js/Math.floor (/ millis duration/MINUTE_MS_))
          millis (mod millis duration/MINUTE_MS_)
          secs   (js/Math.floor (/ millis 1000))
          millis (mod millis 1000)]
      {:days    days
       :hours   hours
       :minutes mins
       :seconds secs
       :millis  millis})))

(def time-format (tf/formatter "HH:mm:ss"))

(defn elapse 
  "If the timer is started, calculate interval and update elapsed "
  [{:keys [now millis started running] :as timer-state}]
  (if running
    (let [elapsed (t/interval started now)
          remain  (- millis (t/in-millis (or elapsed 0)))]
      (if (<= remain 0)
        (-> timer-state 
            (assoc-in [:completed] (t/now))
            (assoc-in [:running] false))
        (assoc-in timer-state [:elapsed] elapsed)))
    timer-state))

(defn advance 
  "Take the state of the timer and advance everything"
  [timer-state]
  (-> timer-state 
      (assoc-in [:now] (t/now))
      (elapse)))

(defn restart-timer [timer-state]
  (-> timer-state
      (assoc :started (t/now))
      (assoc-in [:completed] nil)
      (assoc :running true)))

(defn pause-timer [timer-state]
  (-> timer-state
      (assoc :running false)))

(defn start-timer [timer-state]
  (-> timer-state
      (assoc :running true)))

(defn run-loop! 
  "Schedule `advance` to run a second from now and update the global
  state"
  [data path]
  (js/setTimeout #(swap! data update-in path 
                         (fn [o] (advance o))) 1000))

(defn timer [data path]
  (fn [data path]
    (let [now       (get-in @data (conj path :now) (t/now))
          elapsed   (get-in @data (conj path :elapsed))
          started   (get-in @data (conj path :started))
          total     (get-in @data (conj path :millis))
          running   (get-in @data (conj path :running))
          completed (get-in @data (conj path :completed))]
      (run-loop! data path)
      [:div {:id "timer"}

       [:button {:on-click #(swap! data update-in path restart-timer)} 
        (if started "Restart" "Start")]
       
       (if running 
         [:button {:on-click #(swap! data update-in path pause-timer)} 
          "Pause"])

       (if (and started (not running) (not completed))
         [:button {:on-click #(swap! data update-in path start-timer)} 
          "Continue"])
       
       [:div (str "Current Time: "
                  (tf/unparse time-format (t/to-default-time-zone now)))]
       [:div "Time Remaining: " 
        (let [{:keys [minutes seconds hours]} 
              (unparse-millis (if elapsed (- total (t/in-millis elapsed)) 
                                  total))]
          (gstr/format "%02d:%02d:%02d" hours minutes seconds))]
       ])))

(def data (r/atom {:timer {:millis (* 1 duration/MINUTE_MS_)}}))
(def timer-path [:timer])

(defn main []
  (if-let [node (.getElementById js/document "pwd-gen")]
    (r/render-component [timer data timer-path] node)))

(main)




