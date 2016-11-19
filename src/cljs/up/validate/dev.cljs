(ns up.validate.dev
  (:require
   [devcards.core    :as dc :include-macros true]
   [reagent.core     :as r]
   [up.validate.core :as v])
  (:require-macros
   [devcards.core :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

;; (def alerts-path [:alerts])
;; (def notify-path [:notifications])

(def data (r/atom {}))

(defcard 
  "### Positive, Required, Decimal"
  (dc/reagent 
   (fn [data _]
     (let [path [:ext2] {:keys [errors display] :as field} (get-in @data path)]
       [:div {:class (str "form-group" (if (v/has-errors? field) " has-error"))}
        [:input.form-control 
         {:type "text"
          :value display
          :on-change (v/validate! data path 
                                  (v/required)
                                  (v/number) 
                                  (v/positive))}]])))
  (r/atom {})
  {:inspect-data true})

(defcard 
  "### Positive Decimal"
  (dc/reagent 
   (fn [data _]
     (let [path [:ext2] {:keys [errors display] :as field} (get-in @data path)]
       [:div {:class (str "form-group" (if (v/has-errors? field) " has-error"))}
        [:input.form-control 
         {:type "text"
          :value display
          :on-change (v/validate! data path 
                                  (v/number) 
                                  (v/positive))}]])))
  (r/atom {})
  {:inspect-data true})

(defcard 
  "### Decimal"
  (dc/reagent 
   (fn [data _]
     [:input.form-control 
      {:type "text"
       :value (get-in @data [:ex2 :display])
       :on-change (v/validate! data [:ex2] (v/number))}]))
  (r/atom {})
  {:inspect-data true})

(defcard 
  "### No Validation"
  (dc/reagent 
   (fn [data _]
     [:input.form-control 
      {:type "text"
       :value (get-in @data [:ex1 :display])
       :on-change (v/validate! data [:ex1])}]))
  (r/atom {})
  {:inspect-data true})

(deftest unit-tests
  (testing "number"
    (is (number? 1.01))
    (is (= 1.0 (v/parse-number "1.0")))
    (is (= 1 (v/parse-number "1.")))
    (is (= 1 (v/parse-number "1")))
    (is (= 1 (v/parse-number 1)))
    (is (= 1.01 (v/parse-number 1.01)))
    (is (= 1 (v/parse-number "00001")))
    (is (nil? (v/parse-number "1a")))
    (is (nil? (v/parse-number "1a.0"))))
  (testing "errors"
    (is (v/valid? {:errors {:number nil}}))
    (is (v/has-errors? {:errors {:number "hmm" :valid nil}}))))

;; (defn init! [data]
;;   (js/console.log "Initializing notifications ...")
;;   (n/notification-init! data notify-path))

;; (defn main []
;;   (init! ex1)
;;   (dc/start-devcard-ui!))
