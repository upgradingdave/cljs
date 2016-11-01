(ns up.notify.core
  (:require [up.alerts.core :as a]
            [up.event       :as e]))

(defn notification-supported? []
  js/Notification)

(defn notification-permission []
  (.-permission js/Notification))

(defn notification-permitted? []
  (= "granted" (notification-permission)))

(defn notification-request-permission! [data path]
  (.requestPermission 
   js/Notification 
   (fn [permission]
     (swap! data assoc-in path {:enabled true
                                :permitted (= "granted" permission)
                                :permission permission}))))

(defn notification-enabled? [data path]
  (get-in @data (conj path :enabled)))

(defn disable! [data path]
  (swap! data assoc-in (conj path :enabled) false))

(defn enable! [data path]
  (swap! data assoc-in (conj path :enabled) true))

(defn create-notification 
  [enabled? & [{:keys [title body icon sound]
                :or {title ""
                     body  ""
                     icon "/i/clojure-logo.png"}}]]
  (when (and (notification-permitted?) enabled?)
    (let [n (js/Notification. 
             title 
             (clj->js {:body body
                       :icon icon}))]
      (when sound
        (.play (js/Audio. sound)))
      ;;(js/setTimeout #(.close n), 5000)
      n)))

(defn notification-init! 
  [data path]
  (if (notification-permitted?)
    (let [permission (notification-permission)]
      (swap! data assoc-in path 
             {:enabled    (get-in @data (conj path :enabled) true)
              :permitted  (= "granted" permission)
              :permission permission}))
    (notification-request-permission! data path)))

;;;; widgets

(defn example [data notify-path path]
  (let [title-path (conj path :title)
        title (get-in @data title-path "Heads up!")
        body-path (conj path :body)
        body  (get-in @data body-path 
                      "I have something very interesting to say")
        enabled? (and (notification-permitted?)
                      (notification-enabled? data notify-path))]
    [:form
     (when (not enabled?)
       [a/alert "a1" 
        [:div [:strong "Doh"] " Notifications are disabled."]
        {:class "alert-warning"}])
     [:div {:class "form-group"}
      [:label {:for "notify-title"} "Title of Notification?"]
      [:input {:id "notify-title" 
               :class "form-control"
               :type "text"
               :value title
               :on-change (e/on-change! data title-path)
               :disabled (not enabled?)}]]
     [:div {:class "form-group"}
      [:label {:for "notify-body"} "What Message to Display?"]
      [:input {:id "notify-body" 
               :class "form-control"
               :type "text"
               :value body
               :on-change (e/on-change! data body-path)
               :disabled (not enabled?)}]]
     [:div [:button {:class "btn btn-primary"
                     :on-click #(do 
                                  (e/prevent-default %)
                                  (create-notification 
                                   enabled?
                                   {:title title
                                    :body body}))
                     :disabled (not enabled?)} 
            "Notify"]]]))

(defn toggle-notifications [data path]
  [:div {:id "notification-toggle"}
   (if (notification-permitted?)

     ;; Notifications Permitted
     (if (notification-enabled? data path)

       ;; Notifications enabled
       [:div {:class "btn-group"}
        [:button {:class "btn btn-success"
                  :on-click #(enable! data path)}
         "Enabled"]
        [:button {:class "btn"
                  :on-click #(disable! data path)}
         "Disable"]]

       ;; Notifications disabled
       [:div {:class "btn-group"}
        [:button {:class "btn"
                  :on-click #(enable! data path)}
         "Enable"]
        [:button {:class "btn btn-danger"
                  :on-click #(disable! data path)}
         "Disabled"]])

     ;; Notifications Blocked by Browser
     [a/alert "notify-alert" 
      [:div (str "Notifications are currently blocked, "
                 "please update your Browser settings to "
                 "allow them.") ]
      {:class "alert-warning"}])])

(defn full-example [data notify-path]
  [:div
   [toggle-notifications data notify-path]
   [example data notify-path [:notify-form]]])

