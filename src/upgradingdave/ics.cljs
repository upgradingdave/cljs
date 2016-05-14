(ns upgradingdave.ics
  (:require [reagent.core :as r]
            [goog.date :as dt]
            [upgradingdave.csv :as csv]
            [cljsjs.ical]))

(defn format-datetime 
  [datetime]
  (str (inc (.getMonth datetime)) "/"
       (.getDate datetime)        "/"
       (.getFullYear datetime)    " "
       (.getHours datetime) ":" 
       (let [mins' (.getMinutes datetime)
             mins (if (> mins' 9) mins' (str mins' 0))]
         mins)))

(defn headings [events]
  (into [] (for [[k {:keys [name]}] (seq (first events))]
             name)))

(defn write-csv [events]
  (let [headings ["Start" "End" "Location" "Summary" "Description"]
        events'
        (->> (into [] (for [e events] 
                        [                        
                         (format-datetime (dt/fromIsoString 
                                           (:value (get e "dtstart"))))
                         (format-datetime (dt/fromIsoString 
                                           (:value (get e "dtend"))))
                         (js/decodeURIComponent (:value (get e "location")))
                         (:value (get e "summary"))
                         (:value (get e "description"))]
                        ;; TODO: this gets data generically
                        ;; but different rows can have different
                        ;; number of keys
                        ;; (for [[k {:keys [type value]}] (seq e)] 
                        ;;    (if (= type "date-time")
                        ;;      (format-datetime (dt/fromIsoString value))
                        ;;        value))
                        ))
             (into [headings]))]
    (csv/write-csv events' :quote true)))

(defn html5-file-api-supported? []
  (and js/File
       js/FileReader 
       js/FileList 
       js/Blob))

(defn ics-file? 
  "Determine whether a js File object represents an iCal"
  [f]
  (let [ftype (.-type f)] 
    (re-matches #"text/calendar" ftype)))

(defn get-file-name [file]
  (if file
    (js/escape (.-name file))))

(defn parse-props 
  "Parse list of ICAL Properties into maps indexed by property name"
  [props]
  (loop [acc [] props props]
    (let [[p & ps] props]
      (if (not p)
        (into {} acc)
        (let [[name _ type value] (.toJSON p)]
          (recur (conj acc [name {:name name :type type :value value}]) ps))))))

(defn parse-events 
  "Parse data from ICAL js objects into vector of maps, where each
  item in the list is a map that represents an event. Events consist
  of map of properties indexed by property name"
  [events]
  (loop [res [] events events]
    (let [[e & es] events]
      (if (not e)
        res
        (recur (conj res (parse-props (.getAllProperties e))) es)))))

(defn process-ics
  "Given a js File Object, parse the contents as ics. Produces a list
  of events, and each event is a map of properties indexed by property
  name. Sets the result into the global state"
  [ics data]
  (let [jcaldata (js/ICAL.parse ics)
        vcal     (js/ICAL.Component. jcaldata)
        events   (.getAllSubcomponents vcal "vevent")
        results (parse-events events)]
    (swap! data (fn [old]
                  (-> old
                      (assoc-in [:results] results)
                      (assoc-in [:loading] false))))))

(defn read-ics-file
  "Given a js File Object, parse the contents as ics. The resulting
  map is put into the data atom"
  [file data]
  (let [reader (doto (js/FileReader.)
                 (aset "onload" #(process-ics (-> % .-target .-result)
                                              data)))]
    (.readAsText reader file)))

(defn handle-file-select [e data]
  (let [files (array-seq (-> e .-target .-files))
        curr (first (filter ics-file? files))]
    (swap! data (fn [old]
                  (-> old
                      (assoc-in [:files]    files)
                      (assoc-in [:results]  nil)
                      (assoc-in [:loading]  true)
                      (assoc-in [:selected] (get-file-name curr)))))
    (read-ics-file curr data)))

(defn csv-link [data]
  (let [st @data
        content (str "data:text/csv;charset=utf-8,"
                     (write-csv (:results st)))
        href (js/encodeURI content)]
    [:a {:href href :download "results.csv"} "Download as CSV"]))

(defn file-select [data]
  [:span {:class "btn btn-default btn-file"
          :style {:position "relative"
                  :overflow "hidden"}}
   "Choose iCal (.ics) Files"
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

(defn display-results [data]
  (if (:loading @data)      
    [:div {:class "alert alert-success"} "Working ..."]
    
    (let [events (:results @data)]
      (if (empty? events)
        [:div {:class "alert alert-warning"} 
         "No results available for this file"]
        
        [:div
         [csv-link data]

         [:table {:class "table table-striped"}
          [:thead 
           [:tr 
            [:th "Start"]
            [:th "End"]
            [:th "Location"]
            [:th "Summary"]]]
          [:tbody
           (for [e events]
             (let [{start-date  :value} (get e "dtstart")
                   {end-date    :value} (get e "dtend")
                   {location    :value} (get e "location")
                   {summary     :value} (get e "summary")
                   {description :value} (get e "description")]
               [:tr {:key (gensym)}
                [:td (format-datetime (dt/fromIsoString start-date))]
                [:td (format-datetime (dt/fromIsoString end-date))]
                [:td location]
                [:td summary]]))]]]))))

(defn not-supported []
  [:div {:class "alert alert-danger"}
   (str "Oops, sorry but this iCal Tool won't work with
   your browser. This will only work with browsers that support the
   HTML5 File API. Maybe try using ")
   [:a {:href "https://www.google.com/chrome/browser"} 
    "Google Chrome instead."]])

(defn file-chooser [images data]
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
                                    (assoc-in [:results] nil)
                                    (assoc-in [:loading] true)
                                    (assoc-in [:selected] chosen))))

                  (read-ics-file curr data)
                  (.preventDefault %))}
           fname]])))])

(defn ical-tool [data & [{supported? :html5-file-api-supported?}]]
  [:div {:id "ical-tool" :class "container"}

   (let [supported (if (nil? supported?) 
                     (html5-file-api-supported?) supported?)
         files (:files @data)
         valid (filter ics-file? files)
         others (filter #(not (ics-file? %)) files)]
     
     ;; file api isn't supported, so show an error message
     (if (not supported)
       [:div {:class "row"}
        [:div {:class "col-xs-12"}
         [not-supported]]]

       ;; file app supported, try showing the widget
       [:div {:class "row"}

        [:div {:class "col-xs-12"}
         [:h3 "iCal Tool"]

         ;; File Chooser button and warning area
         [:div {:class "row"}
          [:div {:class "col-xs-2"}
           [file-select data]]
          [:div {:class "col-xs-10"}
           ;; if any files are not valid, then show warnings
           (if (not (empty? others))
             [:div {:class "alert alert-danger"} 
              "Oops, this only works for ics files, ignoring: "
              (apply str (interpose ", " (map #(get-file-name %) others)))])]]
         
         [:div {:class "row"}

          ;; horizontal separator, and then the chooser and table
          [:div {:class "col-xs-12"}
           [:hr]
           (if (not (empty? valid))
             [:div {:class "row"}
              [:div {:class "col-xs-12 col-sm-3"} 
               [file-chooser valid data]]
              [:div {:class "col-xs-12 col-sm-9"} 
               [display-results data]]])
           ]]]]))])

(def data (r/atom {}))

(defn main []
  (if-let [node (.getElementById js/document "ics-div")]
    (r/render-component [ical-tool data] node)))

(main)
