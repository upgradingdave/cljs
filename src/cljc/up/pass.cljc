(ns up.pass
  (:require #?(:clj  [clojure.spec       :as s]
               :cljs [cljs.spec          :as s])
            #?(:clj  [clojure.spec.gen   :as gen]
               :cljs [cljs.spec.impl.gen :as gen])
            [up.pass-cljs-macro :refer-macros [and']]))

(def char-lowers (into #{} (map char (range 97 122))))
(def char-lower? char-lowers)

(s/def ::two-lowers
  (s/and string? #(<= 2 (count (filter char-lower? (into #{} %))))))

(def char-uppers (into #{} (map char (range 65 91))))
(def char-upper? char-uppers)

(s/def ::two-uppers
  (s/and string? #(<= 2 (count (filter char-upper? (into #{} %))))))

(def char-digits (into #{} (map char (range 48 58))))
(def char-digit? char-digits)

(s/def ::two-digits
  (s/and string? #(<= 2 (count (filter char-digit? (into #{} %))))))

(def char-symbols #{\! \$ \^ \&})
(def char-symbol? char-symbols)

(def max-chars 1000)

(s/def ::max   (s/int-in 1 max-chars))
(s/def ::min   (s/int-in 0 max-chars))
(s/def ::class keyword?)
(s/def ::valid set?)

(s/def ::password-class
  (s/keys :req [::class 
                ::valid
                ::min]))

(defn create-class-spec
  [{min-count ::min valid-chars ::valid}]
  (s/and string? #(<= min-count (count (filter valid-chars (into #{} %))))))

(s/fdef create-class-spec
        :args (s/cat :password-class ::password-class))

(defn create-class-gen 
  "Create a character class generator based on class definition"
  [{min-count ::min valid-chars ::valid}]
  (gen/vector-distinct (gen/elements valid-chars) 
                       {:num-elements min-count}))

(s/fdef create-class-gen
        :args ::password-class)

(defn class-mins-total 
  "Take all the character class minimums and tally them up"
  [{classes ::valid-char-classes}]
  (reduce + (map ::min classes)))
  
;; This is a spec for password configs
;; TODO:  min required must be less than total
(s/def ::password-conf 
  (s/and
   (s/keys :req [::min-length 
                 ::max-length
                 ::valid-char-classes])
   ;; min-length must be at least as big as sum of minimums of various
   ;; char classes
   #(>= (::min-length %) (class-mins-total %))
   ;; total max length of password must be bigger than min length
   #(> (::max-length %) (::min-length %))
   ;; TODO: min required for any given class of chars must be less than the
   ;; number of valid characters
   ))

;; A password config defines how our password generator behaves,
;; here's an example
(def pwd-conf-example
  {::min-length      10
   ::max-length      15
   ::valid-char-classes
   [
    {::class ::uppercase ::min 2 ::valid char-uppers}
    {::class ::lowercase ::min 2 ::valid char-lowers}
    {::class ::symbols   ::min 2 ::valid char-symbols}
    {::class ::digits    ::min 2 ::valid char-digits}]
   })

(defn valid-chars [{classes ::valid-char-classes}]
  "List of valid characters"
  (into #{} (apply concat (map ::valid classes))))

(defn valid-chars-spec [pwd-conf]
  (fn [s] 
    (reduce #(and %1 %2) true (map (valid-chars pwd-conf) (into [] s)))))

(defn valid-chars-gen
  "Create a generator of valid characters"
  [pwd-conf & args]
  (apply gen/vector
         (concat [(gen/one-of 
                   [(gen/elements (valid-chars pwd-conf))])] args)))

(defn create-size-spec
  [pwd-conf]
  (let [{min-len ::min-length
         max-len ::max-length} pwd-conf]
    (s/and #(>= (count %) min-len) 
           #(<  (count %) max-len) 
           (valid-chars-spec pwd-conf))))

(defn gen-concat 
  "Concatenate several generators using s/fmap"
  [& gens]
  (gen/fmap (fn [gens] (apply str (shuffle (apply concat gens))))
            (apply gen/tuple gens)))

(defn create-password-gen
  "Create a password generator based on a pwd-conf. `class-gens` is a
  collection of generators to satisfy class specs and `size-gen` is a
  generator of extra chars in order to satisfy min-length/max-length"
  [pwd-conf]
  (let [{classes    ::valid-char-classes
         min-length ::min-length
         max-length ::max-length} pwd-conf 
        class-gens (map create-class-gen classes)
        len        (class-mins-total pwd-conf)
        size-gen   (valid-chars-gen pwd-conf 
                               (- min-length len) 
                               (- max-length len))]
    (apply gen-concat (conj class-gens size-gen))))

(defn create-password-spec [pwd-conf]
  (s/with-gen (and' (conj (mapv create-class-spec 
                                (::valid-char-classes pwd-conf))
                          (create-size-spec pwd-conf))) 
    #(create-password-gen pwd-conf)))

;;(def create-password-spec (memoize create-password-spec))

(defmulti create-password-fn map?)

(defmethod create-password-fn true [pwd-conf] 
 #(gen/generate (s/gen (create-password-spec pwd-conf))))

(defmethod create-password-fn false [pwd-spec] 
 #(gen/generate (s/gen pwd-spec)))
