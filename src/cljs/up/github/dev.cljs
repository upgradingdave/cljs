(ns up.github.dev
  (:require
   [devcards.core   :as dc :include-macros true]
   [reagent.core    :as r]
   [up.github.core  :as c])
  (:require-macros
   [devcards.core :refer [defcard deftest defcard-doc]]
   [cljs.test     :refer [is testing]]))

(deftest unit-tests
  (testing "Github"
    (is (= "" ""))))

(defcard 
  "## Github"
  (dc/reagent 
   (fn [data _]
     [c/widget]))
  re-frame.db/app-db
  {:inspect-data true})

(defn main []
  (c/main))

(defn reload [])

