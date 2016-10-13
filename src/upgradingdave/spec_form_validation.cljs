(ns upgradingdave.spec-form-validation
  (:require [reagent.core       :as r]
            [cljs.spec          :as s]
            [ud.form            :as f]))

(def css-transition-group
  (r/adapt-react-class js/React.addons.CSSTransitionGroup))

(def transition-in  0.25)
(def transition-out 0.5)

(def style 
  (str 
   ".msg-enter {
      opacity: 0.01;
      max-height: 0px;
    }

    .msg-enter-active {
      opacity: 1;
      max-height: 999px;
      -moz-transition: all " transition-in "s ease-in-out;
      -webkit-transition: all " transition-in "s ease-in-out;
      -ms-transition: all " transition-in "s ease-in-out;
      -o-transition: all " transition-in "s ease-in-out;
      transition: all " transition-in "s ease-in-out;
    }

    .msg-leave {
      opacity: 1;
      max-height: 999px;
    }

    .msg-leave-active {
      opacity: 0.1;
      max-height: 1px;
      -moz-transition: all " transition-out "s ease-in-out;
      -webkit-transition: all " transition-out "s ease-in-out;
      -ms-transition: all " transition-out "s ease-in-out;
      -o-transition: all " transition-out "s ease-in-out;
      transition: all " transition-out "s ease-in-out;
  }"))

(def data (r/atom {}))

(defn parse-int [s]
  (js/parseInt s))

;; user friendly error messages for specs
(def messages {::number "Must be a valid number"
               ::min-max "Max must be larger than Min"
               ::max-range "Must be between 2 and 30"
               ::min-range "Must be between 0 and 10"})

;; text field validations
;; (s/def ::min-range #(f/digitbox-in-range? 0 10 %))
;; (s/def ::max-range #(f/digitbox-in-range? 5 30 %))
;; (s/def ::min        (s/and :bootstrap.textbox/digit? ::min-range))
;; (s/def ::max        (s/and :bootstrap.textbox/digit? ::max-range))

;; ;; form validations
;; (s/def ::min-max #(f/digitbox-gt? (get-in % [::max]) (get-in % [::min])))

;; (s/def ::my-form (s/and 
;;                   ::min-max
;;                   (s/keys :req [::min ::max])))

(defn prob-to-string 
  "Return message for coresponding" [prob]
  (get messages prob (str "Hmm, we have some problems related to: '" 
                          (name prob)  "'")))

(defn problems 
  "Get a list of the 'via' values out of an explain-data data structure"
  [e]
  (flatten (map (fn [[_ {via :via}]] via) (get-in e [:cljs.spec/problems]))))

(defn known-problems 
  "Filter any problems without user friendly messages"
  [probs]
  (filter #(not (nil? (get messages %))) probs))

(defn problems-list [probs]
  [:ul {:class "list-unstyled"}
   (for [p probs]
     [:li {:key (name p)} (prob-to-string p)])])


;; (defn before-form-update! 
;;   "Check the form's state against a spec"
;;   [data path-to-data path-to-validation spec]
;;   #(if (s/valid? spec (get-in @data path-to-data))
;;     (do
;;       (swap! data assoc-in (conj path-to-validation :valid) true)
;;       (swap! data assoc-in (conj path-to-validation :error) nil))
;;     (do
;;       (swap! data assoc-in (conj path-to-validation :valid) false)
;;       (swap! data assoc-in (conj path-to-validation :error)
;;              (s/explain-data spec (get-in @data path-to-data))))))

;; (defn on-form-submit! [data path]
;;   #(do (swap! data assoc-in 
;;               (conj path :validation :message) 
;;               (str "If this form actually did "
;;                    "anything, it'd would've just "
;;                    "done it!"))
;;        (js/setTimeout
;;         (fn [] (swap! data assoc-in 
;;                       (conj path :validation :message) 
;;                       nil)) 1500)))

;; (defn my-form 
;;   [data & [path]]
;;   (let [path (or path [])]   
;;     (r/create-class
;;      {:component-will-update
;;       (let [path-to-data (conj path :fields)
;;             path-to-validation (conj path :validation)]
;;         (before-form-update! data path-to-data path-to-validation ::min-max))

;;       :display-name  "my-form"

;;       :reagent-render 
;;       (fn [data & [path]]
;;         (let [path   (or path [])
;;               st     (get-in @data path)
;;               valid? (get-in @data (conj path :validation :valid))
;;               error  (get-in @data (conj path :validation :error))
;;               errors (problems error)
;;               message (get-in @data (conj path :validation :message))]
;;           [:form {:class "form"}
;;            [:style style]
;;            [:div {:class "container"}
;;             [:div {:class "row" :style {:height "100px"}}
;;              [:div {:class "col-xs-3"}
;;               [digit-input [data (conj path :fields ::min)] 
;;                {:label "Min" :spec ::min}]]
;;              [:div {:class "col-xs-3"}
;;               [digit-input [data (conj path :fields ::max)] 
;;                {:label "Max" :spec ::max}]]]

;;             [:div {:class "help-block with-errors"
;;                    :style {:height "20px"}}
;;              [:div
;;               (if error
;;                 [problems-list errors])
;;               [css-transition-group 
;;                {:transition-name "msg"
;;                 :transition-enter-timeout (* transition-in 1000)
;;                 :transition-leave-timeout (* transition-out 1000)}
;;                (if (and message (not error)) [:div message])]]]

;;             [:div {:class "form-group"}
;;              [:button {:type "submit"
;;                        :class (str "btn btn-primary"
;;                                    (if (not valid?) " disabled") )
;;                        :on-click (on-form-submit! data path)} 
;;               "Submit"]]]
;;            ])
;;         )})))

(defn main []
  (if-let [node (.getElementById js/document "spec-form")]
    (r/render-component [my-form data] node)))

(main)

