(ns upgradingdave.compiler
  (:require 
   [cljs.tools.reader :as reader]
   [cljs.js           :as cljs]
   [devcards.core     :as dc]
   [sablono.core      :as sab :include-macros true])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-doc]]))

(def st (cljs/empty-state))

(defn eval [code-str]
  (cljs/eval-str 
   st code-str 'dave.runtime
   {:eval cljs/js-eval :source-map true :ns 'fiddle.runtime}
   (fn [{:keys [error value]}]
     (if-not error
       value
       (js/console.error error)))))

(def code1 (atom "(+ 2 2)"))

(defcard simple-test 
  (sab/html [:form {:class "form-horizontal"}
             [:div {:class "form-group"}
              [:label {:class "col-sm-3 control-label"} "Some Code"]
              [:div {:class "col-sm-9"}
               [:input {:class "form-control" 
                        :type "text" :value @code1
                        :on-change #(let [new-val (-> % .-target .-value)]
                                      (reset! code1 new-val))}]]]
             [:div {:class "form-group"}
              [:div {:class "col-sm-offset-3 col-sm-9"}
               [:button {:class "btn btn-default"
                         :on-click 
                         #(let [el (.getElementById js/document "display")]
                            (set! (. el -innerHTML) 
                                  (eval @code1)))}
                "Read"]]]
             [:div {:class "form-group"}
              [:h1 {:class "col-sm-offset-3 col-sm-9" 
                    :id "display"} 
               (eval @code1)]]]))
