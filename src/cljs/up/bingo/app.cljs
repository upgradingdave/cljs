(ns up.bingo.app
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core        :as r]
            [up.bingo.core       :as b]
            [up.bingo.components :as c]
            [up.env              :as env]
            [cljs.core.async :refer [put! chan <! >! close!]]))

(defn app-container [!state path]
  (let [{:keys [width height]} (env/get-dimensions)
        vertical? (and (> width height))]
    [:div.container
     [:div.row
      [:div [:h1 "Clojure Bingo!"]]
      ;;[:h3 (str "Width: " width ", Height: " height)]
      ]
     [:div.row 
      [:div {:class (if vertical? "pull-left")}
       (c/board !state [:bingo :board] 
                (-> (b/calc-board-size)
                    (assoc :read-only false)
                    (assoc :click-fn 
                           #(b/save-board! !state [:bingo] b/gameid
                                           (get-in @!state [:bingo :board])
                                           true))))]
      [:div
       {:class (if vertical? "col-xs-2 col-sm-2 col-md-4 col-lg-4")}
       [c/leader-boards 
        !state [:bingo :leaders] 
        (-> (into {} (for [[k v] (b/calc-board-size)] [k (/ v 3.1)]))
            (assoc :vertical? vertical?))]]]]))

(def !state (r/atom {}))

(defn main []
  (if-let [node (.getElementById js/document "bingo")]
    (do
      (r/render-component [app-container !state] node)
      (b/init! !state [:bingo] b/gameid))))

(defn reload []
  (b/init! !state [:bingo] b/gameid))
