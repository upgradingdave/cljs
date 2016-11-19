(ns up.person.dev
  (:require
   [devcards.core     :as dc]
   [reagent.core      :as r]
   [up.person.core    :as p])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(def data (r/atom {}))

(defcard
  "### Height"
  (dc/reagent 
   (fn [data _] 
     [:div.container
      [:div.row 
       [p/height-form-group data [:person :height]]]]))
  data
  {:inspect-data true})
(defcard
  "### Age"
  (dc/reagent 
   (fn [data _] 
     [:div.container
      [:div.row
       [p/age-form-group data [:person :age]]]]))
  data
  {:inspect-data true})

(defcard
  "### Weight"
  (dc/reagent 
   (fn [data _] 
     [:div.container
      [:div.row
       [p/weight-form-group data [:person :weight]]]]))
  data
  {:inspect-data true})

(defcard
  "### Gender"
  (dc/reagent 
   (fn [data _] 
     [:div.container
      [:div.row
       [p/gender-form-group data [:person :gender]]]]))
  data
  {:inspect-data true})

