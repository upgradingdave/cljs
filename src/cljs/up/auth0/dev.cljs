(ns up.auth0.dev
  (:require
   [devcards.core   :as dc :include-macros true]
   [reagent.core    :as r]
   [up.auth0.lock   :as l]
   [up.auth0.core   :as c])
  (:require-macros
   [devcards.core :refer [defcard deftest defcard-doc]]
   [cljs.test     :refer [is testing]]))

(def data (r/atom {}))

(deftest unit-tests
  (testing "Auth0"
    (is (= false false))))

;; (defcard 
;;   "## Lock API"
;;   (dc/reagent 
;;    (fn [data _]
;;      [:div.form-horizontal
;;       (if-let [profile (get-in @data [:user :profile])]
;;         [:div.form-group
;;          [:div 
;;           [:img.img-thumbnail {:src (:picture profile)}]
;;           [:div (str "@" (:screen_name profile))]]
;;          [:button.btn.btn-primary 
;;           {:on-click (fn [_] (l/logout) (swap! data assoc-in [:user] nil))}
;;           "Logout"]]
;;         [:div.form-group
;;          [:button.btn.btn-primary 
;;           {:on-click #(l/show (get-in @data [:auth0-lock]))}
;;           "Auth0"]])]))
;;   data
;;   {:inspect-data false})

(defcard 
  "## Auth0.js"
  (dc/reagent 
   (fn [data _]
     [:div.form-horizontal
      (if-let [profile (get-in @data [:user :profile])]
        [:div.form-group
         [:div 
          [:img.img-thumbnail {:src (:picture profile)}]
          [:div (str "@" (:screen_name profile))]]
         [:button.btn.btn-primary 
          {:on-click (fn [_] (l/logout) (swap! data assoc-in [:user] nil))}
          "Logout"]]
        [:div.form-group
         [:button.btn.btn-primary 
          {:on-click #(c/login-twitter (get-in @data [:auth0]))}
          "Twitter"]])]))
  data
  {:inspect-data true})

(defn init-lock! [data]
  (let [lock  (l/create-client) ]
    (swap! data assoc-in [:auth0-lock] lock)
    (l/handle-authentication! lock #(swap! data assoc-in [:user] %))))

(defn init-auth0! [data]
  (let [auth0 (c/create-client)]
    (swap! data assoc-in [:auth0] auth0)
    (c/handle-authentication! auth0 #(swap! data assoc-in [:user] %))))

(defn main []
  (if-let [session (l/get-auth0-session)]
    ;;If we're authenticated
    (swap! data assoc-in [:user] session)
    ;; Otherwise ...
    (init-auth0! data)
    ;;(init-lock! data)
))
