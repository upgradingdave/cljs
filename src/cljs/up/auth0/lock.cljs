(ns up.auth0.lock
  (:require [cljsjs.auth0-lock]
            [clojure.walk    :as w]
            [up.cookies.core :as c]))

(def default-domain    "upgradingdave.auth0.com")
(def default-client-id "zxaleUBMxSvDbCCXPjidSCWnljd9ulmF")

(def lock-opts
  {:languageDictionary {:title "Clojure Bingo!"}
   :theme {:logo ""
           :primaryColor "#0f2242"}})

(defn create-client 
  [& [{:keys [domain client-id] 
       :or {domain       default-domain
            client-id    default-client-id}
       :as opts}]]
  (js/Auth0Lock. client-id domain (clj->js lock-opts)))

(defn show [lock]
  (.show lock))

;; Example of profile response
;; {:name "Dave Paroulek", :picture
;; "https://pbs.twimg.com/profile_images/1144679435/dave.paroulek-7266-pp-madmen_fullbody_normal.jpg",
;; :description "Follow Your Bliss.", :lang "en", :location
;; "Fredericksburg, VA", :screen_name "upgradingdave", :time_zone
;; "Central Time (US & Canada)", :url "https://t.co/hLZV3HohMy",
;; :utc_offset -21600, :clientID "zxaleUBMxSvDbCCXPjidSCWnljd9ulmF",
;; :updated_at "2016-11-29T21:59:56.918Z", :user_id
;; "twitter|185300550", :nickname "Dave Paroulek", :identities #js
;; [#js {:provider "twitter", :user_id "185300550", :connection
;; "twitter", :isSocial true}], :created_at
;; "2016-11-29T21:10:47.126Z", :global_client_id
;; "Sr7Qhh1h7VLJYdAOPBMKRtcfHboLomNL"}

(defn get-profile! 
  "Call auth0 to try and get profile information"
  [lock id-token & [cb]]
  (.getProfile 
   lock id-token
   (fn [error profile]
     ;; TODO handle errors
     (when (not error)
       (let [profile (w/keywordize-keys (js->clj profile))
             session {:profile  profile
                      :id-token id-token}]
         (c/set-cookie! "auth0" session)
         (cb session))))))

(defn handle-authentication! 
  "Setup listener for authentication. Optional callback will be passed
  single argument (a map of id-token and profile)"
  [lock & [cb]]
  (.on lock "authenticated"
       (fn [auth-result] 
         (get-profile! lock (.-idToken auth-result) cb))))

(defn get-auth0-session 
  "Attempts to find profile and id-token inside cookie."
  []
  (c/get-cookie "auth0"))

(defn logout []
  (c/remove-cookie! "auth0"))
