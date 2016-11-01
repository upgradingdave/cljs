(ns up.form.spec-form-validation-dev
  (:require
   [devcards.core                :as dc]
   [reagent.core                 :as r]
   [up.form.spec-form-validation :as sfv])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(def data (r/atom {:digit-field {:field/label "Digit"
                                 :field/value "0"}}))

(defcard 
  "### Digit Input Textbox Field"
  (dc/reagent (fn [data _] [sfv/my-form data]))
  data
  {:inspect-data true})

(deftest unit-tests
  (testing "Basic"
    (is (= true true))))

(devcards.core/start-devcard-ui!)

