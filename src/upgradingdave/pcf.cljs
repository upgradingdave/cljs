(ns upgradingdave.pcf
  (:require [reagent.core :as r]))

(def lookup {:red    "000"
             :orange "001"
             :yellow "010"
             :green  "011"
             :blue   "100"
             :indigo "101"
             :violet "110"
             :white  "111"})

(defn get-next-color [idx]
  (let [possible (into [] (map-indexed (fn [idx item] [idx item]) lookup))]
    (if (>= idx (dec (count possible)))
      (get possible 0)
      (get possible (inc idx)))))

(defn pixel-style [color]
  {:width            "11%" 
   :height           "50px" 
   :background-color color
   :cursor           "pointer"
   :float            "left"
   :text-align       "center"
   :vertical-align   "middle"
   :line-height      "50px"
   :-webkit-touch-callout "none"
   :-webkit-user-select   "none"
   :-khtml-user-select    "none"
   :-moz-user-select      "none"
   :-ms-user-select       "none"
   :user-select           "none"})

(defn find-by-binary 
  "Find key in lookup table for corresponding binary string s, like
  \"000\""
  [s]
  (first (filter (fn [[i [k v]]] (= v s)) (map-indexed vector lookup))))

(defn binary-to-pixels
  "Convert string of 1's and 0's into pixels data structure"
  [binary]
  (let [triples (map #(let [b (apply str %)] b) (partition 3 binary))]
    (into [] (map find-by-binary triples))))

(defn pixels-to-binary [pixels]
  (apply str (map (fn [[_ [_ b]]] b) pixels)))

(def border-style "2px solid #000000")

(defn pixel [idx data & [{:keys [show-codes? position order]}]]
  (let [[i [k v]] (get (into {} (map-indexed vector (:pixels @data))) idx)
        color (name k)
        style' (if (= color "indigo") 
                  (assoc (pixel-style color) :color "white") 
                  (pixel-style color))
        style'' (case position
                  :first  (assoc style' :border-left border-style)
                  :middle (assoc style' :border-left border-style)
                  (assoc style' :border-right border-style
                         :border-left border-style))
        style (case order
                :first  (assoc style'' :border-top border-style)
                :middle (assoc style'' :border-top border-style)
                (assoc style'' :border-bottom border-style
                       :border-top border-style))]
    [:div {:style style
           :on-click 
           #(swap! 
             data 
             (fn [o] (let [o (assoc-in o [:pixels idx] (get-next-color i))]
                       (assoc o :editor-error false 
                              :editor-value (pixels-to-binary (:pixels o))))))}
     (if show-codes? v)]))

(defn pixel-row [pixels data & [opts]]
  (let [[fidx _] (first pixels)
        [lidx _] (last  pixels)
        middle (butlast (rest pixels))]
    [:div {:class "pcf-row"}
     ^{:key fidx} [pixel fidx data (assoc opts :position :first)]
     (for [[idx _] middle]
       ^{:key idx} [pixel idx data (assoc opts :position :middle)])
     ^{:key lidx} [pixel lidx data (assoc opts :position :last)]
     [:div {:style {:clear "both"}}]]))

(defn image-editor [data & [opts]]
  (let [rows  (partition 8 (map-indexed vector (:pixels @data)))
        frow  (first rows)
        lrow  (last rows)
        mrows (butlast (rest rows))]
    [:div {:id "pcf-image"}
     ^{:key (gensym)} [pixel-row frow data (assoc opts :order :first)]
     (for [row mrows]
       ^{:key (gensym)} [pixel-row row data (assoc opts :order :middle)])
     ^{:key (gensym)} [pixel-row lrow data (assoc opts :order :last)]
     ]))

(defn update-binary-textbox [prev new data]
  (if (and (= (* 64 3) (count new))
           (every? #(or (= \0 %) (= \1 %)) new))
    (swap! data (fn [o] (-> o 
                            (assoc-in [:pixels] (binary-to-pixels new))
                            (assoc :editor-error false :editor-value new))))
    (if (every? #(or (= \0 %) (= \1 %)) new)
      (swap! data assoc :editor-error true :editor-value new))))

(defn binary-editor [data]
  [:div {:id "pcf-binary-input"}
   (let [error?  (:editor-error @data)
         editor-val (:editor-value @data)
         style   {:resize "none"
                  :outline "none"
                  :font-size "22px"
                  :width "14em"}]
     [:textarea {:cols "25"
                 :rows "8"
                 :style (if error? (assoc style :border "3px solid red")
                            (assoc style :border "3px solid green")) 
                 :value editor-val
                 :on-change #(let [v (-> % .-target .-value)]
                               (update-binary-textbox editor-val v data)
                               (.preventDefault %))}])])

(defn editor [data]
  (let [show-codes? (:show-codes? @data)]
    [:div {:id "pcf-editor"}
     [:div {:style {:float "left"
                    :width "60%"}}
      [image-editor data {:show-codes? show-codes? }]]
     [:div {:style {:float "left"
                    :width "40%"}}
      [:div {:class "checkbox"}
       [:label {:for "binarycodes"}
        [:input {:type "checkbox" 
                 :name "binarycodes" 
                 :value "binarycodes"
                 :on-change #(swap! data assoc :show-codes? (not show-codes?))}]
        "Show Binary Codes"]]
      [binary-editor data]]
     [:div {:style {:clear "both"}}]]))

(def data (r/atom 
           (let [pixels (into [] (repeat 64 [7 [:white "111"]]))] 
             {:pixels pixels
              :editor-value (pixels-to-binary pixels)})))

(def ex1 "011011011100100100100100011100011000100000100000011100011000100000100000011011011000100000100000011100100000100000100000011100100000100000100000011100100100000100000100100100100100100100100100")

(def ex2 "100100100100100100010010100100011100100100010010100011011011100100100100100011011011100100100100011011011011011100100100100011001011100100100100100100001100100100100100000000001000000000000000")

(def ex3 "010010010010010010010010010100100100100100100010010100010010010010100010010100010100100010100010010100010100010010100010010100010100100100100010010100010010010010010010010100100100100100100100")

(defn main []
  (if-let [node (.getElementById js/document "pcf-div")]
    (r/render-component [editor data] node)))

(main)


