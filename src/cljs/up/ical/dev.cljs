(ns up.ical.dev
  (:require
   [devcards.core     :as dc]
   [reagent.core      :as r]
   [up.ical.core      :as ics]
   [goog.date         :as dt]
   [cljsjs.ical])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(def data (r/atom {}))

(def ical-data
  (clojure.string/join 
   "\r\n"
   ["BEGIN:VCALENDAR"
    "CALSCALE:GREGORIAN"
    "PRODID:-//Example Inc.//Example Calendar//EN"
    "VERSION:2.0"
    "BEGIN:VEVENT"
    "DTSTAMP:20080205T191224Z"
    "DTSTART:20081006"
    "SUMMARY:Planning meeting"
    "UID:4088E990AD89CB3DBB484909"
    "END:VEVENT"
    "END:VCALENDAR"
    ]))

(def sample-data [{"dtstamp" {:name "dtstamp" 
                                 :type "date-time" 
                                 :value "2016-04-14T19:32:47Z"}
                      "transp" {:name "transp" 
                                :type "text" 
                                :value "OPAQUE"}
                      "created" {:name "created" 
                                 :type "date-time" 
                                 :value "2013-03-07T20:32:21Z"} 
                      "uid" {:name "uid" 
                             :type "text" 
                             :value "lksgo03up83dlls3trpm0kku8k@google.com"}
                      "summary" {:name "summary" 
                                 :type "text" 
                                 :value "Practice at Thornburg Middle"}
                      "sequence" {:name "sequence" 
                                  :type "integer" 
                                  :value "0"}
                      "location" {:name "location" 
                                  :type "text" 
                                  :value "Thornburg Middle"}
                      "status" {:name "status"
                                :type "text" 
                                :value "CONFIRMED"}
                      "dtend" {:name "dtend" :type "date-time" 
                               :value "2013-03-09T15:00:00Z"}
                      "last-modified" {:name "last-modified"
                                       :type "date-time" 
                                       :value "2013-03-07T20:32:21Z"}
                      "dtstart" {:name "dtstart" 
                                 :type "date-time" 
                                 :value "2013-03-09T14:00:00Z"}
                      "description" {:name "description" 
                                     :type "text" :value ""}}])

(defcard 
  "### CSV Download"
  (dc/reagent 
   (fn [data _] [ics/csv-link data]))
  (r/atom {:results sample-data}))

(defcard 
  "### ICS"
  (dc/reagent 
   (fn [data _] [ics/ical-tool data]))
  data
  {:inspect-data true})

(deftest file-api-supported
  (testing "file api supported"
    (is js/File)
    (is js/FileReader) 
    (is js/FileList) 
    (is js/Blob)
    (is (not (with-redefs [ics/html5-file-api-supported? (fn [] false)]
               (ics/html5-file-api-supported?)))))

  (testing "dates"
    (is (= "4/7/2016 19:30" 
           (ics/format-datetime (dt/fromIsoString "2016-04-07T23:30:00Z"))))
    (is (= "4/7/2016 19:00" 
           (ics/format-datetime (dt/fromIsoString "2016-04-07T23:00:00Z"))))
    (is (= "4/22/2016 19:00" 
           (ics/format-datetime (dt/fromIsoString "2016-04-22T23:00:00Z")))))

  (testing "csv conversion"
    (is (= ["dtstamp"
            "transp"
            "created"
            "uid"
            "summary"
            "sequence"
            "location"
            "status" 
            "dtend" 
            "last-modified" 
            "dtstart" 
            "description"]
           (ics/headings sample-data)))
    (is (= "\"Start\",\"End\",\"Location\",\"Summary\",\"Description\"\n\"3/9/2013 9:00\",\"3/9/2013 10:00\",\"Thornburg Middle\",\"Practice at Thornburg Middle\",\"\""
           (ics/write-csv sample-data))))

  (testing "ICAL"
    (is (= "Planning meeting" 
           (-> (js/ICAL.parse ical-data)
               (js/ICAL.Component.) 
               (.getFirstSubcomponent "vevent")
               (.getFirstPropertyValue "summary")
               ))
        )))

(defcard 
  "### Not Supported"
  (dc/reagent 
   (fn [data _] [ics/ical-tool data {:html5-file-api-supported? false}]))
  data)


