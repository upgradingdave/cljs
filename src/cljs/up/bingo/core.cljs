(ns up.bingo.core  
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core    :as r]
            [up.bingo.css    :as css]
            [up.bingo.aws    :as aws]
            [up.datetime     :as dt]
            [up.cookies.core :as c]
            [cljs.core.async :refer [put! chan <! >! close!]]))

;; Session Management

(defn new-session []
  {:sessionid    (str (random-uuid))
   :last_updated (dt/unparse dt/iso-8601-format (dt/now))})

(defn save-session! [!state path session]
  (swap! !state assoc-in (conj path :session) session)
  (c/set-cookie! :bingo session))

(defn get-session [!state path]
  (get-in @!state (conj path :session)))

;; /Session Management

;; Data

(def default-cell-height 110)
(def default-cell-width  110)
(def default-font-size   20)
(def default-gutter-size 15)

(defn make-cell 
  [value top-idx left-idx]
  {:key   (str top-idx left-idx) 
   :value value
   :top   top-idx
   :left  left-idx})

(defn make-row 
  "Transform list of words into a row (list of reagent cell components)"
  [data idx 
   & [{:keys [cell-width cell-height gutter-size] 
       :as opts
       :or {cell-width  default-cell-width
            cell-height default-cell-height
            gutter-size default-gutter-size}}]]
  (map-indexed #(make-cell %2 idx %1) data))

(defn make-board 
  "Transforms list of words into list of rows"
  [data 
   & [{:keys [cell-width cell-height gutter-size] 
       :as opts
       :or {cell-width  default-cell-width
            cell-height default-cell-height
            gutter-size default-gutter-size}}]]
  (into [] 
        (apply concat
               (map (fn [[idx row]] 
                      (make-row row idx cell-width cell-height gutter-size))
                    (map-indexed vector (partition 5 data))))))

;; shouldn't need this anymore
;; (defn resize-board 
;;   "Adjust `top` and `left` attributes of all cells in a board"
;;   [orig cell-width cell-height gutter-size]
;;   (let [new (map #(dissoc %1 :value) 
;;                  (make-board (range 25)
;;                              cell-width cell-height gutter-size))]
;;     (into [] (map-indexed #(merge (get orig %1) %2) 
;;                           (map #(dissoc %1 :value) new)))))

;; (defn new-board [cell-width cell-height gutter-size]
;;   (js/console.log "Making New Board ...")
;;   (make-board (take 25 possible) cell-width cell-height gutter-size))

;; TODO calculate score
(defn get-board [!state bingo-path]
  (get-in @!state (conj bingo-path :board)))

(defn load-board [sessionid]
  (go (<! (aws/<run aws/get-item sessionid))))

;; :on-click #(do 
;;              (swap! !state update-in path-to-cell
;;                     (togglefn :marked))
;;              (save-board! !state 
;;                           bingo-path 
;;                           (get-board !state bingo-path)))

(defn save-board! [!state bingo-path b]
  (swap! !state assoc-in (conj bingo-path :board) b)
  (go (let [{:keys [sessionid last_updated]} (get-session !state bingo-path)]
        (<! (aws/<run aws/put-item 
                      {:sessionid sessionid 
                       :last_updated last_updated
                       :score 0
                       :board b})))))

;; /Data


;; State

(def !state (r/atom {}))

(defn init!
  [!state bingo-path]
  ;; Try to get sessionid from cookie, if no cookie, then create one
  (let [{:keys [sessionid last_updated] :as session} 
        (or (c/get-cookie :bingo) (new-session))]
    
;;    (js/console.log "INIT" sessionid last_updated)
    (save-session! !state bingo-path session)

    ;; Try to load board from db. If no board, then create one
    ;; (go (let [res (<! (aws/<run aws/get-item sessionid last_updated))
    ;;           err (:error res)
    ;;           ;; if successful, get the board from response, otherwise
    ;;           ;; new board
    ;;           board (or (:board (second res)) 
    ;;                     (new-board css/cell-width 
    ;;                                css/cell-height 
    ;;                                css/gutter-size))]
    ;;       (when err
    ;;         (swap! !state assoc-in (conj bingo-path :error) err))
    ;;       (save-board! !state bingo-path board)))

    ;; Try to load other boards from db.
    ;; (go (let [res (<! (aws/<run aws/scan))
    ;;           err (:error res)
    ;;           others (:Items res)
    ;;           player-path (conj bingo-path :players)
    ;;           ;; resize the other player's boards
    ;;           others (map (fn [{:keys [board]}] 
    ;;                         (resize-board board 50 50 5)) others)
    ;;           ]
    ;;       (if err
    ;;         (swap! !state assoc-in (conj player-path :error) err)
    ;;         (swap! !state assoc-in (conj player-path :boards) others)
    ;;         )))
    ))

;; /State

(defn main [])

(defn reload [])



