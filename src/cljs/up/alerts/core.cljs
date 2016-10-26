(ns up.alerts.core
  (:require
   [reagent.core   :as r]
   [goog.string    :as gstr]))

(defn alert
  [id body & [{:keys [class] 
               :or {class "alert-warning"}
               :as opts}]]
  (when body
    [:div {:id id :class (str "alert " class)} body]))

(defn close [data path]
  (swap! data assoc-in (conj path :open?) false))

(defn open [data path]
  (swap! data assoc-in (conj path :open?) true))

(defn dismissable
  [data path body & [{:keys [class] 
                      :or {class "alert-warning"}
                      :as opts}]]  
  (let [open? (get-in @data (conj path :open?))]
    (when open?
      [:div {:id (last path) 
             :class (str "alert alert-dismissible " class)}
       [:button {:type "button" :class "close"}
        [:span {:on-click 
                #(swap! data assoc-in (conj path :open?) false)} 
         (gstr/unescapeEntities "&times;")]]
       body])))
