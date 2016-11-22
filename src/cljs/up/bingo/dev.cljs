(ns up.bingo.dev
  (:require
   [devcards.core  :as dc :include-macros true]
   [reagent.core   :as r]
   [up.bingo.core  :as b]
   [up.bingo.css   :as css]
   [up.bingo.aws   :as aws]
   [up.datetime    :as dt]
   [up.cookies.core :as c]
   [cljs.core.async :refer [put! chan <! >! close!]])
  (:require-macros
   [devcards.core :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]
   [cljs.core.async.macros :refer [go go-loop]]))

(def bingo-path [:bingo])

;; (deftest unit-tests
;;   (testing "Board and Cells"
;;     (is (= 25 (count (b/make-board (take 25 b/possible)))))
;;     (is (= "2px"
;;            (:top (first (b/resize-board 
;;                          (b/make-board (range 25)) 25 25 2))))))

;;   (testing "Session"
;;     (is (c/cookie-exists? :bingo))
;;     (is (= "3f94ee63-35da-4b02-9362-852cbd69679c" 
;;            (:sessionid (c/get-cookie :bingo)))))




(defn main []
  (b/init! b/!state bingo-path))


