(ns up.cookies.core
  (:require [cljs.reader :as reader])
  (:import [goog.net Cookies]))

(def cookies (Cookies. js/document))

(defn cookie-exists? [cookie-name]
  (.containsKey cookies (name cookie-name)))

(defn set-cookie! [cookie-name value]
  (.set cookies (name cookie-name) (pr-str value) 
        86400 "/"))

(defn remove-cookie! [cookie-name]
  (.remove cookies cookie-name "/"))

(defn get-cookie [cookie-name]
  (when-let [v (.get cookies (name cookie-name))]
    (reader/read-string v)))

(defn main [])

(defn reload [])
