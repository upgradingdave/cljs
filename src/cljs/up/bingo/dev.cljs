(ns up.bingo.dev
  (:require
   [devcards.core  :as dc :include-macros true]
   [reagent.core   :as r]
   [up.bingo.core  :as b]
   [up.bingo.css   :as css]
   [up.bingo.aws   :as aws]
   [up.datetime    :as dt]
   [up.cookies.core :as c]
   [cljs.core.async :refer [put! chan <! >! close!]])
  (:require-macros
   [devcards.core :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]
   [cljs.core.async.macros :refer [go go-loop]]))

(def bingo-path [:bingo])

;; (deftest unit-tests
;;   (testing "Board and Cells"
;;     (is (= 25 (count (b/make-board (take 25 b/possible)))))
;;     (is (= "2px"
;;            (:top (first (b/resize-board 
;;                          (b/make-board (range 25)) 25 25 2))))))

;;   (testing "Session"
;;     (is (c/cookie-exists? :bingo))
;;     (is (= "3f94ee63-35da-4b02-9362-852cbd69679c" 
;;            (:sessionid (c/get-cookie :bingo)))))

;;   (testing "DynamoDB"
;;     (is (=  {:sessionid {:S "foo"}, :score {:N "0"}} 
;;             (aws/clj->db {:sessionid "foo" :score 0})))

;;     (is (= {:sessionid "foo" :score 0}
;;            (aws/db->clj {:sessionid {:S "foo"}, :score {:N "0"}})))

;;     (is (=  {:sessionid "foo",
;;              :score 0,
;;              :board
;;              [{:key "00", :top "15px", :left "15px", :value "transducer"}
;;               {:key "01", :top "15px", :left "140px", :value "map"}
;;               {:key "02", :top "15px", :left "265px", :value "ring"}
;;               {:key "03", :top "15px", :left "390px", :value "list"}
;;               {:key "04", :top "15px", :left "515px", :value "transduce"}]} 
;;             (aws/db->clj
;;              (aws/clj->db 
;;               (merge {:sessionid "foo" :score 0}
;;                      {:board (b/make-board (take 5 b/possible)
;;                                            css/cell-width 
;;                                            css/cell-height 
;;                                            css/gutter-size)})))))))

;; (defcard 
;;   "### Dynamo DB"
;;   (dc/reagent 
;;    (fn [data _]
;;      [:div
;;       [:div.btn.btn-primary 
;;        {:on-click 
;;         #(go 
;;            (let [res (<! (aws/<run 
;;                           aws/put-item 
;;                           {:sessionid "test" 
;;                            :last_updated "20161024T213150.000-04:00"
;;                            :score 0
;;                            :board (b/make-board 
;;                                    (take 5 b/possible)
;;                                    css/cell-width
;;                                    css/cell-height
;;                                    css/gutter-size)}))]
;;              (swap! data assoc :result res)))}
;;        "Put Item"]
;;       [:div.btn.btn-primary 
;;        {:on-click #(go (let [res (<! (aws/<run aws/get-item "test" 
;;                                                "20161024T213150.000-04:00"))] 
;;                          (swap! data assoc :result res)))}
;;        "Get Item"]
;;       [:div.btn.btn-primary 
;;        {:on-click #(go (let [res (<! (aws/<run aws/query {:sessionid "test"}))] 
;;                          (swap! data assoc :result res)))}
;;        "Query Item"]
;;       [:div.btn.btn-primary 
;;        {:on-click #(go (let [res (<! (aws/<run aws/scan))] 
;;                          (swap! data assoc :result res)))}
;;        "Scan"]]))
;;   (r/atom {})
;;   {:inspect-data true})

(defn main []
  (b/init! b/!state bingo-path))


