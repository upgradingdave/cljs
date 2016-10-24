(ns up.passwd.spec.dev
  (:require
   [devcards.core       :as dc]
   [reagent.core        :as r]
   [up.pass             :as p]
   [up.passwd.spec.core :as sp])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(def upper-class     
  {:up.pass/class :up.pass/uppercase 
   :up.pass/min   2 
   :up.pass/valid p/char-uppers
   :up.pass/enabled? true
   :up.pass/desc "Require Upper Case Characters?"})

(def lower-class
  {:up.pass/class :up.pass/lowercase 
   :up.pass/min   2 
   :up.pass/valid p/char-lowers
   :up.pass/enabled? true
   :up.pass/desc "Require Lower Case Characters?"})

(def digits-class
  {:up.pass/class :up.pass/digits
   :up.pass/min   2 
   :up.pass/valid p/char-digits
   :up.pass/desc "Require Numbers?"})

(def symbols-class
  {:up.pass/class :up.pass/symbols   
   :up.pass/min   2 
   :up.pass/valid p/char-symbols
   :up.pass/desc "Require Symbols?"})

(def pwd-conf1
  {:up.pass/min-length      10
   :up.pass/max-length      15
   :up.pass/valid-char-classes
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
  (dc/reagent (fn [data _] (sp/class-checkbox :up.pass/lowercase 
                                             data [:pwd-conf])))
  data
  {:inspect-data false})

(defcard 
  "### class min textbox"
  (dc/reagent (fn [data _] (sp/class-min-textbox
                            :up.pass/lowercase 
                            data [:pwd-conf])))
  data
  {:inspect-data false})

(defcard 
  "### min textbox"
  (dc/reagent (fn [data _] 
                [:form
                 (sp/min-max-textbox "Minimum Length" 
                                     :up.pass/min-length
                                     data [:pwd-conf])
                 (sp/min-max-textbox "Maximum Length" 
                                     :up.pass/max-length
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
    (is (= lower-class(sp/find-class pwd-conf1 :up.pass/lowercase)))
    (is (sp/class-enabled? pwd-conf1 :up.pass/lowercase))
    (is (not (sp/class-enabled? pwd-conf1 :up.pass/digits)))
    (is (sp/class-enabled? 
         (sp/enable-class pwd-conf1 :up.pass/digits)
         :up.pass/digits))
    (is (not (sp/class-enabled? 
              (sp/disable-class pwd-conf1 :up.pass/digits)
              :up.pass/digits))))
  (testing "format"
    (is (map? (sp/format-pwd-conf pwd-conf1))))
  (testing "class min"
    (is (map? (sp/update-class-min pwd-conf1 
                                   :up.pass/lowercase 10)))))

(devcards.core/start-devcard-ui!)

