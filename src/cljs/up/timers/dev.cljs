(ns up.timers.dev
  (:require
   [cljs-time.core     :as t]
   [cljs-time.format   :as tf]
   [devcards.core      :as dc]
   [reagent.core       :as r]
   [up.datetime        :as dt]
   [up.timers.core     :as timer]
   [up.common          :refer [o->map]]
   [goog.date.duration :as duration])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(def timer-path [:timer])
(def data (r/atom {}))

(defcard 
  "### Countdown"
  (dc/reagent (fn [data _]
                [:div {:class "form-group"}
                 [:div {:class "btn-group"}
                  [:div {:class "btn btn-primary"
                         :on-click #(timer/start-timer! data timer-path)
                         } "Start"]]
                 (if-let [n (get-in @data [:timer :elapsed])]
                   [:div (:seconds (dt/unparse-millis 
                                    (- (get-in @data [:timer :millis])
                                       (t/in-millis n))))])]))
  data
  {:inspect-data true})

(defcard 
  "### Clock"
  (dc/reagent (fn [data _]
                (let [n (get-in @data [:timer :now])]
                  [:div (dt/unparse-local dt/time-format n)])))
  data
  {:inspect-data true})

(deftest unit-tests
  (testing "Time and Dates"
    (let [n (t/date-time 2016 10 25 1 31 50)]
      (is (= "20161024T213150.000-04:00" 
             (dt/unparse-local dt/iso-8601-format n)))
      (is (= "20161025T013150.000Z" 
             (dt/unparse dt/iso-8601-format n)))
      (is (not (dt/same-date? n (dt/now))))
      (is (dt/same-date?
           n
           (dt/parse dt/iso-8601-format
                     (dt/unparse-local dt/iso-8601-format n))))
      (is (= "1:31:50 am" (tf/unparse dt/time-format n)))
      (is (= "9:31:50 pm" (tf/unparse dt/time-format 
                                      (t/to-default-time-zone n))))
      (is (= "9:31:50 pm" (dt/unparse-local dt/time-format n)))
      (is (= 1477359110000 (dt/to-millis n)))
      (is (dt/same-date? n (dt/from-millis (dt/to-millis n))))
      )
    (is (t/date? (t/now)))
    (is (t/date? (t/date-time 2016 8 10)))
    (is (= 0  (t/second (t/date-time 2016 8 10))))
    (is (= 20 (t/in-seconds (t/interval (t/date-time 2016 8 10 10 55 20)
                                        (t/date-time 2016 8 10 10 55 40)))))
    (is (t/period? (t/seconds 
                    (t/in-seconds 
                     (t/interval (t/date-time 2016 8 10 10 55 20)
                                 (t/date-time 2016 8 10 10 55 40))))))
    (is (= "0 minutes" (tf/unparse-duration
                        (t/interval (t/date-time 2016 8 10 10 55 20)
                                    (t/date-time 2016 8 10 10 55 40)))))
    (is (= 20000 (t/in-millis
                  (t/interval (t/date-time 2016 8 10 10 55 20)
                              (t/date-time 2016 8 10 10 55 40)))))
    (is (= 86400000 duration/DAY_MS_))

    (is (= [0]            (vals (dt/unparse-millis 0))))
    (is (= [0 0 0 1 0]    (vals (dt/unparse-millis 1000))))
    (is (= [0 0 0 3 0]    (vals (dt/unparse-millis 3000))))
    (is (= [0 0 5 54 600] (vals (dt/unparse-millis 354600))))))

(defn main []
  (timer/init-timer! data timer-path))
