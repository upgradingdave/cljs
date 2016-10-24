(ns up.img.exif.dev
  (:require
   [devcards.core     :as dc]
   [reagent.core      :as r]
   [up.img.exif.core  :as exif]
   [up.img.core       :as img])
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

;;TODO: in progress

(defn new-canvas []
  (doto (js/document.createElement "canvas")))

(defn fix-orientation 
  "Given image and exif orientation, ensure the photo is displayed
  rightside up"
  [img exif-orientation]
  (let [width  (.-width img)
        height (.-height img)
        canvas (js/document.getElementById "canvas")
        ctx    (.getContext canvas "2d")]
    (js/console.log "width")
    (js/console.log width)
    (js/console.log "height")
    (js/console.log height)
    (case exif-orientation
      1 (.transform ctx  1  0  0  1 0      0)
      2 (.transform ctx -1  0  0  1 width  0)
      3 (.transform ctx -1  0  0 -1 width  height)
      4 (.transform ctx  1  0  0 -1 0      height)
      5 (.transform ctx  0  1  1  0 0      0)
      6 (.transform ctx  0  1 -1  0 height 0)
      7 (.transform ctx  0 -1 -1  0 height width)
      8 (.transform ctx  0 -1  1  0 0      width))
    (.drawImage ctx img 0 0 width height 0 0 width height)))

(defn get-orientation [img data]
  (js/EXIF.getData 
   img
   (fn []
     (this-as 
      this 
      (let [exifdata    (js->clj (.-exifdata this))
            orientation (get exifdata "Orientation")]
        (swap! data assoc-in [:orientation] orientation)
        (fix-orientation this orientation))))))

(deftest file-api-supported
  (testing "sanity"
    (is (= true true))))

