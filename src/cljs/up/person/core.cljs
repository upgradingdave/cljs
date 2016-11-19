(ns up.person.core
  (:require [up.event :as e] 
            [up.units :as u]
            [reagent.core :as r]))

(def default-weight-unit "lb")
(def default-weight      "150")
(def default-gender      "female")
(def default-age         30)
(def default-feet        5)
(def default-inches      10)
(def default-cm          178)
(def default-height-unit "ft")

;; gender 

(defn gender-form-group
  "A gender select drop down"
  [data gender-path]
  [:div.form-group
   [:label.control-label.col-sm-3.col-xs-3 "Gender"]
   [:div.col-sm-3.col-xs-3
    [:select.form-control 
     {:value (get-in @data gender-path default-gender)
      :on-change (e/on-change! data gender-path)}
     [:option {:value "female" :key "female"} "Female"]
     [:option {:value "male"   :key "male"}   "Male"]]]])

;; weight 

(defn update-weight-units
  "Whenever weight is changed, recalculate lbs and kgs"
  [{:keys [unit value] :or {unit default-weight-unit} :as weight}]
  (if (= "lb" unit)
    (-> weight
        (assoc :lb value)
        (assoc :kg (u/lb->kg value)))
   (-> weight
        (assoc :lb (u/kg->lb value))
        (assoc :kg value))))

(defn weight-unit-select 
  "A select drop down to choose between pounds or kilograms"
  [data weight-path]
  (let [unit-path (conj weight-path :unit)]
    [:select {:class "form-control"
              :value (get-in @data unit-path default-weight-unit)
              :on-change #(let [new-val (-> % .-target .-value)]
                            (swap! data update-in weight-path
                                   (fn [o]
                                     (-> o 
                                         (assoc :unit new-val)
                                         (update-weight-units)))))}
     [:option {:value "lb" :key "lb"} "Pounds"]
     [:option {:value "kg" :key "kg"} "Kilograms"]]))

(defn weight-form-group
  "A form group for entering weight in pounds or kilograms"
  [data weight-path]
  (let [val-path (conj weight-path :value)
        error    (get-in @data (conj weight-path :error))]
    [:div {:class (str "form-group" (if error " has-error"))}
     [:label.control-label.col-sm-3.col-xs-3 "Weight"]
     [:div.col-sm-3.col-xs-3
      [:input.form-control 
       {:type "text" :value (get-in @data val-path default-weight)
        :placeholder error
        :on-change #(let [new-val (-> % .-target .-value)]
                      (cond
                        
                        (empty? new-val)
                        (swap! data assoc-in weight-path {})
                        
                        (js/isNaN new-val)
                        (swap! data assoc-in (conj weight-path :error)
                               "Please enter a valid number" )

                        :else
                        (swap! data update-in weight-path
                               (fn [o]
                                 (-> o 
                                     (assoc :value (js/parseInt new-val))
                                     (assoc :error nil)
                                     (update-weight-units))))))}]]
     [:div.col-sm-2.col-xs-2
      [weight-unit-select data weight-path]]]))

;; age 

(defn age-form-group [data age-path]
  [:div {:class "form-group"}
   [:label.control-label.col-sm-3.col-xs-3 "Age"]
   [:div.col-sm-2.col-sx-2
    [:select {:class "form-control"
              :value (get-in @data age-path default-age)
              :on-change (e/on-change! data age-path)}
     (map #(vector :option {:value (str %) :key (str %)} (str %)) 
          (range 1 100))]]])


;; height

(defn update-height-units
  "Whenever height is changed, recalculate feet, inches, and centimeters"
  [{:keys [unit ft in cm] 
    :or   {unit default-height-unit
           ft   default-feet
           in   default-inches
           cm   default-cm} :as height}]
  (if (= "ft" unit)
    (-> height
        (assoc :cm (u/to-cm ft in)))

    (let [in              (u/cm->in cm) 
          {:keys [ft in]} (u/in->ftin in)]
      (-> height
          (assoc :ft ft)
          (assoc :in in)))))

(defn update-height! [data height-path k int?]
  (fn [e]
    (let [new-val (-> e .-target .-value)]
      (swap! data update-in height-path
             (fn [o]
               (-> o 
                   (assoc k (if int? (js/parseInt new-val) new-val))
                   (update-height-units)))))))

(defn feet-select [data height-path]
  (let [feet-path (conj height-path :ft)]
    [:select {:class "form-control"
              :value (get-in @data feet-path default-feet)
              :on-change (update-height! data height-path :ft true)}
     (map #(vector :option {:value (str %) :key (str %)} (str % " feet")) 
          (range 1 8))]))

(defn inch-select [data height-path]
  (let [inch-path (conj height-path :in)]
    [:select {:class "form-control"
              :value (get-in @data inch-path default-inches)
              :on-change (update-height! data height-path :in true)}
     (map #(vector :option {:value (str %) :key (str %)} (str % " inches")) 
          (range 1 12))]))

(defn height-radio-group [data height-path]
  (let [v (get-in @data (conj height-path :unit) default-height-unit)]
    [:div.form-group
     [:div.radio-inline
      [:label
       [:input {:type "radio"
                :name "height-radios"
                :key   "ft"
                :value "ft"
                :checked (= "ft" v)
                :on-change (update-height! data height-path :unit false)}]
       "Feet & Inches"]]
     [:div.radio-inline
      [:label
       [:input {:type "radio"
                :name "height-radios"
                :key   "cm"
                :value "cm"
                :checked (= "cm" v)
                :on-change (update-height! data height-path :unit false)}]
       "Centimeters"]]]))

(defn height-form-group [data height-path]
  [:div
   [height-radio-group data [:person :height]]
   (let [unit  (get-in @data (conj height-path :unit) default-height-unit)
         cm    (get-in @data (conj height-path :cm)   default-cm)
         error (get-in @data (conj height-path :error))]
     (if (= "ft" unit)
       [:div.form-group
        [:div.col-sm-3.col-xs-3
         [feet-select data [:person :height]]]
        [:div.col-sm-3.col-xs-3
         [inch-select data [:person :height]]]]
       [:div.form-group
        [:div.col-sm-3.col-xs-3
         [:div.input-group
          [:input.form-control 
           {:type "text" :value cm
            :placeholder error
            :on-change #(let [new-val (-> % .-target .-value)]
                          (cond
                            
                            (empty? new-val)
                            (swap! data assoc-in height-path {})
                            
                            (js/isNaN new-val)
                            (swap! data assoc-in (conj height-path :error)
                                   "Please enter a valid number" )

                            :else
                            (swap! data update-in height-path
                                   (fn [o]
                                     (-> o 
                                         (assoc :cm (js/parseInt new-val))
                                         (update-height-units))))))}]
          [:span.input-group-addon "cm"]]]]))])
