(ns up.bingo.app
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core        :as r]
            [up.bingo.core       :as b]
            [up.bingo.components :as c]
            [up.bingo.css        :as css]
            [cljs.core.async :refer [put! chan <! >! close!]]))

(defn app-container [!state path]
  (let [show-grid? true]
    [:div.container
     [:div.row 
      [:div.col-xs-12.col-sm-12.col-md-12
       {:style (if show-grid? (css/show-grid))}
       (let [{:keys [width height]} (get-in @!state [:env])]
         [:h3 (str "Width: " width ", Height: " height)])]]
     [:div.row 
      [:div.col-xs-12.col-sm-12.col-md-12
       {:style (if show-grid? (css/show-grid))}
       [:h2 "Clojure Bingo!"]]]
     [:div.row {:style (css/show-grid)}
      [:div.col-xs-12.col-sm-12.col-md-7 
       {:style (if show-grid? (css/show-grid))}
       [c/board !state [:bingo :board] 
        {:font-size 15
         :read-only false
         :click-fn #(b/save-board! !state [:bingo] b/gameid
                                   (get-in @!state [:bingo :board])
                                   true)}]]
      [:div.col-xs-12.col-sm-12.col-md-5
       [:h2 "Leader Boards"]]]]))

(def !state (r/atom {}))

(defn main []
  (if-let [node (.getElementById js/document "bingo")]
    (do
      (r/render-component [app-container !state] node)
      (b/init! !state [:bingo] b/gameid))))

(defn reload []
  (b/init! !state [:bingo] b/gameid))
