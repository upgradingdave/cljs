(ns up.auth0.core
  (:require [cljsjs.auth0]
            [clojure.walk    :as w]
            [up.cookies.core :as c]))

(def default-domain       "upgradingdave.auth0.com")
(def default-clientid     "zxaleUBMxSvDbCCXPjidSCWnljd9ulmF")
(def default-callback-url js/window.location.href)

(defn create-client [& [{:keys [domain client-id callback-url] 
                         :or {domain       default-domain
                              client-id    default-clientid
                              callback-url default-callback-url}
                         :as opts}]]
  (js/Auth0. (clj->js {:domain       domain
                       :clientID     client-id
                       :callbackURL  callback-url
                       :callbackOnLocationHash true
                       ;;:responseType "token"
                       })))

(defn login-twitter [auth0]
  (.login auth0 (clj->js {:connection "twitter"})))

(defn login-github [auth0]
  (.login auth0 (clj->js {:connection "github"})))

(defn get-profile! [auth0 id-token & [cb]]
  (.getProfile 
   auth0 id-token 
   (fn [error profile]
     ;; TODO handle errors
     (when (not error)
       (let [profile (w/keywordize-keys (js->clj profile))
             session {:profile  profile
                      :id-token id-token}]
         (c/set-cookie! "auth0" session)
         (cb session))))))

(defn handle-authentication! [auth0 & [cb]]
  (let [result   (.parseHash auth0 (.-hash (.-location js/window)))
        error    (when result (.-error result))
        id-token (when result (.-idToken result))]
    ;; TODO handle errors
    (when id-token
      (get-profile! auth0 id-token cb)
      (js/console.log "success!")
      )))

(defn get-auth0-session 
  "Attempts to find profile and id-token inside cookie."
  []
  (c/get-cookie "auth0"))

(defn logout []
  (c/remove-cookie! "auth0"))

