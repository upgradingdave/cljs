(ns up.reframe.core
  (:require    [reagent.core    :as r]
               [re-frame.core :refer [dispatch-sync
                                      dispatch
                                      subscribe
                                      reg-event-db 
                                      reg-sub]]))

(def initial-state {::name "re-frame"})

;; Subscription Handlers

(reg-sub ::name    (fn [db _] (::name db)))
(reg-sub ::name-in (fn [db _] (::name-in db)))

;; Event handlers 

(reg-event-db                 
  :initialize                 
  (fn
    [db _]
    (merge db initial-state)))

(reg-event-db                 
  :input-name
  (fn
    [db [_ val]]
    (assoc-in db [::name-in] val)))

(reg-event-db                 
  :update-name
  (fn
    [db _]
    (assoc-in db [::name] (get-in db [::name-in]))))

;; Components

(defn greet-input []
  (let [v (subscribe [::name-in])]
    (fn []
      [:div {:class "form-group"}
       [:label "Name"]
       [:input 
        {:class "form-control"
         :type "text"
         :value @v
         :on-change #(dispatch [:input-name (.-value (.-target %))])}]])))

(defn greet-btn []
  [:div.btn.btn-primary 
   {:on-click #(dispatch [:update-name])} 
   "Update"])

(defn greet []
  (let [name (subscribe [::name])]
    (fn []
      [:div "Hello, " @name])))

(defn main []
  (dispatch-sync [:initialize]))



