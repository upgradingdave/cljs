(ns up.timers.dev
  (:require
   [cljs-time.core       :as t]
   [cljs-time.format     :as tf]
   [devcards.core        :as dc]
   [reagent.core         :as r]
   [up.timers.core       :as timer]
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
                   [:div (:seconds (timer/unparse-millis 
                                    (- (get-in @data [:timer :millis])
                                       (t/in-millis n))))])]))
  data
  {:inspect-data true})

(defcard 
  "### Clock"
  (dc/reagent (fn [data _]
                (let [n (get-in @data [:timer :now])]
                  [:div (timer/unparse-local timer/time-format n)])))
  data
  {:inspect-data true})

(deftest unit-tests
  (testing "Time and Dates"
    (let [n (t/date-time 2016 10 25 1 31 50)]
      (is (= "1:31:50 am" (tf/unparse timer/time-format n)))
      (is (= "9:31:50 pm" (tf/unparse timer/time-format 
                                      (t/to-default-time-zone n))))
      (is (= "9:31:50 pm" (timer/unparse-local timer/time-format n)))
      (is (= 1477359110000 (timer/to-millis n)))
      ;;TODO: Why doesn't this work?
      (is (timer/same-date? n (timer/from-millis (timer/to-millis n))))
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

    (is (= [0]            (vals (timer/unparse-millis 0))))
    (is (= [0 0 0 1 0]    (vals (timer/unparse-millis 1000))))
    (is (= [0 0 0 3 0]    (vals (timer/unparse-millis 3000))))
    (is (= [0 0 5 54 600] (vals (timer/unparse-millis 354600))))))

(defn main []
  (timer/init-timer! data timer-path))
