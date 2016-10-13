(ns upgradingdave.spec-pwd-dev
  (:require
   [devcards.core            :as dc]
   [reagent.core             :as r]
   [upgradingdave.pass       :as p]
   [upgradingdave.spec-pwd   :as sp])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(def upper-class     
  {:upgradingdave.pass/class :upgradingdave.pass/uppercase 
   :upgradingdave.pass/min   2 
   :upgradingdave.pass/valid p/char-uppers
   :upgradingdave.pass/enabled? true
   :upgradingdave.pass/desc "Require Upper Case Characters?"})

(def lower-class
  {:upgradingdave.pass/class :upgradingdave.pass/lowercase 
   :upgradingdave.pass/min   2 
   :upgradingdave.pass/valid p/char-lowers
   :upgradingdave.pass/enabled? true
   :upgradingdave.pass/desc "Require Lower Case Characters?"})

(def digits-class
  {:upgradingdave.pass/class :upgradingdave.pass/digits
   :upgradingdave.pass/min   2 
   :upgradingdave.pass/valid p/char-digits
   :upgradingdave.pass/desc "Require Numbers?"})

(def symbols-class
  {:upgradingdave.pass/class :upgradingdave.pass/symbols   
   :upgradingdave.pass/min   2 
   :upgradingdave.pass/valid p/char-symbols
   :upgradingdave.pass/desc "Require Symbols?"})

(def pwd-conf1
  {:upgradingdave.pass/min-length      10
   :upgradingdave.pass/max-length      15
   :upgradingdave.pass/valid-char-classes
   [upper-class
    lower-class
    digits-class
    symbols-class]})

(def data (r/atom {:pwd-conf pwd-conf1
                   :result ""}))

(sp/create-password-generator! data)
(js/console.log (sp/gen-pwd data))

(defcard 
  "### class checkbox"
  (dc/reagent (fn [data _] (sp/class-checkbox :upgradingdave.pass/lowercase 
                                             data [:pwd-conf])))
  data
  {:inspect-data false})

(defcard 
  "### class min textbox"
  (dc/reagent (fn [data _] (sp/class-min-textbox
                            :upgradingdave.pass/lowercase 
                            data [:pwd-conf])))
  data
  {:inspect-data false})

(defcard 
  "### min textbox"
  (dc/reagent (fn [data _] 
                [:form
                 (sp/min-max-textbox "Minimum Length" 
                                     :upgradingdave.pass/min-length
                                     data [:pwd-conf])
                 (sp/min-max-textbox "Maximum Length" 
                                     :upgradingdave.pass/max-length
                                     data [:pwd-conf])]))
  data
  {:inspect-data false})

(defcard 
  "### password form"
  (dc/reagent (fn [data _] 
                (sp/password-form data)))
  data
  {:inspect-data false})

(defcard 
  "### Random Password Generator

A [Random Password Generator]() using `clojure.spec` written in
clojurescript using devcards"
  (dc/reagent (fn [data _] [sp/password-generator data]))
  data
  {:inspect-data true})

(deftest basic
  (testing "classes"
    (is (= lower-class(sp/find-class pwd-conf1 :upgradingdave.pass/lowercase)))
    (is (sp/class-enabled? pwd-conf1 :upgradingdave.pass/lowercase))
    (is (not (sp/class-enabled? pwd-conf1 :upgradingdave.pass/digits)))
    (is (sp/class-enabled? 
         (sp/enable-class pwd-conf1 :upgradingdave.pass/digits)
         :upgradingdave.pass/digits))
    (is (not (sp/class-enabled? 
              (sp/disable-class pwd-conf1 :upgradingdave.pass/digits)
              :upgradingdave.pass/digits))))
  (testing "format"
    (is (map? (sp/format-pwd-conf pwd-conf1))))
  (testing "class min"
    (is (map? (sp/update-class-min pwd-conf1 
                                   :upgradingdave.pass/lowercase 10)))))

(devcards.core/start-devcard-ui!)

