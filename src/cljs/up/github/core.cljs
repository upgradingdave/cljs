(ns up.github.core
  (:require 
   [clojure.string :refer [split]]
   [up.common     :refer [<send]]
   [reagent.core  :as r]
   [re-frame.core :refer [dispatch-sync
                          dispatch
                          subscribe
                          reg-event-db 
                          reg-sub]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

;; functions

(def endpoint "https://api.github.com/repos/")

(defn github-contributors 
  "The response contains list of contributors. Inside each contributor
  is list of weekly statistics. Inside each week is lines added and
  lines deleted. This tallies lines added minus lines deleted.
  Inspired by
  http://stackoverflow.com/questions/26881441/can-you-get-the-number-of-lines-of-code-from-a-github-repository"
  [user repo]
  (let [url (str endpoint user "/" repo "/stats/contributors")]
    (go (let [contribs (<! (<send url))
              total    (reduce (fn [acc {weeks "weeks"}]
                                 (+ acc
                                    (reduce (fn [acc {a "a" d "d"}] 
                                              (+ acc (- a d))) 0 weeks)))
                               0 contribs)
              ]
          (dispatch [::line-count total])))))

(defn format-total 
  "Put comma's in number to make it more readable. I wrote this very
  quickly and sure there's a better way"
  [total]
  (let [reversed    (reverse (str total))
        partitioned (partition 3 3 "" reversed)]
    (apply str (reverse (flatten (interpose "," partitioned))))))

;; subscription handlers

(reg-sub ::repo-input (fn [db _] (::repo-input db)))
(reg-sub ::user-input (fn [db _] (::user-input db)))
(reg-sub ::waiting    (fn [db _] (::waiting db)))
(reg-sub ::line-count (fn [db _] (::line-count db)))

;; event handlers

(reg-event-db                 
 ::initialize                 
 (fn
   [db _]
   (merge db {::repo-input "jquery"
              ::user-input "jquery"
              ::line-count ""
              ::waiting    false})))

(reg-event-db
 ::repo-input
 (fn
   [db [_ val]]
   (assoc-in db [::repo-input] val)))

(reg-event-db
 ::user-input
 (fn
   [db [_ val]]
   (assoc-in db [::user-input] val)))

(reg-event-db
 ::find-contributors
 (fn
   [db [_ user repo]]
   (let [user (get-in db [::user-input])
         repo (get-in db [::repo-input])]
     (github-contributors user repo)
     (-> db
      (assoc-in [::waiting] true)
      (assoc-in [::line-count] "")))))

(reg-event-db
 ::line-count
 (fn
   [db [_ val]]
   (-> db
       (assoc-in [::waiting]    false)
       (assoc-in [::line-count] val))))

;; components

(defn waiting []
  (let [v (subscribe [::waiting])]
    (fn []
      (when @v
        [:span
         [:i
          {:class "fa fa-hourglass animated infinite swing"}]]))))

(defn user-input []
  (let [v (subscribe [::user-input])
        waiting (subscribe [::waiting])]
    (fn []
      [:div.form-group
       [:label "Github User or Org Name"]
       [:input 
        {:class "form-control"
         :style {:width "175px"}
         :type "text"
         :value @v
         :disabled @waiting
         :on-change #(dispatch [::user-input (.-value (.-target %))])}]])))

(defn repo-input []
  (let [v (subscribe [::repo-input])
        waiting (subscribe [::waiting])]
    (fn []
      [:div.form-group
       [:label "Github Repo"]
       [:input 
        {:class "form-control"
         :style {:width "175px"}
         :type "text"
         :value @v
         :disabled @waiting
         :on-change #(dispatch [::repo-input (.-value (.-target %))])}]])))

(defn contrib-btn []
  (let [waiting (subscribe [::waiting])]
    (fn []
      [:div.btn.btn-primary 
       {:on-click (fn [e] 
                    (do
                      (dispatch [::find-contributors])
                      (.preventDefault e)))
        :disabled @waiting}
       "Find Contributors"])))

(defn line-count []
  (let [v (subscribe [::line-count])]
    (fn []
      [:div
       [:h3 "Total LOC: " [:span.text-success (format-total @v)] [waiting]]])))

(defn widget []
  [:div
   [user-input]
   [repo-input]
   [contrib-btn]
   [line-count]])

(defn main []
  (dispatch-sync [::initialize])
;;  (dispatch [::find-contributors])
  )

(defn dom 
  "This is what bootstraps in advanced compilation"
  []
  (if-let [node (.getElementById js/document "github")]
    (do
      (main)
      (r/render-component [widget] node)
      (dispatch [::find-contributors]))))
