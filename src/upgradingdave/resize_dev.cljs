(ns upgradingdave.resize_dev
  (:require
   [devcards.core        :as dc]
   [reagent.core         :as r]
   [goog.date            :as dt]
   [upgradingdave.resize :as resize]
   [upgradingdave.html5  :as html5]
   )
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(def data (r/atom {}))

(defcard 
  "### Photo Editor"
  (dc/reagent 
   (fn [data _] [resize/resize-tool data]))
  data
  {:inspect-data true})

(deftest file-api-supported
  (testing "file api supported"
    (is js/File)
    (is js/FileReader) 
    (is js/FileList) 
    (is js/Blob)
    (is (not (with-redefs [html5/file-api-supported? (fn [] false)]
               (html5/file-api-supported?)))))
  (testing "image dimensions"
    (is (= {:display-height 281.25 :display-width 500}
           (select-keys (resize/limit-width {:height 2322 :width 4128} 500)
                        [:display-width :display-height])))
    (is (= {:display-height 888.8888888888888 :display-width 500} 
           (select-keys (resize/limit-width {:height 4128 :width 2322} 500)
                        [:display-width :display-height])))))

(defcard 
  "### Not Supported"
  (dc/reagent 
   (fn [data _] [resize/resize-tool data {:html5-file-api-supported? false}]))
  data)

(dc/start-devcard-ui!)
