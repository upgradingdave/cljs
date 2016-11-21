(ns up.bingo.core  
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core    :as r]
            [up.bingo.css    :as css]
            [up.bingo.aws    :as aws]
            [up.datetime     :as dt]
            [up.cookies.core :as c]
            [cljs.core.async :refer [put! chan <! >! close!]]))

(def possible 
  #{"atom" 
    "big data" 
    "boot"
    "conj"
    "complect"
    "decomplect"
    "destructure"
    "emacs"
    "eval"
    "filter"
    "lein"
    "macro"
    "map"
    "paper"
    "reduce"
    "reify"
    "ring"
    "simple"
    "state"
    "transduce"
    "transducer"
    "transact"
    "react"
    "reader"
    "list"})

;; Session Management

(defn new-session []
  (js/console.log "NEW SESSSION")
  {:sessionid (str (random-uuid))
   :timestamp (dt/now-in-millis)})

(defn save-session! [!state path session]
  (swap! !state assoc-in (conj path :session) session)
  (c/set-cookie! :bingo session))

(defn get-session [!state path]
  (get-in @!state (conj path :session)))

;; /Session Management

;; Data

(defn make-cell [value top-idx left-idx]
  {:key (str top-idx left-idx) 
   :top  (str (+ (* top-idx  css/cell-height) 
                 (* (inc top-idx)  css/gutter-size)) "px")
   :left (str (+ (* left-idx css/cell-width)  
                 (* (inc left-idx) css/gutter-size)) "px") 
   :value value})

(defn make-row 
  "Transform list of words into a row (list of reagent cell components)"
  [data idx]
  (map-indexed #(make-cell %2 idx %1) data))

(defn make-board 
  "Transforms list of words into list of rows"
  [data]
  (into []
        (apply concat
               (map (fn [[idx row]] (make-row row idx))
                    (map-indexed vector (partition 5 data))))))

(defn new-board []
  (make-board (take 25 possible)))

;; TODO calculate score
(defn get-board [!state bingo-path]
  (get-in @!state (conj bingo-path :board)))

;; TODO implement this to get board from db. Probably use async to
;; make it synchronous
(defn load-board [sessionid]
  (go (<! (aws/<run aws/get-item sessionid))))

(defn save-board! [!state bingo-path b]
  (swap! !state assoc-in (conj bingo-path :board) b)
  (go (let [sessionid (:sessionid (get-session !state bingo-path))]
        (<! (aws/<run aws/put-item 
                      {:sessionid sessionid 
                       :score 0
                       :board b})))))

;; /Data

;; Reagent Components

(defn togglefn [k] 
  (fn [col]
    (let [orig (get col k)]
      (assoc col k (not orig)))))

(defn cell [!state path-to-cell]
  (let [{:keys [key top left value marked]} (get-in @!state path-to-cell)
        bingo-path (into [] (drop-last 2 path-to-cell))]
    [:div {:key key
           :style (-> (css/cell-style)
                      (merge (if top (css/cell-pos top left)))
                      (merge (if marked (css/cell-marked))))
           :on-click #(do 
                        (swap! !state update-in path-to-cell
                               (togglefn :marked))
                        (save-board! !state 
                                     bingo-path 
                                     (get-board !state bingo-path)))}
     value]))

(defn board [!state bingo-path]
  [:div {:style (css/board-style)}
   (map-indexed #(cell !state (conj bingo-path :board %1)) 
                (get-in @!state (conj bingo-path :board)))])

;; /Reagent Components

;; State

(def !state (r/atom {}))

(defn init!
  [!state bingo-path]
  ;; Try to get sessionid from cookie, if no cookie, then create one
  (let [session (or (c/get-cookie :bingo) (new-session))]
    (save-session! !state bingo-path session))

  ;; Try to load board from db. If no board, then create one
  (go (let [sessionid (:sessionid (get-session !state bingo-path)) 
            
            [_ {:keys [sessionid score board]}] 
            (<! (aws/<run aws/get-item sessionid))

            board (or board (new-board))]
        (js/console.log "BOARD" board)
        (save-board! !state bingo-path board))))

;; /State

(defn main [])

(defn reload [])
