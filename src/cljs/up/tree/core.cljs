(ns up.tree.core
  (:require [reagent.core :as r]))

(def css-transition-group
  (r/adapt-react-class js/React.addons.CSSTransitionGroup))

(def transition-in  1)
(def transition-out 0.2)

(def style 
  (str 
   ".node-enter {
      opacity: 0.01;
      max-height: 0px;
    }

    .node-enter-active {
      opacity: 1;
      max-height: 999px;
      -moz-transition: all " transition-in "s ease-in-out;
      -webkit-transition: all " transition-in "s ease-in-out;
      -ms-transition: all " transition-in "s ease-in-out;
      -o-transition: all " transition-in "s ease-in-out;
      transition: all " transition-in "s ease-in-out;
    }

    .node-leave {
      opacity: 1;
      max-height: 999px;
    }

    .node-leave-active {
      opacity: 0.1;
      max-height: 1px;
      -moz-transition: all " transition-out "s ease-in-out;
      -webkit-transition: all " transition-out "s ease-in-out;
      -ms-transition: all " transition-out "s ease-in-out;
      -o-transition: all " transition-out "s ease-in-out;
      transition: all " transition-out "s ease-in-out;
  }"))

(defn dir->node 
  "Convert a directory to a representation that can be used as a node
  in the tree. `n` is a node and ref is a path to n" 
  [ref n]
  (let [{:keys [label subdirs]} n]
    {:ref ref
     :label label
     :leaf? (empty? subdirs)
     :expanded false}))

(defn subdirs->nodes
  "Given a list of subdirectories, convert them into tree nodes. `ref`
  is a reference to the parent node containing the subdirs."
  [path subdirs]
  (into []
        (map-indexed (fn [i n] (dir->node (conj path :subdirs i) n)) subdirs)))

(defn expand-node! [data path]
  (fn [e]
    (let [{:keys [ref expanded label children] :as node} (get-in @data path)
          subdirs (get-in @data (conj ref :subdirs))
          new-children (if children children (subdirs->nodes ref subdirs)) ]
      (swap! data 
             (fn [old]
               (-> old
                   (assoc-in path (assoc node :expanded true))
                   (assoc-in (conj path :children) new-children)
                   ))))))

(defn collapse-node! [data path]
  (fn [e]
    (let [{:keys [ref expanded label children] :as node} (get-in @data path)]
      (swap! data assoc-in path (assoc node :expanded false)))))

(defn select-node! [data path]
  (fn [e]
    (let [{:keys [selected] :as node} (get-in @data path)]
      (swap! data assoc-in path (assoc node :selected (not selected))))))

(defn node [data path]
  (let [{:keys [leaf? selected expanded label children]} (get-in @data path)]
    [:li {:style {:list-style "none"}}
     [:div {:style {:font-size "24px"
                    :height "1.5em"
                    :line-height "1.5em"}}
      (if leaf?
        [:span {:class "glyphicon glyphicon-folder-close"}]
        (if expanded
          [:button {:class "btn btn-info btn-sm"
                    :on-click (collapse-node! data path)}
           ;;[:span {:class "glyphicon glyphicon-minus"}]
           [:span {:class "glyphicon glyphicon-folder-open"}]
           ]
          [:button {:class "btn btn-info btn-sm"
                    :on-click (expand-node! data path)}
           ;;[:span {:class "glyphicon glyphicon-plus"}]
           [:span {:class "glyphicon glyphicon-folder-close"}]
           ]))
      [:span " "]
      (if selected
        [:button {:class "btn btn-success btn-sm"
                  :on-click (select-node! data path)}
         [:span {:class "glyphicon glyphicon-ok"}]]
        [:button {:class "btn btn-danger btn-sm"
                  :on-click (select-node! data path)}
         [:span {:class "glyphicon glyphicon-ban-circle"}]])
      [:span (str " ") label]] 
     [:ul   
      [css-transition-group {:transition-name "node"
                             :transition-enter-timeout (* transition-in 1000)
                             :transition-leave-timeout (* transition-out 1000)}
       (when expanded
         (map-indexed (fn [i _] ^{:key i} [node data (conj path :children i)])
                      children))]]]))

(defn tree-widget [data]
  [:div {:class "tree well"}
   [:style style]   
   [:ul
    [node data [:local :tree :root]]]])

(def sample-fs 
  {:label "/"
   :subdirs [{:label "Users"
              :subdirs [{:label "dave"
                         :subdirs [{:label "code"}
                                   {:label "fun"}]}
                        {:label "ben"}]}
             {:label "etc"
              :subdirs [{:label "apache"}
                        {:label "postgres"}]}]})

(def data 
  (r/atom {:filesystem sample-fs
           :local {:tree {:root 
                          (->
                           (dir->node [:filesystem] sample-fs)
                           (assoc :expanded true)
                           (assoc :children 
                                  (subdirs->nodes 
                                   [:filesystem] 
                                   (:subdirs sample-fs))))}}}))

(defn main []
  (if-let [node (.getElementById js/document "tree-div")]
    (r/render-component [tree-widget data] node)))

(main)


