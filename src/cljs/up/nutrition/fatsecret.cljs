(ns up.nutrition.fatsecret
  (:require [up.datetime :as t]
            [goog.crypt  :as c])
  (:import [goog.crypt Hmac Sha1]))

(def api-url "http://platform.fatsecret.com/rest/server.api")
(def oauth-signature-method "HMAC-SHA1")
(def oauth-version "1.0")

(def default-params
  {:oauth_signature_method oauth-signature-method
   :oauth_version oauth-version})

(defn encode [s]
  (js/encodeURIComponent s))

(defn hmac [secret message]
  (let [h (Hmac. (Sha1.) (c/stringToByteArray (str secret "&")) 64)]
    (encode (js/btoa (c/byteArrayToString (.getHmac h message))))))

(defn params->str
  "Turn a map of params into string of key=value sorted alphabetically"
  [params]
  (apply str (interpose "&" (sort (map (fn [[k v]] (str (name k) "=" v))
                                       params)))))
(defn signature-base [params]
  (str "GET&" (encode api-url) "&" (encode (params->str params))))

(defn make-request [params] 
  (str api-url "?" (params->str params)))

(defn add-signature
  "This has to be added last"
  [params oauth-secret-key]
  (merge params {:oauth_signature (hmac oauth-secret-key
                                        (signature-base params))}))

(defn add-nonce [params & [nonce]]
  (merge params {:oauth_nonce (or nonce (name (gensym)))}))

(defn add-format [params & [format]]
  (merge params {:format (or format "json")}))

(defn add-timestamp [params & [timestamp]]
  (merge params {:oauth_timestamp (or timestamp (t/now-in-millis))}))

(defn add-consumer-key [params oauth-consumer-key]
  (merge params {:oauth_consumer_key oauth-consumer-key}))

(defn add-base-params [params oauth-consumer-key]
  (-> params
      (add-format)
      (add-consumer-key oauth-consumer-key)
      (add-nonce)
      (add-timestamp)))

(defn add-search-params [params search-term max-results page-number]
  (merge params {:method "foods.search"
                 :max_results max-results
                 :page_number page-number
                 :search_expression (encode search-term)}))

(defn search [oauth-consumer-key
              oauth-consumer-secret
              search-term 
              max-results 
              page]
  (make-request 
   (-> default-params
       (add-base-params oauth-consumer-key)
       (add-search-params search-term max-results page)
       (add-signature oauth-consumer-secret))))
