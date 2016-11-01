(ns up.http.core
  (:require [hiccup.core :as h]
            [hiccup.page :as p]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]))

(defn page [& [body]]
  (p/html5 
   [:head 
    [:title "Dave's Clojurescript Experiments"]
    (p/include-css "/css/bootstrap.min.css")]
   [:body 
    body]))

(defn jumbotron [heading & [body]]
  [:div.jumbotron 
   [:h1 heading]
   body])

(defn js-widget [id js-path]
  [:div.col-md-10
   [:div {:id id} ""]
   (p/include-js js-path)])

;; Main Examples

(defroutes default-routes
  (route/resources "/") ; :root defaults to "public"
  (route/not-found "Page not found"))

(defn main-routes []
  (routes 
   (GET "/" [] (js-widget "devcards" "/js/devcards.js"))
   default-routes))

(def handler
  (-> (handler/site (main-routes))))

;; Notifications Examples

(defn notify-page [id js-page]
  (page
   [:div.container
    [:div.row
     [:div.col-10
      [:h3 "Advanced Compiled Examples"]]]
    [:div.row
     [:div.col-md-12
      [:ul.nav.nav-tabs
       [:li {:id "notify-menu"
             :class (if (= id "notify") "active")}
        [:a {:href "/notify"} "Notifications Example"]]
       [:li {:id "notify-dev-menu"
             :class (if (= id "notify_dev") "active")} 
        [:a {:href "/notify-dev"} "Devcards"]]]]]
    [:div.row {:style "margin-top:40px"}
     (js-widget (str id "-widget") js-page)]]))

(defn notify-routes []
  (routes 
   (GET "/" [] (notify-page "notify" "/js/compiled/notify.js"))
   (GET "/notify" [] (notify-page "notify" "/js/compiled/notify.js"))
   (GET "/notify-dev" [] 
        (page (js-widget "notify-dev-widget" "/js/compiled/notify_dev.js")))
   default-routes))

(def notify-handler
  (-> (handler/site (notify-routes))))

