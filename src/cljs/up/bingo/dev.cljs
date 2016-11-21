(ns up.bingo.dev
  (:require
   [devcards.core  :as dc :include-macros true]
   [reagent.core   :as r]
   [up.bingo.core  :as b]
   [up.bingo.css   :as css]
   [up.bingo.aws   :as aws]
   [up.cookies.core :as c]
   [cljs.core.async :refer [put! chan <! >! close!]])
  (:require-macros
   [devcards.core :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]
   [cljs.core.async.macros :refer [go go-loop]]))

(def bingo-path [:bingo])

(deftest unit-tests
  (testing "Board and Cells"
    (is (= 25 (count (b/make-board (take 25 b/possible)))))
    ;;(is (= "00" (:key (get-in @!state [:bingo :board 0]))))
    )

  (testing "Session"
    (is (c/cookie-exists? :bingo))
    (is (= "a83edadf-5e8e-405f-ae54-257dc5689bd0" 
           (:sessionid (c/get-cookie :bingo)))))

  (testing "DynamoDB"
    (is (=  {:sessionid {:S "foo"}, :score {:N "0"}} 
            (aws/clj->db {:sessionid "foo" :score 0})))

    (is (= {:sessionid "foo" :score 0}
           (aws/db->clj {:sessionid {:S "foo"}, :score {:N "0"}})))

    (is (=  {:sessionid "foo",
             :score 0,
             :board
             [{:key "00", :top "15px", :left "15px", :value "transducer"}
              {:key "01", :top "15px", :left "140px", :value "map"}
              {:key "02", :top "15px", :left "265px", :value "ring"}
              {:key "03", :top "15px", :left "390px", :value "list"}
              {:key "04", :top "15px", :left "515px", :value "transduce"}]} 
            (aws/db->clj
             (aws/clj->db 
              (merge {:sessionid "foo" :score 0}
                     {:board (b/make-board (take 5 b/possible))})))))))

(defcard 
  "### Dynamo DB"
  (dc/reagent 
   (fn [data _]
     [:div
      [:div.btn.btn-primary 
       {:on-click 
        #(go 
           (let [res (<! (aws/<run aws/put-item 
                                   {:sessionid "test" 
                                    :score 0
                                    :board (b/make-board 
                                            (take 5 b/possible))}))]
             (swap! data assoc :result res)))}
       "Put Item"]
      [:div.btn.btn-primary 
       {:on-click #(go (let [res (<! (aws/<run aws/get-item "test"))] 
                         (swap! data assoc :result res)))}
       "Get Item"]]))
  (r/atom {})
  {:inspect-data true})

(defn color-swatch [color-name hex]
  [:div {:key color-name
         :style {:width "100px"
                 :background-color hex}} 
   (name color-name)])

(defcard 
  "### Color Palette"
  (dc/reagent 
   (fn [data _]
     [:div
      (map (fn [[color hex]] (color-swatch color hex)) @data)]))
  (r/atom css/colors) 
  {:inspect-data false})

(defcard 
  "### Bingo Cell"
  (dc/reagent 
   (fn [data _]
     [b/cell data [:cell]]))
  (r/atom {:cell {:key "00" :value "Example"}})
  {:inspect-data false})

(defcard 
  "### Bingo Board"
  (dc/reagent 
   (fn [data _]
     (b/board data bingo-path)))
  b/!state
  {:inspect-data true})

(defn main []
  (b/init! b/!state bingo-path))


