(ns up.bingo.dev.components
  (:require
   [devcards.core       :as dc :include-macros true]
   [reagent.core        :as r]
   [up.bingo.core       :as b]
   [up.bingo.components :as c]
   [up.bingo.css        :as css]
   [up.bingo.dev.data   :as d]
   [up.env              :as env])
  (:require-macros
   [devcards.core :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(def css-transition-group
  (r/adapt-react-class js/React.addons.CSSTransitionGroup))

(defn word [data pending-path word]
  (let [opacity (r/atom 1)]
    {:render
     (fn [data pending-path word]
       [:li {:style {:opacity opacity}} 
        [:div (str "Did you hear '"word"'?")] 
        [:div.btn-group
         [:btn {:class "btn btn-primary"
                :on-click #(b/confirm! data pending-path word false)} "Yes"]
         [:btn {:class "btn btn-danger"} "Nope"]]])}))

(defcard 
  "### Auto Resize"
  (dc/reagent 

   (fn [data _]
     (let [{:keys [width height]} (env/get-dimensions)
           pending-path [:pending]]
       [:div
        [:div.container
         [:div.row.col-xs-12
          [:h3 (str "Width: " width ", Height: " height)]]
         [:div.row 
          [:div.col-xs-6.col-sm-10.col-md-8.col-lg-7
           (c/board data [:bingo :board] 
                    (-> (b/calc-board-size)
                        (assoc :read-only false)
                        (assoc :click-fn 
                               (fn [path-to-cell]
                                 (b/save-board! data [:bingo] b/gameid
                                                (get-in @data [:bingo :board])
                                                false)
                                 (b/submit-pending! data 
                                                    pending-path 
                                                    path-to-cell false)))))]
          [:div.col-xs-4.col-sm-2.col-md-3.col-lg-5
           [:h4 "Help people out! Click the buttons to confirm that you
         just heard the same word they did"]
           [:ul
            (for [[k _] (->> (get-in @data pending-path) 
                             (filter (fn [[k {:keys [confirmed]}]] (not confirmed)))
                             (sort (fn [[_ {t1 :ts}] [_ {t2 :ts}]] (< t1 t2))))]
              ^{:key k}[word data pending-path k])]
           ]]
         [:div.row
          [c/leader-boards 
           data [:bingo :players :boards] 
           (-> (into {} (for [[k v] (b/calc-board-size)] [k (/ v 3.1)])))]]]])))

  ;; reagent state
  (r/atom {:bingo 
           {:board (b/make-board (take 25 d/words))
            :players 
            {:boards 
             [{:board (b/make-board (take 25 (shuffle d/words)))}
              {:board (b/make-board (take 25 (shuffle d/words)))}
              {:board (b/make-board (take 25 (shuffle d/words)))}]}}})
  
  {:inspect-data true})

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
  {:inspect-data false})

(defcard 
  "### Active Bingo Board"
  (dc/reagent 
   (fn [data _]
     (c/board data [:bingo :board] {:cell-width  75
                                    :cell-height 75
                                    :font-size   10
                                    :gutter-size 5
                                    :read-only false})))
  (r/atom {:bingo {:board (b/make-board (take 25 d/words))}})
  {:inspect-data false})

(defcard 
  "### Default Bingo Board"
  (dc/reagent 
   (fn [data _]
     (c/board data [:bingo :board])))
  (r/atom {:bingo {:board (b/make-board (take 25 d/words))}})
  {:inspect-data false})

(defcard 
  "### Bingo Boards"
  (dc/reagent 
   (fn [data _]
     (c/leader-boards data [:bingo :players :boards] 
                      {:cell-width  35
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

(defn main []
  ;;(env/watch-env! data)
  )

(defn reload []
  ;;(env/watch-env! data)
  )
