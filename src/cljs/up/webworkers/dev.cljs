(ns up.webworkers.dev
  (:require
   [devcards.core :as dc :include-macros true]
   [reagent.core  :as reagent]
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
                [:div [:button {:class "btn btn-primary"
                                :on-click #(ww/webworker-post data ww-path "")} 
                       "Run Web Worker"]]))
  ex1
  {:inspect-data true})

(defcard 
  "### Clojurescript Web Worker"
  (dc/reagent (fn [data _]
                [:div [:button {:class "btn btn-primary"
                                :on-click #(ww/webworker-post data ww-path "")} 
                       "Run Web Worker"]]))
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

(defn log-event [e]
  (js/console.log (.-data e)))

(defn init  [data script-path event-handle]
  (js/console.log "Starting web worker ...")
  (ww/webworker-create data ww-path script-path)
  (js/console.log "Registering web worker listener ...")
  (set! (.-onmessage (ww/webworker-get data ww-path)) event-handle)
  (set! (.-onerror (ww/webworker-get data ww-path)) 
        (fn [e] (js/console.error e)))

  (js/console.log "Web worker is ready."))

(defn main []
  (init ex1 "webworker.js" log-event)
  (init ex2 "wworker.js" log-event)
  (init ex3 "timer.js" 
        (fn [e] 
          (swap! ex3 assoc-in [:timer] (t/advance (get-in ex3 [:timer]))))))
