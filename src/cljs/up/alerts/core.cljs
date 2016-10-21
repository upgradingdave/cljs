(ns up.alerts.core
  (:require
   [goog.string    :as gstr]))

(defn alert
  [data path id & 
   [{:keys [class] 
     :or [class "alert-warning"]
     :as opts} body]]
  [:div {:id id :class (str "alert " class)} body])

(defn dismissable 
  [data path id & [{:keys [class] 
                    :or [class "alert-warning"]
                    :as opts} body]]
  (let [st      (get-in @data (conj path id))
        closed? (get st :closed? false)]
    (when (not closed?)
      [:div {:id id :class (str "alert alert-dismissible " class)}
       [:button {:type "button" :class "close"}
        [:span {:on-click #(swap! data assoc-in (conj path id :closed?) true)} 
         (gstr/unescapeEntities "&times;")]]
       body])))

