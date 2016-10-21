(ns up.todo.core
  (:require
   [reagent.core       :as r]
   [up.timers.core     :as t]
   [goog.date.duration :as duration]
   [goog.string        :as gstr]
   [goog.string.format]))

(comment
  ;; todo

  ;; create task

  ;; delete task

  ;; time a task
)

(def total (* 25 duration/MINUTE_MS_))

(def data (r/atom {:timer {:millis total}}))

(def worker-path [:webworker])
(def timer-path  [:timer])
(def notify-path [:notify])

(defn timer [data path]
  (let [st (get-in @data path)
        {:keys [now elapsed started total running completed]} st
        now (or now (t/now))]
    [:div {:id "timer"}
     [:h1 
      (let [{:keys [minutes seconds hours]} 
            (t/calc-elapsed elapsed total)]
        (gstr/format "%02d:%02d:%02d" hours minutes seconds))]

     [:div {:class "btn-group"}
      [:button {:class "btn btn-primary" 
                :on-click #(swap! data update-in path t/restart-timer)} 
       (if started "Restart" "Start")]
      
      [:button 
       {:class "btn btn-default" 
        :disabled (if (or (not started)
                          completed) "disabled")
        :on-click (if running 
                    #(swap! data update-in path t/pause-timer)
                    #(swap! data update-in path t/start-timer))} 
       (cond 
         
         (and started (not running) (not completed))
         "Continue"

         :else 
         "Pause"
         )]]
     
     ]))

;; (defn main []
;;   (if-let [node (.getElementById js/document "todo-gen")]
;;     (r/render-component [timer data timer-path] node)))

;; ;;(main)





