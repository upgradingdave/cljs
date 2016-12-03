(ns up.bingo.core  
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core    :as r]
            [up.env          :as env]
            [up.bingo.css    :as css]
            [up.bingo.aws    :as aws]
            [up.bingo.dev.data :as d]
            [up.datetime     :as dt]
            [up.cookies.core :as c]
            [cljs.core.async :refer [put! chan <! >! close!]]))

;; Session Management

(defn new-session []
  {:sessionid    (str (random-uuid))
   :last_updated (dt/unparse dt/iso-8601-format (dt/now))})

(defn save-session! 
  "Update global state and create a cookie with session info"
  [!state path session]
  (swap! !state assoc-in (conj path :session) session)
  (c/set-cookie! :bingo session))

(defn get-session [!state path]
  (get-in @!state (conj path :session)))

;; /Session Management

;; Data
;;TODO should we get the game id from the db??
(def gameid "clojurebingo")

(def default-cell-height 110)
(def default-cell-width  110)
(def default-font-size   20)
(def default-gutter-size 15)

;;110 20 15 
(defn calc-board-size 
  "Calculate dimensions of the board based on size of window"
  []
  (let [{:keys [width height]} (env/get-dimensions)
        smallest (if (< width height) width height)
        size (* 0.75 (/ smallest 5))]
    {:cell-width  size
     :cell-height size
     :gutter-size (/ size 11)
     :font-size   (/ size 6)}))

(defn make-cell 
  [value top-idx left-idx]
  {:key   (str top-idx left-idx) 
   :value value
   :top   top-idx
   :left  left-idx})

(defn make-row 
  "Transform list of words into a row (list of reagent cell components)"
  [data idx]
  (map-indexed #(make-cell %2 idx %1) data))

(defn make-board 
  "Transforms list of words into list of rows"
  [words]
  (into [] 
        (apply concat
               (map (fn [[idx row]] (make-row row idx))
                    (map-indexed vector (partition 5 (take 25 words)))))))

(defn random-board [possible-words]
  (js/console.log "Making New Board ...")
  (make-board (take 25 (shuffle possible-words))))

(defn <load-board [gameid sessionid]
  (go (let [res (<! (aws/<run aws/get-item "bingo.cards" 
                              {:sessionid sessionid
                               :gameid gameid}))]
        (if (:error res)
          {:error  res}
          {:result (:board (second res))}))))

(defn <leader-boards 
  "Query the score index to get list of boards by score excluding the
  current board"
  [gameid sessionid]
  (go (let [key-cond {:gameid {:EQ gameid}}
            filter   {:sessionid {:NE sessionid}}
            res (<! (aws/<run aws/query "bingo.cards" 
                              {:key-cond-map key-cond
                               :query-filter filter
                               :index-name "gameid-score-index"}))]
        (if (:error res)
          {:error  res}
          (do
            ;;(js/console.log "LEADER BOARDS" (pr-str (:Items res)))
            {:result (:Items res)})))))

(defn <load-words []
  (go (let [key-cond {:app_name {:EQ "bingo"}}
            res (<! (aws/<run aws/query "app.config" 
                              {:key-cond-map key-cond}))]
        (if-let [words (into #{} (:words (first (get res :Items))))]
          {:result words}
          {:error  res}))))

(defn save-board! [!state bingo-path gameid b persist?]
  (swap! !state assoc-in (conj bingo-path :board) b)
  (if persist?
    (go (let [{:keys [sessionid 
                      last_updated]} (get-session !state bingo-path)]
          (<! (aws/<run aws/put-item "bingo.cards"
                        {:gameid gameid 
                         :sessionid sessionid 
                         :last_updated last_updated
                         :score 0
                         :board b}))))))

(defn submit-pending! 
  [!state pending-path path-to-cell persist?]
  (let [{:keys [value]} (get-in @!state path-to-cell)]
    (swap! !state assoc-in (conj pending-path value) 
           {:ts (dt/unparse dt/iso-8601-format (dt/now))}))
  ;; TODO persist pending words
  ;; (if persist?)
  )

(defn confirm! 
  [!state pending-path word persist?]
  (swap! !state assoc-in (conj pending-path word) 
         {:ts (dt/unparse dt/iso-8601-format (dt/now))
          :confirmed true})
  ;; TODO persist pending words
  ;; (if persist?)
  )

;; /Data

;; State

(def !state (r/atom {}))

(defn init'
  [!state bingo-path gameid]
  
  ;; Try to get sessionid from cookie, if no cookie, then create one
  (let [{:keys [sessionid last_updated] :as session} 
        (or (c/get-cookie :bingo) (new-session))]
    
    (js/console.log "INIT" sessionid last_updated)
    (save-session! !state bingo-path session)

    (go 
      ;; Try to load board from db using values from session cookie
      (let [{:keys [error result]} 
            (<! (<load-board gameid sessionid))]
        
        (when error
            (swap! !state assoc-in (conj bingo-path :error) error))

        (if result
          ;; we got a board, update local state and move on
          (save-board! !state bingo-path gameid result false)
          
          ;; An error occurred or no board found in db. 
          ;; So let's create a new board.
          ;; Try to get list of possible words from db. If we can't get
          ;; words from db, use words from dev data file
          (let [{:keys [error result]} (<! (<load-words))
                words (or result (d/words))]
            (js/console.log "CREATING NEW BOARD")
            (save-board! !state bingo-path gameid (random-board words) true)))))

    ;; Try to load other boards from db.
    (go (let [{:keys [error result]} 
              (<! (<leader-boards gameid sessionid))]

          (when (not error)
            (swap! !state assoc-in (conj bingo-path :leaders) result))))))

(defn poll [millis cb]
  (js/setTimeout #(do 
                    (js/console.log "Polling ...")
                    (cb)
                    (poll millis cb)) millis))

(defn init! [!state bingo-path gameid]
  
  (let [f #(init' !state bingo-path gameid)]
    (f)
    (poll 10000 f)))

;; /State

