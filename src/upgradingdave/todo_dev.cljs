(ns upgradingdave.todo-dev
  (:require
   [cljs-time.core       :as t]
   [cljs-time.format     :as tf]
   [devcards.core        :as dc]
   [reagent.core         :as r]
   [upgradingdave.todo   :as todo]

   [goog.date.duration :as duration])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(defcard 
  "### Notifications"
  (dc/reagent (fn [data _]
                [todo/toggle-notifications data todo/notify-path]))
  todo/data
  {:inspect-data false})

(defcard 
  "### Timer"
  (dc/reagent (fn [data _]
                [todo/timer data todo/timer-path]))
  todo/data
  {:inspect-data true})

(defcard-doc 
  "### Timer

  The timer just counts down from seconds found in state atom
"
  (dc/mkdn-pprint-source todo/timer))

(deftest unit-tests
  (testing "Time and Dates"
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

    (is (= [0]            (vals (todo/unparse-millis 0))))
    (is (= [0 0 0 1 0]    (vals (todo/unparse-millis 1000))))
    (is (= [0 0 0 3 0]    (vals (todo/unparse-millis 3000))))
    (is (= [0 0 5 54 600] (vals (todo/unparse-millis 354600)))))

  (testing "Notifications"
    (is (todo/notification-supported?))
    (is (todo/notification-permitted?))
    (is (= "granted" (todo/notification-permission))))

  (testing "Web Workers"
    (is (not (todo/webworker?)))))

(devcards.core/start-devcard-ui!)
(if (not (todo/notification-permitted?))
  (todo/notification-request-permission! todo/data [:notifications]))
