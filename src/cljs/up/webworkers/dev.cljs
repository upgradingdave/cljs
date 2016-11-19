(ns up.webworkers.dev
  (:require
   [devcards.core :as dc :include-macros true]
   [reagent.core  :as reagent]
   [up.alerts.core       :as a]
   [up.timers.core       :as t]
   [up.webworkers.core   :as ww]
   [up.webworkers.timer  :as tw])
  (:require-macros
   [devcards.core :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(def ww-path [:worker])
(def ex1 (atom {}))
(def ex2 (atom {}))
(def ex3 (atom {:timer {}}))

(defcard 
  "### Javascript Web Worker"
  (dc/reagent (fn [data _]
                [:div
                 [:div {:class "form-group"}
                  [:div [:button 
                         {:class "btn btn-primary"
                          :on-click #(ww/webworker-post data ww-path "")}
                         "Run Web Worker"]]]
                 [a/dismissable data [:alerts] "js-ww-alert" 
                  (get-in @data [:result :js]) ]
                 ]))
  ex1
  {:inspect-data true})

(defcard 
  "### Clojurescript Web Worker"
  (dc/reagent (fn [data _]
                [:div {:class "form-group"}
                 [:div [:button 
                        {:class "btn btn-primary"
                         :on-click #(ww/webworker-post data ww-path "")} 
                        "Run Web Worker"]]
                 [a/dismissable data [:alerts] "cljs-ww-alert"
                  (get-in @data [:result :cljs])]
                 ]))
  ex2
  {:inspect-data true})

(defcard 
  "### Timer Web Worker"
  (dc/reagent (fn [data _]
                [:div (t/unparse t/date-time-format 
                                 (get-in @data [:timer :now]))]))
  ex3
  {:inspect-data true})

(deftest unit-tests
  (testing "Web Workers"
    (is (not (ww/webworker?)))))

(defn show-event [data path]
  (fn [e]
    (swap! data assoc-in path (.-data e))))

(defn init  [data script-path event-handle]
  (js/console.log "Starting web worker ...")
  (ww/webworker-create! data ww-path script-path)
  (js/console.log "Registering web worker listener ...")
  (set! (.-onmessage (ww/webworker-get data ww-path)) event-handle)
  (set! (.-onerror (ww/webworker-get data ww-path)) 
        (fn [e] (js/console.error e)))

  (js/console.log "Web worker is ready."))

(defn main []
  (init ex1 "webworker.js" (show-event ex1 [:result :js]))
  (init ex2 "webworkers/simple.js" 
        (show-event ex2 [:result :cljs]))
  (init ex3 "webworkers/timer.js" 
        (fn [e] 
          (swap! ex3 assoc-in [:timer] (t/advance (get-in ex3 [:timer]))))))
