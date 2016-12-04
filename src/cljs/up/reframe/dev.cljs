(ns up.reframe.dev
  (:require
   [devcards.core   :as dc :include-macros true]
   [reagent.core    :as r]
   [up.reframe.core :as c])
  (:require-macros
   [devcards.core :refer [defcard deftest defcard-doc]]
   [cljs.test     :refer [is testing]]))

(deftest unit-tests
  (testing "Sanity"
    (is (= false false))))

(defcard 
  "## Simple"
  (dc/reagent 
   (fn [data _]
     [:div
      [c/greet-input]
      [c/greet-btn]
      [c/greet]]))
  re-frame.db/app-db
  {:inspect-data true})

(defn main   []
  (c/main))

(defn reload [])

