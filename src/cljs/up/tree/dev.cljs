(ns up.tree.dev
  (:require
   [devcards.core        :as dc]
   [reagent.core         :as r]
   [up.tree.core         :as tree])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-doc]]
   [cljs.test            :refer [is testing]]))

(def data 
  (r/atom {:filesystem tree/sample-fs
           :local {:tree {:root 
                          (tree/dir->node [:filesystem] tree/sample-fs)}}}))

(defcard 
  "### Bootstrap Clojurescript Tree"
  (dc/reagent 
   (fn [data _] [tree/tree-widget data]))
  data
  {:inspect-data true})

(defcard-doc 
  "### Reagent Bootstrap Tree Component

   The reagent component looks like this: 

  "
  (dc/mkdn-pprint-source tree/node)

  "The component renders a node by looking inside the `path` out of
global state (`data`). So, the root node is rendered like this:" 

  (dc/mkdn-pprint-source tree/tree-widget)

  "But what if there wasn't anything in `[:local :tree :root]` yet? Before we can render the tree, we need to convert the datastructure found under `[:filesystem]` into the format that the reagent tree understands. I wrote it this way in hopes that I could reuse this code to render trees of other things besides directories. So, there are 2 helper functions for converting directory representation into node representation: "

    (dc/mkdn-pprint-source tree/dir->node)

    (dc/mkdn-pprint-source tree/subdirs->nodes)
  
  "So, \"filesystem\" representations are first converted into \"tree node\" representations, and then react component renders based on the \"tree node\" representation. This makes the code more reusable. Plus it will help to implement lazy loading from the server. Check out how `expand-node` does the translation and adds the nodes to the \"tree node\" datastructure: "

    (dc/mkdn-pprint-source tree/expand-node!)

"`collapse-node` just sets expanded to false. This way, when the folder is re-expanded, it still has the same shape as before:"

    (dc/mkdn-pprint-source tree/collapse-node!)

  "Go back and look at the source code for the `node` component and notice the `css-transition-group` right after the `ul` but before any of the children list items. It's important to remember that [The CSSTransitionGroup component doesn't work unless it's mounted to
the dom](https://facebook.github.io/react/docs/animation.html#animation-group-must-be-mounted-to-work). For example, if the `css-transition-group` was moved so that it was inside the `when expanded` clause, then it wouldn't work! So be careul about that. Here's the css for the transitions:"

  (dc/mkdn-pprint-source tree/style))

(deftest unit-tests
  (testing "Convert dirs into tree nodes"
    (is (=  {:ref [:filesystem], :label "/", :leaf? false, :expanded false}
            (tree/dir->node [:filesystem] tree/sample-fs)))
    (is (=  [{:ref [:filesystem :subdirs 0],
              :label "Users",
              :leaf? false,
              :expanded false}
             {:ref [:filesystem :subdirs 1],
              :label "etc", 
              :leaf? false, 
              :expanded false}]
            (tree/subdirs->nodes [:filesystem] (:subdirs tree/sample-fs))))))

(dc/start-devcard-ui!)
