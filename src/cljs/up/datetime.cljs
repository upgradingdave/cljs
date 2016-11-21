(ns up.datetime
  (:require [cljs-time.core     :as t]
            [cljs-time.format   :as tf]
            ;;[cljs-time.local    :as tl]
            [cljs-time.coerce   :as tc]
            [goog.date.duration :as duration]))

;; ISO 8601 Time format is preferred way to store dates
;; ex: 2016-11-21T07:39:37-05:00
;;(def built-in-formatter (formatters :basic-date-time))

(def time-format (tf/formatter "h:mm:ss a"))
(def date-time-format (tf/formatter "MM/dd/yyyy h:mm:ss a"))

(defn same-date? [d1 d2]
  (= (tc/to-local-date d1) (tc/to-local-date d2)))

(defn now []
  (t/now))

(defn to-millis [dt]
  (tc/to-long dt))

(defn now-in-millis []
  (to-millis (t/now)))

(defn from-millis [millis]
  (tc/from-long millis))

(defn unparse [fmt dt]
  (if dt
    (tf/unparse fmt dt)))

(defn unparse-local [fmt dt]
  (if dt
    (tf/unparse fmt (t/to-default-time-zone dt))))

(defn unparse-millis
  "Unparse seconds into minutes, seconds, days, hours.
  `goog.date.duration.format` almost does what I needed, but it doesn't
  provide seconds"
  [millis]
  (if (<= millis 0) 
    {:seconds 0}
    (let [days   (js/Math.floor (/ millis duration/DAY_MS_))
          millis (mod millis duration/DAY_MS_)
          hours  (js/Math.floor (/ millis duration/HOUR_MS_))
          millis (mod millis duration/HOUR_MS_)
          mins   (js/Math.floor (/ millis duration/MINUTE_MS_))
          millis (mod millis duration/MINUTE_MS_)
          secs   (js/Math.floor (/ millis 1000))
          millis (mod millis 1000)]
      {:days    days
       :hours   hours
       :minutes mins
       :seconds secs
       :millis  millis})))
