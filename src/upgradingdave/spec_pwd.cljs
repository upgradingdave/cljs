(ns upgradingdave.spec-pwd
  (:require [reagent.core       :as r]
            [upgradingdave.pass :as p]
            [cljs.spec          :as s]
            [cljs.spec.impl.gen :as gen]))

(defn format-pwd-conf 
  "There's some extra stuff put into the map to make it more
  convenient to render form fields. This removes that extra stuff
  before sending to password spec"
  [pwd-conf]
  (update-in pwd-conf [:upgradingdave.pass/valid-char-classes]
             #(filter (fn [o] (:upgradingdave.pass/enabled? o)) %)))

(defn gen-pwd [data]
  (if-let [pwd-fn (get-in @data [:pwd :fn])]
    (pwd-fn)))

(defn create-password-generator! [data]
  (let [pwd-conf (format-pwd-conf (get-in @data [:pwd-conf]))]
    (if (s/valid? :upgradingdave.pass/password-conf pwd-conf)
      (let [pwd-spec (p/create-password-spec pwd-conf) 
            pwd-gen  (p/create-password-gen  pwd-conf)
            pwd-fn   (p/create-password-fn   pwd-spec)]
        (swap! data assoc-in [:pwd :spec] pwd-spec)
        (swap! data assoc-in [:pwd :gen]  pwd-gen)
        (swap! data assoc-in [:pwd :fn]   pwd-fn)
        (swap! data assoc-in [:result]    (gen-pwd data)))
      (let [probs (s/explain-str :upgradingdave.pass/password-conf pwd-conf)]
        (js/console.log (str probs))))))

(defn find-class-path 
  "Returns path to map if found"
  [pwd-conf class-kw]
  (let [classes (map-indexed 
                 vector
                 (:upgradingdave.pass/valid-char-classes pwd-conf))]
    [:upgradingdave.pass/valid-char-classes 
     (first
      (first (filter (fn [[_ {class :upgradingdave.pass/class}]] 
                       (= class class-kw))
                     classes)))]))

(defn find-class
  [pwd-conf class-kw]
  (get-in pwd-conf (find-class-path pwd-conf class-kw)))

(defn class-enabled? [pwd-conf class-kw]
  (if-let [c (find-class pwd-conf class-kw)]
    (:upgradingdave.pass/enabled? c)))

(defn enable-class [pwd-conf class-kw]
  (assoc-in pwd-conf (conj (find-class-path pwd-conf class-kw) 
                           :upgradingdave.pass/enabled?) true))

(defn disable-class [pwd-conf class-kw]
  (assoc-in pwd-conf (conj (find-class-path pwd-conf class-kw) 
                           :upgradingdave.pass/enabled?) false))

(defn update-class-min [pwd-conf class-kw v]
  (assoc-in pwd-conf (conj (find-class-path pwd-conf class-kw) 
                           :upgradingdave.pass/min) v))

;; maybe implement this later
;; (defn update-class-valid [pwd-conf class-kw v]
;;   (assoc-in pwd-conf (conj (find-class-path pwd-conf class-kw) 
;;                            :upgradingdave.pass/valid) v))

(defn add-class! [data pwd-conf val]
  (swap! data assoc-in [:pwd-conf :upgradingdave.pass/valid-char-classes]
         (let [classes (:upgradingdave.pass/valid-char-classes pwd-conf)]
           (conj classes val))))

(defn toggle-class [data path pwd-conf class-kw]
  #(let [new-val (-> % .-target .-checked)]
     (swap! data assoc-in path 
            (if new-val
              (enable-class pwd-conf class-kw)
              (disable-class pwd-conf class-kw)))
     (create-password-generator! data)))

(defn class-checkbox [class-kw data & [path]]
  (let [pwd-conf (get-in @data path)
        class-enabled? (class-enabled? pwd-conf class-kw)
        desc (:upgradingdave.pass/desc (find-class pwd-conf class-kw))]
    [:div {:class "form-group checkbox"}
     [:label 
      [:input {:type "checkbox"
               :checked class-enabled?
               :on-change (toggle-class data path pwd-conf class-kw)}]
      desc]]))

(defn update-class-min! [data path class-kw]
  #(let [pwd-conf (get-in @data path)
         new-val (-> % .-target .-value)
         v (if (or (empty? new-val) (js/isNaN new-val))
             0 
             (js/parseInt new-val))]
     (js/console.log v)
     (swap! data assoc-in path 
            (update-class-min pwd-conf class-kw v))
     (create-password-generator! data)
     ))

(defn class-min-textbox [class-kw data path]
  (let [pwd-conf (get-in @data path)
        class-min (get (find-class pwd-conf class-kw)
                       :upgradingdave.pass/min
                       0)]
    [:div {:class "form-group"}
     [:label {:class "control-label"} "Minimum number required?"]
     [:input {:class "form-control" 
              :type "text" 
              :value class-min
              :on-change (update-class-min! data path class-kw)}]]))

(defn class-control [class-kw data & [path]]
  (let [pwd-conf (get-in @data path)
        class-enabled? (class-enabled? pwd-conf class-kw)]
    [:div {:class "class-control"}
     (class-checkbox class-kw data path)
     (if class-enabled?
       (class-min-textbox class-kw data path))]))

(defn update-min-max [data path kw]
  #(let [new-val (-> % .-target .-value)
         pwd-conf (get-in @data path)
         v (if (or (empty? new-val) (js/isNaN new-val))
             0 
             (js/parseInt new-val))]
     (swap! data assoc-in path 
            (assoc pwd-conf kw v))
     (create-password-generator! data)))

(defn min-max-textbox [label kw data & [path]]
  (let [pwd-conf (get-in @data path)
        min-len  (get-in pwd-conf [kw])]
    [:div {:class "form-group"}
     [:label {:class "control-label"} label]
     [:input {:class "form-control" 
              :type "text" 
              :value min-len
              :on-change (update-min-max data path kw)}]]))

(defn password-form [data]
  [:form
   [:div {:class "form-group"}
    [:button {:class "btn btn-default"
              :on-click #(do
                           (swap! data assoc-in [:result] (gen-pwd data))
                           (.preventDefault %))}
     "Generate Random Password"]]
     [:div {:class "form-group"}
      [:div {:class "alert alert-success" 
             :id "rnd-pwd"} 
       (:result @data)]]])

(defn password-generator [data & [path]]
  (let [path           (or path [])]
    [:form {:class "form"}
     (min-max-textbox "Minimum Length for entire Password" 
                      :upgradingdave.pass/min-length
                      data [:pwd-conf])
     (min-max-textbox "Maximum Length for entire Password"
                      :upgradingdave.pass/max-length
                      data [:pwd-conf])
     (class-control :upgradingdave.pass/lowercase data [:pwd-conf])
     (class-control :upgradingdave.pass/uppercase data [:pwd-conf])
     (class-control :upgradingdave.pass/digits data [:pwd-conf])
     (class-control :upgradingdave.pass/symbols data [:pwd-conf])]))

(def data (r/atom {:pwd-conf p/pwd-conf-example
                   :result ""}))

(defn main []
  ;;(create-password-generator! data)
  (if-let [node (.getElementById js/document "pwd-gen")]
    (r/render-component [password-generator data] node)))

(main)
