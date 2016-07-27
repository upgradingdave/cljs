(ns upgradingdave.common_dev
  (:require
   [devcards.core     :as dc]
   [reagent.core      :as r]
   [upgradingdave.exif  :as exif]
   [upgradingdave.img   :as img]
   [upgradingdave.html5 :as html5])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(def data (r/atom {}))

;; (defcard 
;;   "### Display Image"
;;   (dc/reagent 
;;    (fn [data _] 
;;      [:div
;;       (img/image data [:photo])]))
;;   (atom {:photo {:src "/i/20151117_124052.jpg"
;;                  :width  500
;;                  :height 500}})
;;   {:inspect-data true})

(defcard 
  "### Fix Orientation"
  (dc/reagent 
   (fn [data _] 
     [:div
      (img/image data [:photo] {:orient? true})]))
  (atom {:photo {:src "/i/20151117_124052.jpg"
                 :width  500
                 :height 500
                 :id (gensym)}})
  {:inspect-data true})

;; (defcard 
;;   "### Load Exif"
;;   (dc/reagent 
;;    (fn [data _] 
;;      [:div
;;       (img/image data [:photo] {:exif? true})]))
;;   (atom {:photo {:src "/i/20151117_124052.jpg"
;;                  :width  500
;;                  :height 500}})
;;   {:inspect-data true})

(defcard 
  "### File API Not Supported"
  (dc/reagent 
   (fn [data _] [exif/exif-editor data {:html5-file-api-supported? false}]))
  data)

(deftest file-api-supported
  (testing "file api supported"
    (is js/File)
    (is js/FileReader) 
    (is js/FileList) 
    (is js/Blob)
    (is (not (with-redefs [html5/file-api-supported? (fn [] false)]
               (html5/file-api-supported?)))))

  (testing "photo resize"
    (is (= {:src "/i/20151117_124052.jpg",
            :height 112.5,
            :width 200,
            :display-height 112.5, 
            :display-width  200, 
            :percent 4.844961240310078
            :orig-width  4128
            :orig-height 2322
            :orig-display-width  4128
            :orig-display-height 2322}
           (img/resize-by-max-width {:src "/i/20151117_124052.jpg"
                                     :orig-width  4128
                                     :orig-height 2322
                                     :orig-display-width  4128
                                     :orig-display-height 2322} 
                                    200)))

    (is (= {:src "/i/20151117_124052.jpg",
            :height 281.25,
            :width 500,
            :display-height 281.25, 
            :display-width  500, 
            :percent 12.112403100775193
            :orig-width  4128
            :orig-height 2322
            :orig-display-width  4128
            :orig-display-height 2322}
           (img/init-photo {:src "/i/20151117_124052.jpg"
                            :width  500
                            :height 300} 
                           4128 2322)))))



