(ns up.css.dev
  (:require
   [devcards.core   :as dc :include-macros true]
   [reagent.core    :as r]
   [up.css.core     :as c]
   [cljsjs.react-flip-move]
   [re-frame.core :refer [dispatch-sync
                          dispatch
                          subscribe
                          reg-event-db 
                          reg-sub]])
  (:require-macros
   [devcards.core :refer [defcard deftest defcard-doc]]
   [cljs.test     :refer [is testing]]))

(def bg-img
  {:width               "75px"
   :height              "75px" 
   :border-radius       "4px"
   :color               "#444"
   :background-image    "url(\"/public/i/clojure-logo.png\")"
   :background-size     "contain"
   :background-repeat   "no-repeat"
   :background-position "center"
   })

(defn make-class [class-name styles]
  (str "." (name class-name) " {"
       (apply str (map (fn [[k v]] (str (name k) ": " v ";\n")) styles))
       "}\n"))

(defn make-styles [styles]
  (apply str
         (map (fn [[k v]] (make-class k v)) styles)))

(deftest unit-tests
  (testing "CSS classes"
    (is (= ".foo {color: red;\n}\n"
           (make-class "foo" {:color "red"})))))

(reg-sub ::style.ex1.img (fn [db _] (::style.ex1.img db)))

(def flip-move (r/adapt-react-class js/FlipMove))

(defcard 
  "## Boxes"
  (dc/reagent 
   (fn [data _]
     [:div
      [flip-move {:style {:display "flex"
                     :flex-direction "row"
                     :flex-wrap "wrap"
                     :justify-content "space-around"
                     :align-items "center"
                     :padding 0
                     :maring 0
                     :background-color "#edf6fa"}}
       (map (fn [[k v]] (vector :div {:key k
                                      :style {:height "80px"
                                              :line-height "50px"
                                              :margin "10px"
                                              :padding "15px"
                                              :text-align "center"
                                              :font-size "2em"
                                              :border-radius "8px"
                                              :background-color "#5cc70c"}} v)) 
            (:list @data))]
      [:button.btn.btn-primary 
       {:on-click #(swap! data update-in 
                          [:list] (fn [s] 
                                    (into {} (shuffle (seq s)))))}
       "Shuffle"]]))
  (r/atom {:list {0 "Cat"
                  1 "Dog"
                  2 "Mouse"}})
  {:inspect-data true})

(defcard 
  "## List animation!"
  (dc/reagent 
   (fn [data _]
     [:div
      [:ul 
       [flip-move 
        (map (fn [[k v]] (vector :li {:key k} v)) (:list @data))]]
      [:button.btn.btn-primary 
       {:on-click #(swap! data update-in 
                          [:list] (fn [s] 
                                    (into {} (shuffle (seq s)))))}
       "Shuffle"]]))
  (r/atom {:list {0 "Cat"
                  1 "Dog"
                  2 "Mouse"}})
  {:inspect-data true})

(defcard 
  "## Simple Transitions (Try Hovering)"
  (dc/reagent 
   (fn [data _]
     [:div
      [:style (make-styles 
               {"ex1" (merge bg-img {:transition "all 1s ease"})
                "ex1:hover" {:opacity 0}
                "ex2" (merge bg-img {:transition "all 5s ease"})
                "ex2:hover" {:opacity 0}
                "ex3" (merge bg-img {:transition "all 2s ease"})
                "ex3:hover" {:height 0
                             :width  0}
                })]
      [:div.ex1]
      [:div.ex2]
      [:div.ex3]]))
  re-frame.db/app-db
  {:inspect-data true})

(defcard 
  "## Animate.css"
  (dc/reagent 
   (fn [data _]
     [:div [:span.animated.infinite.swing {:class "glyphicon glyphicon-hourglass"}]]))
  (r/atom {})
  {:inspect-data false})

(defn main []
  (c/main))

(defn reload [])

