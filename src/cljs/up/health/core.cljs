(ns up.health.core
  (:require [reagent.core     :as r]
            [up.validate.core :as v]
            [up.number        :as n]
            [up.units         :as u]
            [up.person.core   :as p]))

(def default-fat% 20)

;; map helpers

(defn assoc-all 
  "Update all nested fields with the same value. For example, say you
  need to update all :active keys to false:
  
  (assoc-all {:one {:active true}
              :two {:active true}}
             #(assoc-in % [:active] false))"
  [m f]
  (into {} (for [[k v] m] [k (f v)])))

;; equations

(defn harris-benedict 
  "Harris J, Benedict F. A biometric study of basal metabolism in man.
  Washington D.C. Carnegie Institute of Washington. 1919."
  [male? weight-kg height-cm age]
  (if male?
    (- (+ 66.5 (* 13.75 weight-kg) (* 5.003 height-cm))
       (* 6.775 age))
    (- (+ 655 (* 9.563 weight-kg) (* 1.850 height-cm))
       (* 4.676 age))))

(def exercise-levels 
  {:sedentary {:multiplier 1.2
               :desc "Little or no exercise"}
   :light     {:multiplier 1.375
               :desc "Light exercise (1-3 days/week)"}
   :moderate  {:multiplier 1.55
               :desc "Moderate exercise (3-5 days/week)"}
   :active    {:multiplier 1.725
               :desc "Hard exercise (6-7 days/week)"}
   :heavy     {:multiplier 1.9
               :desc "Very hard exercise & physical job or 2x training"}
   })

(defn tee
  "The energy (thermic) cost of exercise, TEE"
  [weight-kg mins-exercising]
  (let [weight-lifting-variable 0.86]
    (* weight-kg mins-exercising weight-lifting-variable)))

;; (defn reset-level-radios
;;   "Reset all checkboxes to false"
;;   [m key]
;;   (swap! m update-in [:exercise] assoc-all #(assoc-in % [:checked] false)))

(defn exercise-radio [data k]
  (let [{:keys [multiplier desc checked]} (get-in @data [:exercise k])]
    [:div {:class "radio"}
     [:label
      [:input {:type "radio" 
               :name "exerciseRadios" 
               :id "exerciseLight"
               :value multiplier
               :checked checked
               :on-change 
               #(let [v (-> % .-target .-value)]
                  ;; (reset-level-radios data [:exercise])
                  ;;(swap! data assoc-in [:exercise k :checked] true)
                  (swap! data update-in [:exercise] 
                         (fn [o] 
                           (-> o
                               (assoc-all (fn [o] 
                                            (assoc-in o [:checked] false)))
                               (assoc-in  [k :checked] true)
                               )))

                  (swap! data assoc-in [:level] (js/parseFloat v)))}]
      desc]]))

;; (defn bmr-calculator [data]
;;   [:form {:class "form-horizontal"}

;;    [age data]

;;    [gender-select data]

;;    [weight data]

;;    [height data]

;;    [:div {:class "form-group"}
;;     [:div {:class "col-sm-offset-1 col-sm-11 alert alert-success" 
;;            :id "bmr-result"} (str "Your BMR: " (calc-bmr @data))]]])

;; (defn energy-calculator [data]
;;   [:div
;;    (for [k (keys (:exercise @data))]
;;      ^{:key k}[exercise-radio data k])
;;     [:div {:class "alert alert-success" 
;;            :id "bmr-result"} 
;;      (str "Est. Calories Burned Per Day : " 
;;           (harris-benedict (get-in   @data [:level]) 
;;                            (calc-bmr @data)))]])

(defn body-fat-weight [weight fat%]
  "Calculate body fat weight given total weight and body fat percent"
  (* weight (* 0.01 fat%)))

(defn body-fat-form-group [data fat%-path]
  (let [{:keys [display errors]
         :or {display default-fat%}} 
        (get-in @data fat%-path)]
    [:div {:class (str "form-group" (if errors " has-error"))}
     [:label.control-label.col-sm-3.col-xs-3 "Body Fat Percent"]
     [:div.col-sm-3.col-xs-3
      [:div.input-group
       [:input.form-control 
        {:type "text"
         :value display
         :on-change (let [opts {:type :percent}] 
                      (v/validate! data fat%-path ))}]
       [:span.input-group-addon "%"]]]
     [:div.col-sm-3.col-xs-3 
      (if errors [:ul (for [e errors] [:li {:key (gensym)} e])])]]))

(defn lean-muscle [data path]
  [:div.form-horizontal

   [p/weight-form-group data (conj path :weight)]
   [body-fat-form-group data (conj path :fat%)]

   [:div.form-group
    [:label.control-label.col-sm-3.col-xs-3 "Body Fat Weight"]
    [:div.col-sm-3.col-xs-3
     [:div.input-group
      [:input.form-control 
       {:type "text"
        :readOnly true
        :value (let [f (get-in @data (conj path :fat% :value) default-fat%)
                     w (get-in @data (conj path :weight :value) 
                               p/default-weight)]
                 (n/format-decimal (body-fat-weight w f) 2))}]
      [:span.input-group-addon (get-in @data (conj path :weight :unit))]]]]
   [:div.form-group
    [:label.control-label.col-sm-3.col-xs-3 "Lean Mass"]
    [:div.col-sm-3.col-xs-3
     [:div.input-group
      [:input.form-control
       {:type "text"
        :readOnly true
        :value (let [f (get-in @data (conj path :fat% :value) default-fat%)
                     w (get-in @data (conj path :weight :value) 
                               p/default-weight)
                     t (body-fat-weight w f)]
                 (n/format-decimal (- w t) 2))}]
      [:span.input-group-addon "lbs"]]]]])

(defn page [data path]
  [:div "coming soone"])

(def data (r/atom {}))

(defn main []
  (if-let [node (.getElementById js/document "bmr-calc")]
    (r/render-component [page data] node)))

