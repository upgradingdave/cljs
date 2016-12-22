(ns up.bingo.app
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core        :as r]
            [up.bingo.core       :as b]
            [up.bingo.components :as c]
            [up.env              :as env]
            [cljs.core.async :refer [put! chan <! >! close!]]
            [re-frisk.core :refer [enable-re-frisk!]]))

(def app-db (r/atom {}))

(defn app-container [!state path]
  (let [{:keys [width height]} (env/get-dimensions)]
    [:div.container
     [:div.row
      [:div [:h1 "Clojure Bingo!"]]
      ;;[:h3 (str "Width: " width ", Height: " height)]
      ]
     [:div.row 
      [:div 
       [c/board !state [:bingo :board] 
        (-> (b/calc-board-size)
            (assoc :read-only false)
            (assoc :click-fn 
                   #(b/save-board! !state [:bingo] b/gameid
                                   (get-in @!state [:bingo :board])
                                   true)))]]]
     [:div.row
      [c/leader-boards 
       !state [:bingo :leaders] 
       (-> (into {} (for [[k v] (b/calc-board-size)] [k (/ v 3.1)])))]]]))

(def !state (r/atom {}))

(defn main []
  (if-let [node (.getElementById js/document "bingo")]
    (do
      
      (r/render-component [app-container !state] node)
      (enable-re-frisk!)
      ;;(b/init! !state [:bingo] b/gameid)
      )))

(defn reload []
  (b/init! !state [:bingo] b/gameid))
