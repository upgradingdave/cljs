(ns upgradingdave.pcf-dev
  (:require
   [devcards.core     :as dc]
   [reagent.core      :as r]
   [upgradingdave.pcf :as pcf])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(def data (r/atom 
           (let [pixels (into [] (repeat 64 [7 [:white "111"]]))] 
             {:pixels pixels
              :editor-value (pcf/pixels-to-binary pixels)})))

(deftest get-next-color
  (testing "testing get next color"
    (is (= [0 [:red    "000"]] (pcf/get-next-color 7)))
    (is (= [2 [:yellow "010"]] (pcf/get-next-color 1)))))

(defcard 
  "### Pixel"
  (dc/reagent 
   (fn [data _] [:div [pcf/pixel 0 data {:show-codes? true}]
                 [:div {:style {:clear "both"}}]]))
  data)

(defcard 
  "### Pixel Row"
  (dc/reagent 
   (fn [data _] 
     [pcf/pixel-row 
      (first (partition 8 (map-indexed vector (:pixels @data)))) data
      {:show-codes? true}
      ]))
  data)

(defcard 
  "### Image"
  (dc/reagent (fn [data _] [pcf/image-editor data]))
  data
;;  {:inspect-data true}
  )

(deftest binary-conversion
  (testing "testing converting binary"
    (is (= [[0 [:red "000"]] [7 [:white "111"]]] 
           (pcf/binary-to-pixels "000111")))))

(defcard 
  "### Binary"
  (dc/reagent (fn [data _] [pcf/binary-editor data]))
  data)

(defcard 
  "### Editor"
  (dc/reagent (fn [data _] [pcf/editor data]))
  data
;;  {:inspect-data true}
  )

