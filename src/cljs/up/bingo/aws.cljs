(ns up.bingo.aws
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require cljsjs.aws-sdk-js
            [up.common    :as c]
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

(defn- val->db 
  [v]
  (cond 
    (string? v)
    {:S v}
    
    (number? v)
    {:N (str v)}
    
    (vector? v)
    {:L (map val->db v)}
    
    (boolean? v)
    {:BOOL v}

    :else v))

(defn clj->db 
  "Convert clj map to format expected by dynamo"
  [m]
  (let [f (fn [[k v]] [k (val->db v)])]
    (:M (w/postwalk (fn [x] 
                      (if (map? x) {:M (into {} (map f x))} x)) m))))

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

(defn <run
  "Inspired by http://www.lispcast.com/core-async-code-style"
  [f & args]
  (let [c (chan)
        cb (fn [err data]
             (if err 
               (put! c {:error (c/o->map err)})
               (if (not (empty-or-nil? data))
                 (put! c (db->clj (keywordize data)))
                 (close! c))))]
    (apply f (concat args [cb]))
    c))

(defn put-item [table-name content cb]
  (js/console.log "ATTEMPTING PUT" (pr-str (clj->db content)))
  (.putItem db 
            (clj->js {:TableName table-name
                      :Item (clj->db content)})
            cb))

(defn get-item [table-name key-map cb]
  ;;(js/console.log "ATTEMPTING GET" (pr-str (pr-str key-map)))
  (.getItem db 
            (clj->js {:TableName table-name
                      :Key (clj->db key-map)})
            cb))

;; (defn m->key-condition-expression 
;;   "Convert a map to key-condition expression"
;;   [m]
;;   (apply str (interpose " AND " (map (fn [[k _]] (str (name k) "=" k)) m))))

(defn m->key-conditions
  "Convert a clj map into a scan-filter data structure"
  [m]
  (into 
   {} 
   (mapcat (fn [[k m2]]
             {k (into 
                 {}
                 (mapcat (fn [[op v]] 
                           {:ComparisonOperator (name op)
                            :AttributeValueList (vector (val->db v))}) m2))}) 
                (into [] m))))

;; don't think I need this anymore
;; (defn keywordstrify-keys
;;   "Like keywordize-keys but it leaves the colon (:) on the keynames"
;;   [m]
;;   (let [f (fn [[k v]] (if (keyword? k) [(str k) v] [k v]))]
;;     (w/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn query' [table-name {:keys [key-cond-map limit 
                                 index-name query-filter] :as opts} cb]
  (let [q (->
           {:TableName table-name
            :KeyConditions key-cond-map
            :QueryFilter query-filter
            ;;:KeyConditionExpression key-condition-expression
            ;;:ExpressionAttributeValues expression-attribute-values
            :ScanIndexForward false}
           (merge (if index-name {:IndexName index-name}))
           (merge (if limit      {:Limit limit})))]
    ;;(js/console.log "ATTEMPTING QUERY" (pr-str q))
    (.query db (clj->js q) cb)))

(defn query 
  "Use nil as index-name to query the primary key. Use nil for limit
  to return all results. 
  Ex: (\"mytable\" {:gameid {:EQ \"test\"}} 1 nil cb)"
  [table-name {:keys [key-cond-map limit index-name query-filter] :as opts} cb]
  (query' table-name 
          (-> opts
              (assoc :key-cond-map (m->key-conditions key-cond-map))
              (assoc :query-filter (m->key-conditions query-filter))) 
          cb))

(defn scan' 
  "scan-filter-map should look like this 
   {:sessionid {:ComparisonOperator \"NE\"
                :AttributeValueList [{:S \"test\"}]}}"
  [table-name key-cond-map cb]
  (let [q 
        {:TableName table-name
         :ScanFilter key-cond-map}]
    (js/console.log "ATTEMPTING SCAN" (pr-str q))
    (.scan db (clj->js q) cb)))

(defn scan 
  "Ex: (\"mytable\" {:sessionid {:NE \"test\"}} 1 cb)"
  [table-name key-cond-map cb]
  (scan' table-name (m->key-conditions key-cond-map) cb))


