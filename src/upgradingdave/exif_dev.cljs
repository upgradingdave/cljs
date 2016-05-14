(ns upgradingdave.exif_dev
  (:require
   [devcards.core     :as dc]
   [reagent.core      :as r]
   [upgradingdave.exif :as exif])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(def data (r/atom {}))

(defcard 
  "### Exif"
  (dc/reagent 
   (fn [data _] [exif/exif-editor data]))
  data
  {:inspect-data true})

(deftest get-next-color
  (testing "file api supported"
    (is js/File)
    (is js/FileReader) 
    (is js/FileList) 
    (is js/Blob)
    (is (not (with-redefs [exif/html5-file-api-supported? (fn [] false)]
               (exif/html5-file-api-supported?))))))

(defcard 
  "### File API Not Supported"
  (dc/reagent 
   (fn [data _] [exif/exif-editor data {:html5-file-api-supported? false}]))
  data)


