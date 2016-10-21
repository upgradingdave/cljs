(ns up.todo.dev
  (:require
   [cljs-time.core       :as t]
   [cljs-time.format     :as tf]
   [devcards.core        :as dc]
   [reagent.core         :as r]
   [up.timers.core       :as timer]
   [up.todo.core         :as todo]

   [goog.date.duration :as duration])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(defcard 
  "### Timer"
  (dc/reagent (fn [data _]
                [todo/timer data todo/timer-path]))
  todo/data
  {:inspect-data true})

;; (defcard-doc 
;;   "### Timer

;;   The timer just counts down from seconds found in state atom
;; "
;;   (dc/mkdn-pprint-source todo/timer))

(defn main [])
