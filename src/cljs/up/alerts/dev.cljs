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
  "### Dismissable Alerts"
  (dc/reagent (fn [data _]
                (let [path (conj alerts-path :ex5)]
                  [:div
                   [:div {:class "form-group"}
                    [:div {:class "btn btn-primary"
                           :on-click #(a/open data path)}
                     "Click Me"]]
                   [a/dismissable data path
                    [:div [:strong "Heads up:"] 
                     "  Dismissable Alerts Work"]
                    {:class "alert-warning"}]])))
  ex1
  {:inspect-data true})

(defcard 
  "### Success"
  (dc/reagent (fn [data _]
                [a/alert "ex1"
                 [:div [:strong "Awesome!"] " Success Alerts Work"]
                 {:class "alert-success"} ]))
  ex1
  {:inspect-data false})

(defcard 
  "### Info"
  (dc/reagent (fn [data _]
                [a/alert "ex2"
                 [:div [:strong "Cool."] " Info Alerts Work"]
                 {:class "alert-info"} ]))
  ex1
  {:inspect-data false})

(defcard 
  "### Warning"
  (dc/reagent (fn [data _]
                [a/alert "ex3" 
                 [:div [:strong "Hmm ..."] " Warning Alerts Work"]
                 {:class "alert-warning"}]))
  ex1
  {:inspect-data false})

(defcard 
  "### Danger"
  (dc/reagent (fn [data _]
                [a/alert "ex4" 
                 [:div [:strong "Yikes!"] " Danger Alerts Work"]
                 {:class "alert-danger"}  ]))
  ex1
  {:inspect-data false})


