(ns upgradingdave.exif
  (:require [reagent.core :as r]
            [cljsjs.exif]
            [upgradingdave.html5 :as html5]))

(defn load-exif!
  "Given a js File Object, parse the exif metadata. The resulting map
  is put into the data atom"
  [img data path & [orient? canvas-elid]]
  (js/EXIF.getData 
   img
   (fn []
     (this-as 
      this 
      (let [exifdata    (js->clj (.-exifdata this))]
        (swap! data assoc-in (conj path :exif) exifdata))))))

(defn image? 
  "Determine whether a js File object represents an image"
  [f]
  (let [ftype (.-type f)] 
    (re-matches #"image/jpeg" ftype)))

(defn get-file-name [file]
  (js/escape (.-name file)))

(defn process-img 
  "Given a js File Object, parse the exif metadata. The resulting map
  is put into the data atom"
  [img data]
  (js/EXIF.getData 
   img
   (fn []
     (this-as 
      this 
      (let [exifdata (js->clj (.-exifdata this))]
        (swap! data (fn [old] 
                      (-> old
                          (assoc-in [:exif] exifdata)
                          (assoc-in [:loading] nil)))))))))

(defn handle-file-select [e data]
  (let [files (array-seq (-> e .-target .-files))
        curr (first (filter image? files))
        fname (get-file-name curr)]
    (swap! data (fn [old]
                  (-> old
                      (assoc-in [:exif] nil)
                      (assoc-in [:loading] true)
                      (assoc-in [:files] files)
                      (assoc-in [:selected] fname))))
    (process-img curr data)))

(defn file-select [data]
  [:span {:class "btn btn-default btn-file"
          :style {:position "relative"
                  :overflow "hidden"}}
   "Choose Images"
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

(defn display-exif-fields [ks exif]
  [:table {:class "table table-striped"}
   [:tbody
    (for [k ks]
      [:tr {:key (name k)}
       [:td {:style {:text-align ""}} (name k)]
       [:td (str (get exif k))]]
      )]]
  )

(defn display-exif [data]
  (if (:loading @data)      
    [:div {:class "alert alert-success"} "Working ..."]
    
    (let [exif  (:exif @data)
          ks    (sort (filter #(not (= "UserComment" %)) (keys exif)))
          h     (quot (count ks) 2)
          half1 (take h ks)
          half2 (drop h ks)]
      (if (empty? ks)
        [:div {:class "alert alert-warning"} 
         "No exif metadata found in this image"]
        [:div {:id "exif-table" :class "row"}
         [:div {:class "col-sm-12 col-md-6"}
          [display-exif-fields half1 exif]]
         [:div {:class "col-sm-12 col-md-6"}
          [display-exif-fields half2 exif]]]))))

(defn exif-editor-not-supported []
  [:div {:class "alert alert-danger"}
   (str "Oops, sorry but this EXIF Metadata Viewer won't work with
   your browser. This will only work with browsers that support the
   HTML5 File API. Maybe try using ")
   [:a {:href "https://www.google.com/chrome/browser"} 
    "Google Chrome instead."]])

(defn exif-image-chooser [images data]
  [:ul {:class "nav nav-pills nav-stacked"}
   (let [files  images
         active (:selected @data)]
     (for [file files]
       (let [fname (get-file-name file)]
         [:li {:key fname
               :class (if (= active fname) "active")}
          [:a {:href "#" 
               :id fname
               :on-click 
               #(let [chosen (-> % .-target .-id)
                      curr   (first (filter 
                                     (fn [i] (= (get-file-name i) chosen)) 
                                     images))]
                  (swap! data (fn [old]
                                (-> old
                                    (assoc-in [:exif] nil)
                                    (assoc-in [:loading] true)
                                    (assoc-in [:selected] chosen))))

                  (process-img curr data)
                  (.preventDefault %))}
           fname]])))])

(defn exif-editor [data & [{supported? :html5-file-api-supported?}]]
  [:div {:id "exif-editor" :class "container"}

   (let [supported (if (nil? supported?) 
                     (html5/file-api-supported?) supported?)
         files (:files @data)
         images (filter image? files)
         others (filter #(not (image? %)) files)]
     
     ;; file api isn't supported, so show an error message
     (if (not supported)
       [:div {:class "row"}
        [:div {:class "col-xs-12"}
         [exif-editor-not-supported]]]

       ;; file app supported, try showing the widget
       [:div {:class "row"}

        [:div {:class "col-xs-12"}
         [:h3 "Image Exif Viewer"]

         ;; File Chooser button and warning area
         [:div {:class "row"}
          [:div {:class "col-xs-2"}
           [file-select data]]
          [:div {:class "col-xs-10"}
           ;; if any files are not images, then show warnings
           (if (not (empty? others))
             [:div {:class "alert alert-danger"} 
              "Oops, this only works for jpg files, ignoring: "
              (apply str (interpose ", " (map #(get-file-name %) others)))])]]
         
         [:div {:class "row"}

          ;; horizontal separator, and then the chooser and table
          [:div {:class "col-xs-12"}
           [:hr]
           (if (not (empty? images))
             [:div {:class "row"}
              [:div {:class "col-xs-12 col-sm-3"} 
               [exif-image-chooser images data]]
              [:div {:class "col-xs-12 col-sm-9"} 
               [display-exif data]]])
           ]]]]))])

(def data (r/atom {}))

(defn main []
  (if-let [node (.getElementById js/document "exif-div")]
    (r/render-component [exif-editor data] node)))

(main)
