(ns up.nutrition.dev
  (:require
   [devcards.core          :as dc]
   [reagent.core           :as r]
   [up.nutrition.core      :as n]
   [up.nutrition.fatsecret :as f]
   [up.event               :as e]
   [goog.net.EventType     :as ge]
   )
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]])
  (:import [goog.net XhrIo CorsXmlHttpFactory])
  )

(def data  (r/atom {}))

;; TODO moved to common
(defn handler [reply]
  (swap! data assoc-in [:result] reply))

;; TODO moved to common
(def xhr (doto (goog.net.XhrIo. (CorsXmlHttpFactory.))
           (.listen ge/COMPLETE handler)))

;; TODO moved to common
(defn send [url]
  (.send xhr url))

(defcard 
  "# Fatsecret"
  (dc/reagent 
   (fn [data _]
     (let [v (get-in @data [:input :value])]
       [:div.form-horizontal
        [:div.form-group
         [:label.control-label.col-sm-3.col-xs-3 "Search"]
         [:div.col-sm-3.col-xs-3
          [:input.form-control 
           {:type "text"
            :value v
            :on-change (e/on-change! data [:input])
            }]]
         [:button.btn.btn-primary 
          {:on-click #(send (f/search n/oauth-consumer-key n/oauth-secret-key
                                      v 50 0))}
          "Search"]]])))
  data
  {:inspect-data true})

(deftest unit-tests
  (testing "Verify against fatsecret rest api documentation"
    (let [example (-> f/default-params
                      (f/add-consumer-key "demo")
                      (f/add-nonce        "abc")
                      (f/add-timestamp    "12345678")
                      (assoc :a "foo")
                      (assoc :z "bar"))]
      (is (=  "a=foo&oauth_consumer_key=demo&oauth_nonce=abc&oauth_signature_method=HMAC-SHA1&oauth_timestamp=12345678&oauth_version=1.0&z=bar"
              (f/params->str example)))

      (is (=  "GET&http%3A%2F%2Fplatform.fatsecret.com%2Frest%2Fserver.api&a%3Dfoo%26oauth_consumer_key%3Ddemo%26oauth_nonce%3Dabc%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D12345678%26oauth_version%3D1.0%26z%3Dbar" 
              (f/signature-base example)))))

  (testing "Verify against java implementation"
    (let [example (-> f/default-params
                      (f/add-format)
                      (f/add-consumer-key  "demo")
                      (f/add-nonce         "11097115122100106108119")
                      (f/add-timestamp     "1478875973")
                      (f/add-search-params "peanut butter" 50 0))]
      
      (is (= "format%3Djson%26max_results%3D50%26method%3Dfoods.search%26oauth_consumer_key%3Ddemo%26oauth_nonce%3D11097115122100106108119%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1478875973%26oauth_version%3D1.0%26page_number%3D0%26search_expression%3Dpeanut%2520butter"
           (f/encode (f/params->str example))))

      (is (= "GET&http%3A%2F%2Fplatform.fatsecret.com%2Frest%2Fserver.api&format%3Djson%26max_results%3D50%26method%3Dfoods.search%26oauth_consumer_key%3Ddemo%26oauth_nonce%3D11097115122100106108119%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1478875973%26oauth_version%3D1.0%26page_number%3D0%26search_expression%3Dpeanut%2520butter"
             (f/signature-base example)))
      
      (is (= "Pv08DYuX%2F7bT1m68NLZ98%2FVPKJg%3D"
             (f/hmac "secret" (f/signature-base example))))

      (is (= "http://platform.fatsecret.com/rest/server.api?format=json&max_results=50&method=foods.search&oauth_consumer_key=demo&oauth_nonce=11097115122100106108119&oauth_signature=Pv08DYuX%2F7bT1m68NLZ98%2FVPKJg%3D&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1478875973&oauth_version=1.0&page_number=0&search_expression=peanut%20butter" 
             (f/make-request (f/add-signature example "secret"))))))
    
  (testing "Other Misc Tests"
    (is (= "" (f/search n/oauth-consumer-key n/oauth-secret-key
                        "peanut butter" 50 0)))))
