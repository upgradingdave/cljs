(ns up.bingo.dev.components
  (:require
   [devcards.core       :as dc :include-macros true]
   [reagent.core        :as r]
   [up.bingo.core       :as b]
   [up.bingo.components :as c]
   [up.bingo.css        :as css]
   [up.bingo.dev.data   :as d])
  (:require-macros
   [devcards.core :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(defn color-swatch [color-name hex]
  [:div {:key color-name
         :style {:width "100px"
                 :background-color hex}} 
   (name color-name)])

(defcard 
  "### Bingo Cell"
  (dc/reagent 
   (fn [data _]
     [:div
      [c/cell data [:cell]]
      ;; override defaults to change the size
      [c/cell data [:cell] {:cell-width  75
                            :cell-height 75
                            :font-size   10}]
      ;; even smaller
      [c/cell data [:cell] {:cell-width  50
                            :cell-height 50
                            :font-size   5}]

      [c/cell data [:cell] {:cell-width  25
                            :cell-height 25
                            :font-size   5}]
      ]))
  (r/atom {:cell {:key "00" :value "Example"}})
  {:inspect-data true})

(defcard 
  "### Bingo Board"
  (dc/reagent 
   (fn [data _]
     (c/board data [:bingo :board] {:cell-width  35
                                    :cell-height 35
                                    :font-size   5
                                    :gutter-size 2})))
  (r/atom {:bingo {:board (b/make-board (take 25 d/words))}})
  {:inspect-data true})

(defcard 
  "### Bingo Board"
  (dc/reagent 
   (fn [data _]
     (c/board data [:bingo :board] {:cell-width  75
                                    :cell-height 75
                                    :font-size   10
                                    :gutter-size 5})))
  (r/atom {:bingo {:board (b/make-board (take 25 d/words))}})
  {:inspect-data false})

(defcard 
  "### Bingo Board"
  (dc/reagent 
   (fn [data _]
     (c/board data [:bingo :board])))
  (r/atom {:bingo {:board (b/make-board (take 25 d/words))}})
  {:inspect-data false})

(defcard 
  "### Bingo Boards"
  (dc/reagent 
   (fn [data _]
     (c/leader-boards data [:bingo] {:cell-width  35
                                     :cell-height 35
                                     :font-size   5
                                     :gutter-size 2})))
  (r/atom {:bingo 
           {:players 
            {:boards 
             [{:board (b/make-board (take 25 (shuffle d/words)))}
              {:board (b/make-board (take 25 (shuffle d/words)))}
              {:board (b/make-board (take 25 (shuffle d/words)))}]}}})
  {:inspect-data false})

(defcard 
  "### Color Palette"
  (dc/reagent 
   (fn [data _]
     [:div
      (map (fn [[color hex]] (color-swatch color hex)) @data)]))
  (r/atom css/colors) 
  {:inspect-data false})
