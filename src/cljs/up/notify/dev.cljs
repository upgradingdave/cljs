(ns up.notify.dev
  (:require
   [devcards.core  :as dc :include-macros true]
   [reagent.core   :as r]
   [up.notify.core :as n]
   [up.alerts.core :as a])
  (:require-macros
   [devcards.core :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(def alerts-path [:alerts])
(def notify-path [:notifications])
(def ex1 (r/atom {}))

(defn on-text-change [data path]
  #(swap! data assoc-in path
          (.-value (.-target %))))

(defcard 
  "### Notification Example"
  (dc/reagent 
   (fn [data _]
     (let [st (get @data :notify-form)
           {:keys [body title] 
            :or {body "Notification from Clojurescript!"
                 title ""}} st]
       [:form
        [:div {:class "form-group"}
         [:label {:for "notify-title"} "Title of Notification?"]
         [:input {:id "notify-title" 
                  :class "form-control"
                  :type "text"
                  :value title
                  :on-change (on-text-change data [:notify-form :title])}]]
        [:div {:class "form-group"}
         [:label {:for "notify-body"} "What Message to Display?"]
         [:input {:id "notify-body" 
                  :class "form-control"
                  :type "text"
                  :value body
                  :on-change (on-text-change data [:notify-form :body])}]]
        [:div [:button {:class "btn btn-primary"
                        :on-click #(n/create-notification 
                                    (n/notification-enabled? 
                                     data notify-path)
                                    {:title title
                                     :body body})} 
               "Notify"]]])))
  ex1
  {:inspect-data true})

(defcard 
  "### Toggle Notifications"
  (dc/reagent (fn [data _]
                [:div {:id "notification-toggle"}
                 (if (n/notification-permitted?)

                   ;; Notifications Permitted
                   (if (n/notification-enabled? data notify-path)

                     ;; Notifications enabled
                     [:div {:class "btn-group"}
                      [:button {:class "btn btn-success"
                                :on-click #(n/enable! data notify-path)}
                       "Enabled"]
                      [:button {:class "btn"
                                :on-click #(n/disable! data notify-path)}
                       "Disable"]]

                     ;; Notifications disabled
                     [:div {:class "btn-group"}
                      [:button {:class "btn"
                                :on-click #(n/enable! data notify-path)}
                       "Enable"]
                      [:button {:class "btn btn-danger"
                                :on-click #(n/disable! data notify-path)}
                       "Disabled"]])

                   ;; Notifications Blocked by Browser
                   [a/dismissable data alerts-path "notify-alert" 
                    {:class "alert-warning"} 
                    [:div (str "Notifications are currently blocked, "
                               "please update your Browser settings to "
                               "allow them.") ]])]))
  ex1
  {:inspect-data true})

(deftest unit-tests
  (testing "Notifications"
    (is (n/notification-permitted?))))

(defn init! [data]
  (js/console.log "Initializing notifications ...")
  (n/notification-init! data notify-path))

(defn main []
  (init! ex1)
  (dc/start-devcard-ui!))
