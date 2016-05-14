(ns upgradingdave.resize
  (:require [reagent.core :as r]
            [goog.date :as dt]
            [clojure.string      :as str]
            [upgradingdave.csv   :as csv]
            [upgradingdave.html5 :as html5]))

(defn get-file-name [file]
  (if file
    (js/escape (.-name file))))

(defn get-base-file-name [file-name]
  (second (first (re-seq #"^(.+)\.jpg$" file-name))))

(defn image? 
  "Determine whether a js File object represents an image"
  [f]
  (let [ftype (.-type f)] 
    (re-matches #"image/jpeg" ftype)))

(defn limit-width [{:keys [width height] :as photo} & [max-width]]
  (let [max-width  (or max-width width)
        bigger     (> width max-width)
        new-width  (if bigger max-width width)
        new-height (if bigger (* new-width (/ height width)) height)]
    (-> photo
        (assoc :display-height new-height)
        (assoc :orig-display-height new-height)
        (assoc :display-width  new-width)
        (assoc :orig-display-width  new-width))))

(defn new-image [img & [max-width]]
  (-> {:orig-width  (aget img "width")
       :width       (aget img "width")
       :orig-height (aget img "height")
       :height      (aget img "height")
       :src         (aget img "src")}
      (limit-width max-width)))

(defn load-image [data]
  (let [files     (:files @data)
        curr      (first (filter image? files))
        fname     (get-file-name curr)
        max-width (:max-width @data)]
    (if curr
      (let [src (js/URL.createObjectURL curr)]
        (swap! data (fn [old] (-> old 
                                  (assoc-in [:photo]       nil)
                                  (assoc-in [:image]       nil)
                                  (assoc-in [:zoom-slider] nil)
                                  (assoc-in [:loading]     true)
                                  (assoc-in [:selected]    fname))))
        (doto (js/Image.)
          (aset "onload" 
                (fn [_] 
                  (this-as t (swap! data assoc-in [:photo] 
                                    (new-image t max-width))
                           (swap! data assoc :image t))
                  (swap! data assoc-in [:loading] false)))
          (aset "src" src))))))

(defn handle-file-select [e data]
  (let [files (array-seq (-> e .-target .-files))
        ;;curr  (first (filter image? files))
        ;;fname (get-file-name curr)
        ]
    (swap! data assoc-in [:files] files)
    (load-image data)))

(defn file-select [data]
  [:span {:class "btn btn-default btn-file"
          :style {:position "relative"
                  :overflow "hidden"}}
   "Choose a Photo"
   [:input {:type "file" :id "files" :multiple true 
            :style {:position "absolute"
                    :top "0"
                    :right "0"
                    :min-width "100%"
                    :min-height "100%"
                    :font-size "100px"
                    :text-align "right"
                    :filter "alpha(opacity=0)"
                    :opacity "0"
                    :outline "none"
                    :background "white"
                    :cursor "inherit"
                    :display "block"}
            :on-change #(handle-file-select % data)}]])

(defn new-canvas [width height]
  (doto (js/document.createElement "canvas")
    (aset "width" width)
    (aset "height" height)))

;; (defn resize-raw-image 
;;   [img width height]
;;   (let [canvas (new-canvas width height)
;;         ctx  (.getContext canvas "2d")
;;         _    (.drawImage ctx img 0 0 width height)]
;;     canvas))

(defn resize-in-steps 
  "Uses down stepping technique for better quality"
  [img width height & [step]]
  (let [step        (or step 0.5) 
        orig-width  (aget img "width") 
        orig-height (aget img "height")
        next-width  (* step orig-width)
        next-height (* step orig-height)
        canvas      (doto (js/document.createElement "canvas")
                      (aset "width" next-width)
                      (aset "height" next-height))
        ctx         (.getContext canvas "2d")]

    (loop [prev-width orig-width
           prev-height orig-height
           next-width next-width 
           next-height next-height 
           img-or-canvas img
           ctx ctx]

      (if (<= next-width width)
        ;; final resize
        (let [final-canvas (doto (js/document.createElement "canvas")
                             (aset "width" width)
                             (aset "height" height))
              final-ctx    (.getContext final-canvas "2d")]
          (.drawImage final-ctx img-or-canvas 
                      0 0 prev-width prev-height
                      0 0 width height
                      )
          final-canvas)
        
        ;; keep resizing, calculate next dimensions and recur
        (let [prev-width'  next-width
              next-width'  (* step next-width)
              prev-height' next-height
              next-height' (* step next-height)]
          (.drawImage ctx img-or-canvas 0 0 prev-width prev-height 
                      0 0 next-width next-height)
          (recur prev-width' prev-height' next-width' next-height' canvas ctx)
          )))))

(defn resize-photo! [data height' width' display-height' display-width']
  (swap! data (fn [old]
                (-> old
                    (assoc-in [:photo :height] height')
                    (assoc-in [:photo :width]  width')
                    (assoc-in [:photo :display-height] display-height')
                    (assoc-in [:photo :display-width]  display-width')))))

(defn resize-by-percent! [data percent]
  (let [photo (:photo @data)
        {:keys [orig-height orig-width 
                orig-display-height 
                orig-display-width]} photo
        r       (/ percent 100)
        height' (* r orig-height)
        width'  (* r orig-width)
        display-height' (* r orig-display-height)
        display-width'  (* r orig-display-width)]
    (resize-photo! data height' width' display-height' display-width')))

(defn resize-by-width! [data width]
  (let [photo (:photo @data)
        {:keys [orig-height orig-width 
                orig-display-height 
                orig-display-width]} photo
        width (if (> width orig-width) orig-width width)
        width (if (< width 0) 0 width)
        r     (/ width orig-width)
        percent (* 100 r)
        ratio (/ orig-height orig-width)
        width' width
        height' (* width ratio)
        display-height' (* r orig-display-height)
        display-width'  (* r orig-display-width)]
    (swap! data assoc-in [:zoom-slider :value] percent)
    (resize-photo! data height' width' display-height' display-width')
    ))

(defn resize-by-height! [data height]
  (let [photo (:photo @data)
        {:keys [orig-height orig-width 
                orig-display-height 
                orig-display-width]} photo
        height (if (> height orig-height) orig-height height)
        height (if (< height 0) 0 height)
        r     (/ height orig-height)
        percent (* 100 r)
        ratio (/ orig-width orig-height)
        height' height
        width' (* height ratio)
        display-height' (* r orig-display-height)
        display-width'  (* r orig-display-width)]
    (swap! data assoc-in [:zoom-slider :value] percent)
    (resize-photo! data height' width' display-height' display-width')
    ))

(defn slider-control [data]
  (let [v (or (get-in @data [:zoom-slider :value]) 100)]
    [:div {:class "form-group"}
     [:label (str "Size - " (js/Math.round  
                               (or
                                (get-in @data [:zoom-slider :value]) 100)) "%")]
     [:input {:type "range" :name "zoom" :id "zoom"
              :min "0" :max "100"
              :value v
              :on-change #(let [new-val (-> % .-target .-value)]
                            (swap! data assoc-in [:zoom-slider :value] new-val)
                            (resize-by-percent! data new-val)
                            )}]]))

(def max-thumb-size 150)

(defn finish-resize [data url]
  (let [{:keys [width height]} (:photo @data)
        img      (:image @data) 
        selected (:selected @data)
        name     (str (get-base-file-name selected)
                      "_" (.toFixed width 2) 
                      "_" (.toFixed height 2) ".jpg")
        photo    (-> (new-image img max-thumb-size)
                     (assoc :url url)
                     (assoc :name name)
                     (assoc :width width)
                     (assoc :height height)
                     (limit-width max-thumb-size))]

      (swap! data (fn [old]
                    (let [resized 
                          (into [] (take-last 3 (:resized old)))
                          resized' (conj resized photo)]
                      (assoc old :resized resized'))))))

;;.toDataURL canvas "image/jpg" 0.7
(defn do-resize [data]
  (fn [evt] 
    (let [{:keys [width height]} (:photo @data)
          img                    (:image @data) 
          canvas (resize-in-steps img width height)]
      (html5/to-blob canvas
                     (fn [blob]
                       (let [url (js/URL.createObjectURL blob)]
                         (finish-resize data url))
                       )
                     "image/jpg"
                     0.7)
      (.preventDefault evt))))

(defn resize-button [data]
  (let [{:keys [width height]} (:photo @data)
        img                    (:image @data)]
    [:div {:class "row"}
     [:div {:class "col-xs-3"}
      [:button {:class "btn btn-primary"
                :on-click (do-resize data)}
       (str "Resize to " (.toFixed width 2) " X " (.toFixed height 2))]]
     [:div {:class "col-xs-9"} 
      (if (:working @data) 
        [:div {:class "alert alert-warning"} (str "Generating Resized Image "
                                               "... Please Wait ...")])]]))

(defn open-image [photo]
  (fn [_]
    (let [url (:url photo)]
      (doto (js/window.open (:url photo))))))

(defn resize-downloads [data]
  (r/create-class
   {;; :component-did-update
    ;; (fn [_] (
    ;;   (swap! data assoc :working false))

    :reagent-render
    (fn [data]
      (if (empty? (:resized @data))
        [:div {:class "row"}
         [:div {:class "col-xs-12"}
          [:p {:class "text-info"} (str "Enter a new width or height, or use "
                                        "the slider and then click the "
                                        "resize button to resize the image. "
                                        "Resized images will appear here.")]]]

        [:div {:id "resized-downloads"}
         [:div {:class "row"}
          [:div {:class "col-xs-12"}
           [:h3 "Resized"]]]

         [:div {:class "row"}
          (doall 
           (for [v (:resized @data)]
             (let [{:keys [width height display-width 
                           display-height url name]} v]
               ^{:key (gensym)} [:div {:class "col-sm-3"}
                                 [:div {:class "thumbnail"}
                                  [:img {:width  (min display-width)
                                         :height (min display-height)
                                         :src url
                                         ;; :on-load #(swap! data assoc 
                                         ;;                  :working false)
                                         }]
                                  [:h4 (str (.toFixed width 2) " X " 
                                            (.toFixed height 2))]
                                  [:div {:class "btn-group"}
                                   [:a {:class "btn btn-default"
                                        :href url
                                        :download name} 
                                    "Download"]
                                   [:a {:class "btn btn-default"
                                        ;;:href url
                                        ;;:download name
                                        :on-click (open-image v)
                                        } 
                                    "Open"]]
                                  ]])))]]))}))

(defn photo-editor [data]
  (if (:loading @data)
    [:div {:class "alert alert-success"} "Working ..."]
    
    (if-let [photo (:photo @data)]
      [:div {:id "image-editor"}

       [resize-downloads data]
       [:hr]

       [:div {:class "row"}
        [:div {:class "col-xs-12"}
         [:form {:class "form-inline"}
          [:div {:class "form-group"}
           [:label {:for "photo-width"
                    :style {:width "110px"}} "Photo Width"]
           [:input {:id "photo-width" 
                    :type "text" 
                    :class "form-control"
                    :value (:width photo)
                    :on-change 
                    #(let [new-val (-> % .-target .-value)]
                       (if (empty? new-val) 
                         (swap! data assoc-in [:photo :width] nil)
                         (when (not (js/isNaN new-val))
                           (let [width (js/parseInt new-val)]
                             (resize-by-width! data width)))))}]]
          [:div {:class "form-group"}
           [:label {:for "photo-height"
                    :style {:width "110px"
                            :margin-left "10px"}} "Photo Height"]
           [:input {:id "photo-height" 
                    :type "text" 
                    :class "form-control"
                    :value (:height photo)
                    :on-change
                    #(let [new-val (-> % .-target .-value)]
                       (if (empty? new-val) 
                         (swap! data assoc-in [:photo :height] nil)
                         (when (not (js/isNaN new-val))
                           (let [height (js/parseInt new-val)]
                             (resize-by-height! data height)))))}]]]]]

       [slider-control data]
       [:div {:class "form-group"}
        [resize-button data]]
       [:img {:class  "thumbnail" 
              :src    (:src photo) 
              :width  (:display-width photo) 
              :height (:display-height photo)}]]
      )))

(defn not-supported []
  [:div {:class "alert alert-danger"}
   (str "Oops, sorry but this Image Resize Tool won't work with
   your browser. This will only work with browsers that support the
   HTML5 File API. Maybe try using ")
   [:a {:href "https://www.google.com/chrome/browser"} 
    "Google Chrome instead."]])

(defn resize-tool [data & [{supported? :html5-file-api-supported?}]]
  [:div {:id "resize-tool" :class "container"}

   (let [supported (if (nil? supported?) 
                     (html5/file-api-supported?) supported?)
         files (:files @data)
         others (filter #(not (image? %)) files)]
     
     ;; file api isn't supported, so show an error message
     (if (not supported)
       [:div {:class "row"}
        [:div {:class "col-xs-12"}
         [not-supported]]]

       ;; file api supported, try showing the widget
       [:div {:class "row"}

        [:div {:class "col-xs-12"}
         [:h3 "Resize Tool"]

         ;; File Selection Button and warning area
         [:div {:class "row"}
          [:div {:class "col-xs-2"}
           [file-select data]]
          [:div {:class "col-xs-10"}
           ;; if any files are not valid, then show warnings
           (if (not (empty? others))
             [:div {:class "alert alert-danger"} 
              "Oops, this only works for JPEG files, ignoring: "
              (apply str (interpose ", " (map #(get-file-name %) others)))])]]
         
         [:div {:class "row"}

          ;; horizontal separator, and then the chooser and table
          [:div {:class "col-xs-12"}
           [:hr]
           [:div {:class "row"}
            [:div {:class "col-xs-12"} 
             [photo-editor data]]]
           ]]]]))])

(def data (r/atom {}))

(defn main []
  (if-let [node (.getElementById js/document "resize-div")]
    (r/render-component [resize-tool data] node)))

(main)
