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

  ;; notification popup

  ;; create task

  ;; delete task

  ;; time a task
)

(def total (* 25 duration/MINUTE_MS_))

(def data (r/atom {:timer {:millis total}}))

(def worker-path [:webworker])
(def timer-path  [:timer])
(def notify-path [:notify])

;;;; Web Workers
(defn webworker? []
  (undefined? (.-document js/self)))

(defn webworker-create [data path]
  (let [w (js/Worker. "js/compiled/todo.js")]
    (swap! data assoc-in (conj path :worker) w)))

(defn webworker-get [data path]
  (get-in @data (conj path :worker)))

(defn webworker-post [data path val]
  (.postMessage (webworker-get data path) val)
  (js/console.log "posted message to worker"))

(defn webworker-onreceive
  "Here's a function for main thread to get messages from web worker"
  [e]
  (js/console.log "Received message from worker")
  (js/console.log (.-data e)))

(defn webworker-onmessage 
  "Here's a function for the webworker to use to get messages from
  main thread"
  [e]
  (js/console.log "Received message from main (inside worker)")
  (js/console.log (.-data e)))

;;set up listener if this is the webworker

;; (if (webworker?)
;;   (set! (.-onmessage js/self) webworker-onmessage)
;;   (do
;;     (when (not (webworker-get data worker-path))
;;       (webworker-create data worker-path))
;;     (set! (.-onmessage (webworker-get data worker-path)) webworker-onreceive)))

;;;; Notifications

(defn notification-supported? []
  js/Notification)

(defn notification-permission []
  (.-permission js/Notification))

(defn notification-permitted? []
  (= "granted" (notification-permission)))

(defn notification-request-permission! [data path]
  (.requestPermission 
   js/Notification 
   (fn [permission]
     (swap! data assoc-in path {:enabled true
                                :permitted (= "granted" permission)
                                :permission permission}))))

(defn notification-enabled? [data path]
  (get-in @data (conj path :enabled)))

(defn show-notification [msg data path]
  (let [title "Time's Up" 
        icon  "https://pupeno.files.wordpress.com/2015/08/clojure-logo.png"
        n (js/Notification. 
           title 
           (clj->js {:body msg
                     :icon icon}))]
    (.play (js/Audio. "http://www.wavlist.com/holidays/003/tnks-turkey.wav"))
    (js/setTimeout #(.close n), 5000)
    (swap! data assoc-in (conj path :active) n)))

(defn notification-init! 
  [data path]
  (if (notification-permitted?)
    (let [permission (notification-permission)]
      (swap! data assoc-in path 
             {:enabled    (get-in @data (conj path :enabled) true)
              :permitted  (= "granted" permission)
              :permission permission}))
    (notification-request-permission! data path))
  
  ;; TODO move this
  (add-watch data :watcher1 
             (fn [k ref o n]
               (let [old-completed (get-in o (conj timer-path :completed))
                     new-completed (get-in n (conj timer-path :completed))]
                 (when (and (not old-completed)
                            new-completed)
                   (show-notification "Hooray!" data notify-path))))))

(defn toggle-notifications [data path]
  (let [{:keys [enabled permitted]} (get-in @data path)]

    (if (nil? enabled)
      (notification-init! data path))

    [:div {:class "btn-group"} 
     [:button 
      {:on-click #(swap! data assoc-in (conj path :enabled) true)
       :class (str "btn" (if (and permitted enabled) 
                           " btn-success" " btn-default"))
       :disabled (if (not permitted) "disabled")} 
      "Enable"]
     [:button 
      {:on-click #(swap! data assoc-in (conj path :enabled) false)
       :class (str "btn" (if (and permitted (not enabled))
                           " btn-danger" " btn-default"))
       :disabled (if (not permitted) "disabled")} 
      "Disable"]]

    ))


;;;; Timer

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

(def time-format (tf/formatter "hh:mm a"))

(defn elapse 
  "If the timer is started, calculate interval and update elapsed "
  [{:keys [now millis started running] :as timer-state}]
  (if running
    (let [elapsed (t/interval started now)
          remain  (- millis (t/in-millis (or elapsed 0)))]
      (if (<= remain 0)
        (do
          (-> timer-state 
              (assoc-in [:completed] (t/now))
              (assoc-in [:running] false)))
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

       [:h1 
        (let [{:keys [minutes seconds hours]} 
              (unparse-millis (if elapsed (- total (t/in-millis elapsed)) 
                                  total))]
          (gstr/format "%02d:%02d:%02d" hours minutes seconds))]

       [:div {:class "btn-group"}
        [:button {:class "btn btn-primary" 
                  :on-click #(swap! data update-in path restart-timer)} 
         (if started "Restart" "Start")]
        
        [:button 
         {:class "btn btn-default" 
          :disabled (if (or (not started)
                            completed) "disabled")
          :on-click (if running 
                      #(swap! data update-in path pause-timer)
                      #(swap! data update-in path start-timer))} 
         (cond 
           
           (and started (not running) (not completed))
           "Continue"

           :else 
           "Pause"
           )]]
       
       ])))

(defn main []
  (if (not (webworker?))
    (do
      (when (not (webworker-get data worker-path))
        (webworker-create data worker-path))
      (if-let [node (.getElementById js/document "todo-gen")]
        (r/render-component [timer data timer-path] node)))))

(main)




