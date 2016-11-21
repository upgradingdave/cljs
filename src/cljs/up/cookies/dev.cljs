(ns up.cookies.dev
  (:require
   [devcards.core   :as dc :include-macros true]
   [reagent.core    :as r]
   [up.cookies.core :as c])
  (:require-macros
   [devcards.core :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(deftest unit-tests
  (testing "Cookies"
    (is (= false   (c/cookie-exists? :dne)))
    (is (= true    (c/cookie-exists? :devcards)))
    (is (= "hello" (:message (c/get-cookie :devcards))))
    (is (= 36 (count (str (random-uuid)))))))

(defn main 
  "Create a cookie for testing"
  []
  (when (not (c/cookie-exists? :devcards))
    (c/set-cookie! :devcards {:message "hello"})))

