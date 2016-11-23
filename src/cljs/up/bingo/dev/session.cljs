(ns up.bingo.dev.session
  (:require
   [devcards.core       :as dc :include-macros true]
   [reagent.core        :as r]
   [up.bingo.core       :as b]
   [up.bingo.components :as c]
   [up.cookies.core     :as cookie])
  (:require-macros
   [devcards.core :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(deftest unit-tests
  (testing "Session"
    (is (cookie/cookie-exists? :bingo))

    (is (= "3f94ee63-35da-4b02-9362-852cbd69679c" 
           (:sessionid (cookie/get-cookie :bingo))))))

(def !state (r/atom {}))

(defcard 
  "### init!"
  (dc/reagent 
   (fn [data _]
     [:div 
      [:div "If the `init` function is uncommented below, then the
      board should display and should persist between browser
      refresh. "]

      [c/board !state [:bingo :board] 
       {:cell-width  35
        :cell-height 35
        :font-size   5
        :gutter-size 2
        :click-fn #(b/save-board! data [:bingo] 
                                  (get-in @data [:bingo :board]))}]])) 
  
  !state 
  {:inspect-data true})

(defn main []
  ;;(b/init! !state [:bingo])
  )

