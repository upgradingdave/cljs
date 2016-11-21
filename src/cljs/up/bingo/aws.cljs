(ns up.bingo.aws
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require cljsjs.aws-sdk-js
            [clojure.walk :as w]
            [cljs.core.async :refer [put! chan <! >! close!]]))

(def access-key "AKIAJQHQL4AXM5YR3WBA")
(def secret-key "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")
(def region "us-east-1")

(aset js/AWS "config"
      (js/AWS.Config. 
       #js{:accessKeyId     access-key
           :secretAccessKey secret-key
           :region          region}))

(def db (js/AWS.DynamoDB.))

(defn keywordize [data] (w/keywordize-keys (js->clj data)))

(defn clj->db 
  "Convert clj map to format expected by dynamo"
  [m]
  (let [f (fn [[k v]] 
            (cond 
              (string? v)
              [k {:S v}]

              (number? v)
              [k {:N (str v)}]

              (vector? v)
              {k {:L v}}

              (boolean? v)
              {k {:BOOL v}}

              :else 
              [k v]))]
    (:M (w/postwalk (fn [x] (if (map? x) {:M (into {} (map f x))} x)) m))))

(defn db->clj
  "Convert dynamo result into clj map"
  [m]
  (let [f (fn [[k v]] 
            (case k
              :N (js/parseInt v)
              :S v
              :BOOL v
              :M v
              :L v
              [k v]))]
    (w/postwalk (fn [x] (if (map? x) 
                          (let [r (map f x)]
                            (if (= (count r) 1)
                              (first r)
                              (into {} r))) x)) m)))

(defn empty-or-nil? [data]
  (or (nil? data)
      (undefined? data)
      (empty? (js->clj data))))

(defn error->map
  [x]
  (into {} (for [k (.keys js/Object x)] [k (aget x k)])))

(defn <run
  "Inspired by http://www.lispcast.com/core-async-code-style"
  [f & args]
  (let [c (chan)
        cb (fn [err data]
             (js/console.log "handling response")
             (if err 
               (put! c {:error (error->map err)})
               (if (not (empty-or-nil? data))
                 (put! c (db->clj (keywordize data)))
                 (close! c))))]
    (apply f (concat args [cb]))
    c))

(defn put-item [content cb]
  ;;(js/console.log "ATTEMPTING PUT" (pr-str (clj->db content)))
  (.putItem db 
            (clj->js {:TableName "bingo.cards"
                      :Item (clj->db content)})
            cb))

(defn get-item [sessionid cb]
  ;;(js/console.log "ATTEMPTING GET" (pr-str sessionid))
  (.getItem db 
            (clj->js {:TableName "bingo.cards"
                      :Key {:sessionid {:S sessionid}}}) 
            cb))



