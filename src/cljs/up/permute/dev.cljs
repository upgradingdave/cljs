(ns up.permute.dev
  (:require
   [devcards.core   :as dc :include-macros true]
   [reagent.core    :as r]
   [up.permute.core :as c]
   [up.permute.css  :as css])
  (:require-macros
   [devcards.core :refer [defcard deftest defcard-doc]]
   [cljs.test     :refer [is testing]]))

(defcard 
  "## Permute"
  (dc/reagent 
   (fn [data _]
     [c/widget]))
  re-frame.db/app-db
  {:inspect-data true})

(deftest unit-tests
  (testing "Find starting index of suffix"
    (is (= 4 (c/find-suffix (c/str->perm "01234"))))
    (is (= 2 (c/find-suffix (c/str->perm "345330"))))
    (is (= 4 (c/find-suffix (c/str->perm "zxybca"))))
    (is (= 0 (c/find-suffix (c/str->perm "zyxcba")))))
  (testing "Find starting index of swap"
    (is (= 4 (c/find-swap (c/str->perm "01234")   4)))
    (is (= 5 (c/find-swap (c/str->perm "3425330") 3))))
  (testing "swap"
    (is (= "41230" (c/perm->str (c/swap (c/str->perm "01234") 0 4))))
    (is (= "02134" (c/perm->str (c/swap (c/str->perm "01234") 1 2))))
    (is (= "01234" (c/perm->str (c/swap (c/str->perm "01234") 4 4)))))
  (testing "reverse suffix"
    (is (= "04321" (c/perm->str (c/reverse-suffix (c/str->perm "01234") 1))))))

(defn color-swatch [color-name hex]
  [:div {:key color-name
         :style {:width "100px"
                 :background-color hex}} 
   (name color-name)])

(defcard 
  "### Color Palette"
  (dc/reagent 
   (fn [data _]
     [:div
      (map (fn [[color hex]] (color-swatch color hex)) @data)]))
  (r/atom css/colors) 
  {:inspect-data false})

(defn main []
  (c/main))

(defn reload [])

