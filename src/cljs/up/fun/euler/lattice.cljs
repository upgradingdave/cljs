(ns up.fun.euler.lattice
  (:require [reagent.core :as r]
            [cljs.core.async :refer [chan close! <! take! timeout]])
  (:require-macros
   [cljs.core.async.macros :as m :refer [go]]))

(def data (r/atom {:size       3
                   :input-size 3
                   :cells (into [] (repeat (* 3 3) {:paths 0}))}))

(defn sleep [msec]
  (let [deadline (+ msec (.getTime (js/Date.)))]
    (while (> deadline (.getTime (js/Date.))))))

(defn init-lattice! [n]
  (let [cells (into [] (repeat (* n n) {:paths 0}))]
    (reset! data {:size        n
                  :input-size n
                  :cells cells})))

(def calc-path
  (memoize
   (fn [x y n]
     (let [result (if (or (zero? x) (zero? y))
                    1
                    (+ (calc-path (dec x) y n) (calc-path x (dec y) n)))]
       (swap! data assoc-in [:cells (+ (* y n) x) :paths] result) 
       result))))

(defn pixel-style [size & [color]]
  {:width                 (str (* 100 (/ 1 size)) "%")
   :height                (str (* 100 (/ 1 size)) "%")
   :background-color      (or color "white")
   :float                 "left"
   :text-align            "left"
   :vertical-align        "top"
   :line-height           "20px"
   :font-size             "0.8em"
   :border-top            "none"
   :cursor                "pointer"
   :-webkit-touch-callout "none"
   :-webkit-user-select   "none"
   :-khtml-user-select    "none"
   :-moz-user-select      "none"
   :-ms-user-select       "none"
   :user-select           "none"})

(def border-style "2px solid #000000")

(defn truncate [n s]
  (let [s (str s) 
        len (count s)]
    (if (> len n) (str (apply str (take n s)) "..") s)))

(defn cell [idx data & [{:keys [position order]}]]
  (let [{:keys [paths visited]} 
        (get (into {} (map-indexed vector (:cells @data))) idx)
        
        style'  (-> (pixel-style (:size @data))
                    (assoc :border-left border-style)
                    (assoc :border-top border-style))
        style'' (cond 
                  (= position :first)  (assoc style' :border-left border-style)
                  (= position :middle) (assoc style' :border-left border-style)
                  (= position :last)   (dissoc style' :border-top)
                  )
        style   (cond 
                  (= order :last) (dissoc style'' :border-left)
                  :else style''
                  )]
    [:div {:style style}
     [:div {:style {:margin "3px" 
                    :overflow "hidden"}}
      (if paths [:a {:title paths} (truncate 4 paths)])]]))

(defn cell-row [row data & [opts]]
  (let [[fidx _] (first row)
        [lidx _] (last  row)
        middle (butlast (rest row))]
    [:div {:class "pcf-row"}
     ^{:key fidx} [cell fidx data (assoc opts :position :first)]
     (for [[idx _] middle]
       ^{:key idx} [cell idx data (assoc opts :position :middle)])
     ^{:key lidx} [cell lidx data (assoc opts :position :last)]
     [:div {:style {:clear "both"}}]]))

(defn lattice [data & [opts]]
  (let [cells (:cells @data) 
        rows  (partition (:size @data) (map-indexed vector cells))
        frow  (first rows)
        lrow  (last rows)
        mrows (butlast (rest rows))]
    [:div {:id "lattice"}
     ^{:key (gensym)} [cell-row frow data (assoc opts :order :first)]
     (for [row mrows]
       ^{:key (gensym)} [cell-row row data (assoc opts :order :middle)])
     ^{:key (gensym)} [cell-row lrow data (assoc opts :order :last)]
     ]))

(defn size-form [data]
  [:div {:style {:margin-bottom "50px"}}
   [:form
    [:div {:class "form-group"}
     [:label "Number of Columns and Rows"]
     [:input {:class "form-control" :type "text" :style {:width "100px"}
              :value (if (nil? (:input-size @data)) 
                       (:input-size @data)
                       (dec (:input-size @data)))
              :on-change #(let [new-val (-> % .-target .-value)]
                            (swap! data assoc-in [:input-size] 
                                   (if (or (empty? new-val) (js/isNaN new-val))
                                     nil
                                     (inc (js/parseInt new-val)))
                                   ))}]]
    [:button {:class "btn btn-default"
              :on-click #(do
                           (init-lattice! (:input-size @data))
                           (.preventDefault %))
              } "Generate Lattice"]]])

(defn widget [data]
  (let [size (:size @data)]
    [:div {:id "lattice-container"}
     [size-form data]
     (if (:cells @data)
       [:div {:id "lattice-widget"}
        [:div {:style {:margin-bottom "20px"}}
         [:button {:class "btn btn-default"
                   :on-click #(do
                                (calc-path (dec size) (dec size) size)
                                (.preventDefault %))}
          "Solve!"]]
        [:div {:style {:width "100%"}}
         [lattice data]]])]))

(defn main []
  (if-let [node (.getElementById js/document "lattice-div")]
    (r/render-component [widget data] node)))

(main)
