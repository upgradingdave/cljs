(ns up.bingo.dev.aws
  (:require
   [devcards.core       :as dc :include-macros true]
   [reagent.core        :as r]
   [up.bingo.core       :as b]
   [up.bingo.aws        :as aws]
   [up.bingo.dev.data   :as d]
   [up.datetime         :as dt]
   [cljs.core.async :refer [put! chan <! >! close!]]
   )
  (:require-macros
   [devcards.core :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]
   [cljs.core.async.macros :refer [go go-loop]]))

(defn put-button [data table-name content]
      [:div.btn.btn-primary 
       {:on-click 
        #(go 
           (let [res (<! (aws/<run aws/put-item table-name content))]
             (swap! data assoc :result res)))}
       "Put Item"])

(defn get-button [data table-name content]
  [:div.btn.btn-primary 
   {:on-click 
    #(go (let [res (<! (aws/<run aws/get-item table-name content))] 
           (swap! data assoc :result res)))}
   "Get Item"])

(defn query-button [data table-name key-cond-map]
  [:div.btn.btn-primary 
   {:on-click #(go (let [res (<! (aws/<run aws/query table-name key-cond-map))] 
                     (swap! data assoc :result res)))}
   "Query Item"])

(defn scan-button [data table-name filter]
  [:div.btn.btn-primary 
   {:on-click #(go (let [res (<! (aws/<run aws/scan table-name filter))] 
                     (swap! data assoc :result res)))}
   "Scan"])

(defcard 
  "### Persist Boards"
  (dc/reagent 
   (fn [data _]
     [:div
      [put-button 
       data "bingo.cards" {:sessionid "test" 
                           :last_updated "20161024T213150.000-04:00"
                           :score 0
                           :board (b/make-board (take 5 d/words))}]

      [get-button 
       data "bingo.cards" {:sessionid "test" 
                           :last_updated "20161024T213150.000-04:00"}]

      [query-button 
       data "bingo.cards" {:sessionid "test"}]

      [scan-button 
       data "bingo.cards" {:sessionid {:NE "test"}}]
      ]))
  (r/atom {})
  {:inspect-data true})

(defcard 
  "### Persist App Config"
  (dc/reagent 
   (fn [data _]
     [:div
      [put-button 
       data "app.config" {:app_name "test" 
                          :last_updated "20161024T213150.000-04:00"
                          :words ["one" "two" "three"]}]
      [get-button 
       data "app.config" {:app_name "test" 
                          :last_updated "20161024T213150.000-04:00"}]]))
  (r/atom {})
  {:inspect-data true})

(defcard 
  "### Add Term"
  (dc/reagent 
   (fn [data _]
     [:div.form-horizontal

      [:ul.list-group
       (for [word (sort (:words @data))]
         [:li.list-group-item {:key word} word])]

      [:div.form-group
       [:label.control-label.col-sm-3.col-xs-3 "Add Word"]
       [:div.col-sm-3.col-xs-3
        [:input.form-control
         {:type "text"
          :value (get-in @data [:new-word])
          :on-change
          #(let [v (.-value (.-target %))]
             (swap! data assoc-in [:new-word] v))}]]]

      [:div.form-group
       [:div.col-sm-offset-3.col-sm-10
        [:div.btn-group

      [:div.btn.btn-primary 
       {:on-click 
        #(let [new-word (:new-word @data)
               words    (:words @data)] 
           (swap! data assoc :words (conj words new-word))
           (go 
             (let [res 
                   (<! (aws/<run 
                        aws/put-item 
                        "app.config" 
                        {:app_name "bingo" 
                         :last_updated (dt/unparse dt/iso-8601-format (dt/now))
                         :words (into [] (:words @data))}))]
               (swap! data assoc :result res))))}
       "Add and Save"]

         [:div.btn.btn-primary 
          {:on-click 
           #(b/load-words data [])}
          "Fetch Items"]]]]
      ]))
  (r/atom {:words #{}})
  {:inspect-data true})

(deftest unit-tests
  (testing "DynamoDB"
    (is (=  {:sessionid {:S "foo"}, :score {:N "0"}} 
            (aws/clj->db {:sessionid "foo" :score 0})))

    (is (= {:sessionid "foo" :score 0}
           (aws/db->clj {:sessionid {:S "foo"}, :score {:N "0"}})))

    (is (=  {:sessionid "foo",
             :score 0,
             :board
             [{:key "00", :top 0, :left 0, :value "transducer"}
              {:key "01", :top 0, :left 1, :value "map"}
              {:key "02", :top 0, :left 2, :value "ring"}
              {:key "03", :top 0, :left 3, :value "list"}
              {:key "04", :top 0, :left 4, :value "transduce"}]} 
            (aws/db->clj
             (aws/clj->db 
              (merge {:sessionid "foo" :score 0}
                     {:board (b/make-board (take 5 d/words))})))))

    (is (= {:words {:L [{:S "one"} {:S "two"}]}}
           (aws/clj->db {:words ["one" "two"]})))

    (is (=    {:sessionid {:ComparisonOperator "NE"
                           :AttributeValueList [{:S "test"}]}}
              (aws/m->scan-filter {:sessionid {:NE "test"}})))
    ))
