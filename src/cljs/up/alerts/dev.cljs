(ns up.alerts.dev
  (:require
   [devcards.core  :as dc :include-macros true]
   [reagent.core   :as r]
   [up.alerts.core :as a])
  (:require-macros
   [devcards.core :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(def alerts-path [:alerts])
(def ex1 (r/atom {}))

(defcard 
  "### Success"
  (dc/reagent (fn [data _]
                [a/alert data alerts-path "ex1" {:class "alert-success"} 
                 [:div [:strong "Awesome!"] " Success Alerts Work"] ]))
  ex1
  {:inspect-data false})

(defcard 
  "### Info"
  (dc/reagent (fn [data _]
                [a/alert data alerts-path "ex2" {:class "alert-info"} 
                 [:div [:strong "Cool."] " Info Alerts Work"] ]))
  ex1
  {:inspect-data false})

(defcard 
  "### Warning"
  (dc/reagent (fn [data _]
                [a/alert data alerts-path "ex3" {:class "alert-warning"} 
                 [:div [:strong "Hmm ..."] " Warning Alerts Work"] ]))
  ex1
  {:inspect-data false})

(defcard 
  "### Danger"
  (dc/reagent (fn [data _]
                [a/alert data alerts-path "ex4" {:class "alert-danger"} 
                 [:div [:strong "Yikes!"] " Danger Alerts Work"] ]))
  ex1
  {:inspect-data false})

(defcard 
  "### Danger"
  (dc/reagent (fn [data _]
                [a/dismissable data alerts-path "ex5" {:class "alert-warning"} 
                 [:div [:strong "Heads up:"] 
                  " Dismissable Alerts Work, too"] ]))
  ex1
  {:inspect-data true})


