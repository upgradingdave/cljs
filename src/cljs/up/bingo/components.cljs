(ns up.bingo.components
  (:require [up.bingo.css   :as css]
            [up.bingo.core  :as b]))

;; Reagent Components

(defn togglefn [k] 
  (fn [col]
    (let [orig (get col k)]
      (assoc col k (not orig)))))

(defn toggle-marked 
  [!state path-to-cell]
  (let [{:keys [marked]} (get-in @!state path-to-cell)]
    (if (not marked)
      (swap! !state update-in path-to-cell (togglefn :marked)))))

(defn cell 
  [!state path-to-cell
   & [{:keys [cell-width cell-height font-size gutter-size click-fn read-only] 
       :as opts
       :or {cell-width  b/default-cell-width
            cell-height b/default-cell-height
            font-size   b/default-font-size
            gutter-size b/default-gutter-size
            read-only   true}}]]
  (let [{:keys [top left key value marked]} (get-in @!state path-to-cell)]
    [:div {:key key
           :style (-> (css/cell-style cell-width cell-height font-size)
                      (merge (if top (css/cell-pos top left 
                                                   cell-width 
                                                   cell-height 
                                                   gutter-size
                                                   read-only)))
                      (merge (if marked (css/cell-marked))))
           :on-click (fn [e] 
                       (if (not read-only)
                         (do
                           (toggle-marked !state path-to-cell)
                           (when click-fn (click-fn path-to-cell)))))}
     value]))

(defn board 
  [!state board-path 
   & [{:keys [cell-width cell-height gutter-size font-size click-fn]
       :as opts
       :or {cell-width  b/default-cell-width
            cell-height b/default-cell-height
            gutter-size b/default-gutter-size
            font-size   b/default-font-size}}]]
  (let [cells (get-in @!state board-path)]
    [:div {:key (gensym) 
           :style (css/board-style cell-width cell-height gutter-size)}
     (doall (map-indexed #(cell !state (conj board-path %1) opts) cells))]))

(defn leader-boards 
  "Finds list of [:players :boards] in global state and displays them
  in horizontal row"
  [!state boards-path
   & [{:keys [cell-width cell-height gutter-size font-size click-fn]
       :as opts
       :or {cell-width  b/default-cell-width
            cell-height b/default-cell-height
            gutter-size b/default-gutter-size
            font-size   b/default-font-size}}]]
  (let [boards      (get-in @!state boards-path)
        board-paths (map-indexed #(conj boards-path %1 :board) boards)]
    [:div.container
     
     (when (not (empty? boards))
       [:div.row
        [:h3 "Leader Boards"]
        (doall (for [path board-paths]
                 [:div.pull-left 
                  {:style {:padding "5px"}
                   :key (first (take-last 2 path))}
                  (board !state path opts)]))])]
    ))

;; /Reagent Components
