(ns up.passwd.dev
  (:require
   [devcards.core            :as dc]
   [reagent.core             :as r]
   [up.passwd.core           :as gen])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-doc]]))

(defcard 
  "### Random Password Generator
   A [Random Password Generator](/blog/posts/2015-09-15-generate-data-clojure.html) using `test.check.generators` written in [clojurescript using devcards](/blog/posts/2016-01-04-random-generators-in-cljs.html)"
  (dc/reagent (fn [data _]
                [gen/password-generator data]))
  (r/atom {:pwd-length 15
           :result (gen/gen-pwd {:len 15})})
  {:inspect-data true})

(defcard-doc 
  "### How the Form is Built

  The following function uses `hiccup` syntax to define a html form with
  a `Password Length` input text field and a `Generate Password` Button.

  The `Password Length` input text field uses the `on-change` event to
  update a clojurescript reagent atom named `data`.

  The `Generate Password` button reads from the `:pwd-len` key inside
  the data atom to know what length to pass to the `gen-pwd` function.
  It writes the results into the `:results` key of the `data` atom.
"
  (dc/mkdn-pprint-source gen/password-generator))

(defcard-doc 
  "### How the Password Generator Works

  Here are the functions used to generate a random password.
"
  (dc/mkdn-pprint-source gen/char-upper)
  (dc/mkdn-pprint-source gen/char-lower)
  (dc/mkdn-pprint-source gen/char-special)
  (dc/mkdn-pprint-source gen/gen-pwd))

(defcard-doc 
  "### Rendering to the Page

  Finally, we just need a little code to initialize a reagent atom.
"
  (dc/mkdn-pprint-source gen/data)

  "All that we need now is to insert the component into the page. This
  snippet inserts the component if it sees a div on the page with id
  `\"pwd-gen\"`. The component is rendered using reagent's
  `render-component`"
  
  (dc/mkdn-pprint-source gen/main))

(devcards.core/start-devcard-ui!)

