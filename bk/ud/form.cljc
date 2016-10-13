(ns ud.form
  (:require #?(:clj  [clojure.spec       :as s]
               :cljs [cljs.spec          :as s])))

;; Field spec
(s/def ::field
  (s/keys :req [:field/value]
          :opt [:field/label 
                :field/valid? 
                :field/error
                :field/spec]))

(defn atom? [i]
  #?(:clj  (instance? java.lang.Atom i)
     :cljs (instance? cljs.core.Atom i)))

;; A atom-ref is a pair that contains an atom and a path to an atom
(s/def ::atom-ref
  (s/cat :atom atom? :path string?))

(defn parse-digit 
  "Parse a digit inside a input text field"
  [field]
  (let [v (get field ::value)]
    (cond 

      (and (string? v) (re-matches #"\d+" v)) 
      #?(:clj  (Integer/parseInt v)
         :cljs (js/parseInt v))

      (int? v)
      v)))

(s/fdef parse-digit :args (s/cat :digitbox ::field))

(defn digitbox-in-range? 
  "Ensure digit inside input text field is within bounds"
  [start end field]
  (if-let [i (parse-digit field)] (s/int-in-range? start end i)))

(defn digitbox-gt? 
  "True if digitbox 'field1' is greater than digitbox 'field2'"
  [field1 field2]
  (let [a (parse-digit field1) 
        b (parse-digit field2)]
    (if (and a b)
      (> a b))))

(s/def ::digit? parse-digit)

