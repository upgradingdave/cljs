(ns upgradingdave.orientation
  (:require [reagent.core :as r]
            [upgradingdave.html5 :as html5]))

(defn fix-orientation!
  "Given image and exif orientation, ensure the photo is displayed
  rightside up"
  [img {:keys [width height orig-width orig-height]}
   exif-orientation canvas-elid]
  (let [canvas (js/document.getElementById canvas-elid)
        ctx    (.getContext canvas "2d")]
    (js/console.log exif-orientation)
    (aset canvas "width"  width)
    (aset canvas "height" height)
    (.drawImage ctx img 0 0 orig-width orig-height 0 0 width height)
    (case exif-orientation
      1 (.transform ctx  1  0  0  1 0      0)
      2 (.transform ctx -1  0  0  1 orig-width  0)
      3 (.transform ctx -1  0  0 -1 orig-width  orig-height)
      4 (.transform ctx  1  0  0 -1 0      orig-height)
      5 (.transform ctx  0  1  1  0 0      0)
      6 (do 
          (.rotate ctx (* 90 (/ js/Math.PI 180)))
          (js/console.log "transforming!" (* 90 (/ js/Math.PI 180))) 
            ;;(.transform ctx  0  1 -1  0 orig-height 0)
          )
      7 (.transform ctx  0 -1 -1  0 orig-height orig-width)
      8 (.transform ctx  0 -1  1  0 0      orig-width))
))

(def data (r/atom {}))

;; (defn main []
;;   (if-let [node (.getElementById js/document "exif-div")]
;;     (r/render-component [exif-editor data] node)))

;; (main)
