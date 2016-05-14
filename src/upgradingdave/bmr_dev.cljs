(ns upgradingdave.bmr-dev
  (:require
   [devcards.core     :as dc]
   [reagent.core      :as r]
   [upgradingdave.bmr :as bmr])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(defcard-doc 
  "### The Mifflin St Jeor BMR Equation

  `For men:   BMR = 10 x weight (kg) + 6.25 x height (cm) – 5 x age (years) + 5`

  `For women: BMR = 10 x weight (kg) + 6.25 x height (cm) – 5 x age (years) – 161`

  ### Age Selectbox

  First, we need a select box for age.

  "
  (dc/mkdn-pprint-source bmr/age)

  "This is a select box that updates the `:age` key inside the
  `data` atom whenever the user selects a new value from the drop down.

   ### Gender Select Box
   
   The Gender Select Box is very similar:
   
   "
   (dc/mkdn-pprint-source bmr/gender-select)

   "### Weight Input Fields
  
    Weight is a little more complex. First, we need a select box to
    select pounds or kilograms. 

   "

   (dc/mkdn-pprint-source bmr/weight-select)
   
   "And let's combine the select box with an input text field: 

   "

   (dc/mkdn-pprint-source bmr/weight)

   "### Height Field 

    Now all we need is somewhere to enter height. I chose to use two
    select drop downs: one for feet and one for inches. The code for
    both is very similar. These two select boxes are slightly more
    complicated since they calculate the height in centimeters
    whenever the fields are updated. 

   "
   
   (dc/mkdn-pprint-source bmr/feet-select)

   (dc/mkdn-pprint-source bmr/inch-select)

   "Here's the code for `to-cm`"

   (dc/mkdn-pprint-source bmr/to-cm)

  "Let's test `to-cm` real quick: ")

(deftest height-to-centimeters
  (testing "testing cm"
    (is (= 187.96 (bmr/to-cm 6 2)))))

(defcard-doc
  "### Calculating BMR

   First, we'll need a few helper functions to switch between lbs and kgs:

   " 
   (dc/mkdn-pprint-source bmr/lb->kg)

   (dc/mkdn-pprint-source bmr/kg->lb)

   "Let's make sure they are working correctly (Note that we lose a
   little precision converting back and forth).")

(deftest kilograms-to-pounds
  (testing "testing 215 lbs" (is (= 98  (Math/round (bmr/lb->kg 215     ))))
                             (is (= 215 (Math/round (bmr/kg->lb 97.52228)))))
  (testing "testing 50 kg"   (is (= 110 (Math/round (bmr/kg->lb 50      ))))
                             (is (= 50  (Math/round (bmr/lb->kg 110.231))))))

(defcard-doc 
  "Now, we can calculate BMR!"
  
  (dc/mkdn-pprint-source bmr/calc-bmr)

  "Let's make sure it works:")

(deftest bmr-test
  (testing "testing male bmr"
    (is (= 1974.9728 (bmr/calc-bmr {:gender "m"
                                :weight {:value 215 :unit "lbs"} 
                                :height {:ft 6 :in 2 :cm (bmr/to-cm 6 2)}
                                :age 36}))))
  (testing "testing female bmr"
    (is (= 1552.286 (bmr/calc-bmr {:gender "f"
                               :weight {:value 175 :unit "lbs"} 
                               :height {:ft 5 :in 8 :cm (bmr/to-cm 5 8)}
                               :age 32})))))

(defcard-doc 
  "### Putting it all together

  All we need to do now is combine all the pieces into a calculator.

  "
  (dc/mkdn-pprint-source bmr/bmr-calculator)

  "... and here's how to initialize it onto the page"

  (dc/mkdn-pprint-source bmr/data)
  (dc/mkdn-pprint-source bmr/main))

(defcard
  "### Basal Metabolic Rate (BMR) Calculator 

   A [BMR Calculator](/blog/posts/2016-01-11-bmr-calc.html) written
   in clojurescript using devcards
"
  (dc/reagent 
   (fn [data _] [bmr/bmr-calculator data]))
  (r/atom {:gender "m"
           :weight {:value 220 :unit "lbs"} 
           :height {:ft 6 :in 2 :cm (bmr/to-cm 6 2)}
           :age 36
           })
  {:inspect-data true})

(defcard-doc 
  "### Calories burned by Exercise
  
   In addition to BMR, let's also calculate amount of calories
   burned based on estimated level of exercise. [The Harris-Benedict](https://en.wikipedia.org/wiki/Harris–Benedict_equation) equation takes the BMR and gives a multiplier based on estimated amount of weekly exercise. Here are the multiplier values for the different levels of exercise:"

  (dc/mkdn-pprint-source bmr/exercise-levels)

  "And here's the function to calculate the total calories burned"

  (dc/mkdn-pprint-source bmr/harris-benedict)

  "Let's use radio buttons this time. Here's the code for the radio
   button widget"

  (dc/mkdn-pprint-source bmr/exercise-radio)
  (dc/mkdn-pprint-source bmr/energy-calculator)
  
  )

(deftest harris-benedict
  (testing "testing cm"
    (is (= 2397.1848 (bmr/harris-benedict 1.2 1997.654)))))

(defcard
  "### Total Energy"
  (dc/reagent 
   (fn [data _] 
     [:div
      [bmr/bmr-calculator    data]
      [bmr/energy-calculator data]]))
  (r/atom {:gender "m"
           :weight {:value 220 :unit "lbs"} 
           :height {:ft 6 :in 2 :cm (bmr/to-cm 6 2)}
           :age 36
           :exercise bmr/exercise-levels
           })
  {:inspect-data true})

(devcards.core/start-devcard-ui!)

