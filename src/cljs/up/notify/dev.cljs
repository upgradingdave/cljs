(ns up.notify.dev
  (:require
   [devcards.core  :as dc :include-macros true]
   [reagent.core   :as r]
   [up.notify.core :as n])
  (:require-macros
   [devcards.core :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(def alerts-path [:alerts])
(def notify-path [:notifications])

(def ex1 (r/atom {}))

(defcard 
  "### Notification Example"
  (dc/reagent 
   (fn [data _]
     [n/example data notify-path [:notify-form]]))
  ex1
  {:inspect-data true})

(defcard 
  "### Toggle Notifications"
  (dc/reagent 
   (fn [data _]
     [n/toggle-notifications data notify-path]))
  ex1
  {:inspect-data true})

(defcard 
  "### All Together"
  (dc/reagent 
   (fn [data _]
     [n/full-example data notify-path]))
  ex1
  {:inspect-data true})

(deftest unit-tests
  (testing "Notifications"
    (is (n/notification-permitted?))))

(defn init! [data]
  (js/console.log "Initializing notifications ...")
  (n/notification-init! data notify-path))

(defn main []
  (init! ex1)
  (dc/start-devcard-ui!))
