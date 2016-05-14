(ns upgradingdave.lattice_dev
  (:require
   [devcards.core         :as dc]
   [reagent.core          :as r]
   [upgradingdave.lattice :as lat])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(defcard-doc 
  "### Project Euler Problem #15 - Lattice Paths

  This is a recursive solution. If we start at any point on the far
  right edge or far bottom edge, we know there is only 1 path to the
  bottom right corner.

  Each time we move one up or left one step to coordinate (x,y), we
  can calculate the number of paths from the the new location by
  adding the number of paths from (x, y-1) to the number of paths
  from (y-1, x).

  Here's the recursive clojurescript function that implements this
  algorithm:

  "
  (dc/mkdn-pprint-source lat/calc-path))

(defcard 
  "### Lattice Solver"
  (dc/reagent
   (fn [data _]
     (lat/widget data)))
  lat/data
  {:inspect-data true}
)

(defcard 
  "### Lattice"
  (dc/reagent
   (fn [data _]
     (lat/lattice data)))
  lat/data)

(defcard 
  "### Form to Generate new Lattice"
  (dc/reagent
   (fn [data _]
     [lat/size-form data]))
  lat/data)

;; (defcard 
;;   "### Cell"
;;   (dc/reagent 
;;    (fn [data _] [:div [lat/cell 0 data]
;;                  [:div {:style {:clear "both"}}]]))
;;   lat/data)

;; (defcard 
;;   "### Row"
;;   (dc/reagent
;;    (fn [data _]
;;      (let [cells (:cells @data) 
;;            size  (:size @data)
;;            rows  (partition size (map-indexed vector cells))]
;;        (lat/cell-row (first rows) data))))
;;   lat/data)

(devcards.core/start-devcard-ui!)
