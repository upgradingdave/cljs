(ns upgradingdave.img
  (:require [reagent.core :as r]
            [upgradingdave.exif :as exif]
            [upgradingdave.html5 :as html5]))

(defn resize-by-percent 
  "Resize a photo by percent"
  [photo percent]
  (let [{:keys [orig-height orig-width 
                orig-display-height 
                orig-display-width]} photo
        r       (/ percent 100)
        height' (* r orig-height)
        width'  (* r orig-width)
        display-height' (* r orig-display-height)
        display-width'  (* r orig-display-width)]
    (-> photo 
        (assoc :height height')
        (assoc :width  width')
        (assoc :display-height display-height')
        (assoc :display-width  display-width')
        (assoc :percent percent))))

(defn resize-by-max-width 
  "Resize a photo ensuring that width is at most new-width. height is
  changed to keep same proportion."
  [photo new-width]  
  (let [{:keys [orig-height orig-width 
                orig-display-height 
                orig-display-width]} photo
        width' (if (> new-width orig-width) orig-width new-width)
        width' (if (< width' 0) 0 width')
        r     (/ width' orig-width)
        percent (* 100 r)
        ratio (/ orig-height orig-width)
        height' (* width' ratio)
        display-height' (* r orig-display-height)
        display-width'  (* r orig-display-width)]
    (-> photo
        (assoc :height height')
        (assoc :width  width')
        (assoc :display-height display-height')
        (assoc :display-width  display-width')
        (assoc :percent percent)
        )))

(defn resize-by-max-height 
  "Resize a photo ensuring that height is at most new-height. width is
  changed to keep the same proportion"
  [photo new-height]
  (let [{:keys [orig-height orig-width 
                orig-display-height 
                orig-display-width]} photo
        height' (if (> new-height orig-height) orig-height new-height)
        height' (if (< height' 0) 0 height')
        r     (/ height' orig-height)
        percent (* 100 r)
        ratio (/ orig-width orig-height)
        width' (* height' ratio)
        display-height' (* r orig-display-height)
        display-width'  (* r orig-display-width)]
    (-> photo
        (assoc :height height')
        (assoc :width  width')
        (assoc :display-height display-height')
        (assoc :display-width  display-width')
        (assoc :percent percent))))

(defn init-photo 
  "Initialize all properties of a photo. If width and height are
  available in original photo, they are considered max-width and
  max-height"
  [{:keys [width height] :as photo} width' height']
  (let [max-width  (or width width')
        max-height (or height height')
        photo' (assoc photo  :orig-width  width')
        photo' (assoc photo' :orig-height height')
        photo' (assoc photo' :orig-display-width  width')
        photo' (assoc photo' :orig-display-height height')]
    (if (>= max-width max-height) 
      (resize-by-max-width  photo' max-width)
      (resize-by-max-height photo' max-height))))

(defn on-load-img!
  "Create an event function to initialize an images state map.

  Available Options: 

  :exif? - if true, exif data is parsed and loaded into state map
  :orient? - if true, exif data is loaded and orientation is set so
  that the photo displays correctly
"
  [data path & [{:keys [orient? exif? canvasid]}]]
  (fn [evt]
    (let [image   (.-target evt)
          width'  (.-width image)
          height' (.-height image)
          photo'  (init-photo (get-in @data path) width' height')]
      (swap! data assoc-in path photo')
      (js/console.log canvasid)
      (if orient?
        (exif/load-exif! image data path true canvasid)
        (if exif? (exif/load-exif! image data path false canvasid))))))

(defn image 
  "A react image component. The :width and :height inside the state
  map are used to ensure the image is displayed within max width and
  max height. 

  You must also ensure there's a unique :id.

  Available Options: 

  :exif? - loads exif data
  :orient? - loads exif data and fixes orientation
"
  [data path & [opts]]
  (let [{:keys [id src height width
                display-height 
                display-width]} (get-in @data path)
        canvasid (str "canvas" id)]
    [:div {:id id}
     [:canvas {:id canvasid}]
     (if (and display-height display-width)
       [:img {:class  "thumbnail" 
              :src    src
              :height display-height
              :width  display-width}]
       [:img {:style   {:display "none"}
              :src     src
              :on-load (on-load-img! data path 
                                     (assoc opts :canvasid canvasid))}]
       )]))
