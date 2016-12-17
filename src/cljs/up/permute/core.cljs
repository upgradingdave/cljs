(ns up.permute.core
  (:require 
   [up.permute.css :as css]
   [reagent.core   :as r]
   [cljsjs.react-flip-move]
   [re-frame.core  :refer [dispatch-sync
                           dispatch
                           subscribe
                           reg-event-db 
                           reg-sub
                           reg-event-fx]]
   [day8.re-frame.undo :as undo :refer [undoable clear-history!]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def flip-move (r/adapt-react-class js/FlipMove))

;; functions

(defn str->perm 
  "convert string into perm datastructure"
  [s]
  (into (sorted-map)
        (for [[i c] (map-indexed vector s)]
          [i {:value c
              :id    i}])))

(defn perm->str
  "convert perm datastructure into string"
  [perm]
  (apply str (map (fn [[k v]] (:value v)) perm)))

(defn swap [m i1 i2]
  (assoc m i2 (get m i1) i1 (get m i2)))

(defn reverse-suffix [perm suffix-idx]
  (loop [perm perm 
         x suffix-idx 
         y (- (count perm) 1)]
    (if (>= x y)
      perm
      (recur (swap perm x y) (inc x) (dec y)))))

(defn clear-pivot-suffix [perm]
  (into (sorted-map) 
        (map (fn [[k v]] 
               [k {:value (:value v)
                   :id    (:id v)}]) perm)))

(defn find-suffix 
  "The suffix is the right most index n where all indexes n+1
  decrease. Find the index where the suffix starts"
  [perm]
  (let [reversed (reverse (keys perm))
        idx      (first reversed)
        xs       (rest reversed)]
    (loop [idx idx xs xs]
      (let [v1   (:value (get perm idx)) 
            next (first xs)
            v2   (:value (get perm next))]
        (if (or (empty? xs) (> v1 v2))
          idx
          (recur next (rest xs)))))))

(defn find-swap
  "Find the right most index n inside the suffix that has value larger
  than the pivot"
  [perm suffix-idx]
  (let [keys     (keys perm)
        reversed (reverse keys)
        idx      (first reversed)
        xs       (rest reversed)]
    (loop [idx idx xs xs]
      (let [v1   (:value (get perm idx))
            v2   (:value (get perm (dec suffix-idx)))]
        (if (or (<= idx suffix-idx) (> v1 v2))
          idx
          (recur (first xs) (rest xs)))))))

;; subscription handlers

(reg-sub ::perm-input  (fn [db _] (::perm-input db)))
(reg-sub ::perm        (fn [db _] (::perm db)))
(reg-sub ::suffix      (fn [db _] (::suffix db)))
(reg-sub ::play?       (fn [db _] (::play? db)))
(reg-sub ::done?       (fn [db _] (::done? db)))
(reg-sub ::interval    (fn [db _] (::interval db)))

;; event handlers

(reg-event-db                 
 ::initialize                 
 (fn
   [db [_ val]]
   (let [i      val
         steps [::find-suffix
                ::find-swap
                ::swap
                ::reverse]]
     (-> db
         (dissoc ::suffix)
         (merge {::perm (str->perm i)
                 ::perm-input i
                 ::play? false
                 ::done? false
                 ::step  (count steps)
                 ::steps steps
                 ::interval 350})))))

(reg-event-db
 :purge-undos
 (fn [db _]
   (clear-history!)
   db))

(reg-event-db
 ::perm-input
 (fn
   [db [_ val]]
   (assoc-in db [::perm-input] val)))

(reg-event-db
 ::reset
 (fn
   [db [_ val]]
   (assoc-in db [::perm-input] val)))

(reg-event-db
 ::find-suffix
 (fn
   [db _]
   (let [suffix (find-suffix (::perm db))]
     (if (<= suffix 0)
       ;; we're at the largest permutation!
       (-> db
           (assoc-in [::done?] true)
           (assoc-in [::play?] false))
       (-> db
           (assoc-in [::done?] false)
           (assoc-in [::perm suffix :suffix?] true)
           (assoc-in [::perm (dec suffix) :pivot?] true)
           (merge    {::suffix suffix}))))))

(reg-event-db
 ::find-swap
 (fn
   [db _]
   (let [n (find-swap (::perm db) (::suffix db))]
     (-> db
         (assoc-in [::perm n :swap?] true)))))

(defn find-idx [perm key]
  (first (first (filter (fn [[k v]] (get v key)) perm))))

(reg-event-db
 ::swap
 (fn
   [db _]
   (let [s (find-idx (::perm db) :swap?)
         p (find-idx (::perm db) :pivot?)]
     (merge db {::perm (swap (::perm db) p s)}))))

(reg-event-db
 ::reverse
 (fn
   [db _]
   (-> db 
       (update-in [::perm] #(clear-pivot-suffix %))
       (update-in [::perm] #(reverse-suffix % (::suffix db))))))

(defn next-step [curr size] 
  (let [n (inc curr)] (if (> n (dec size)) 0 n)))

(reg-event-fx
 ::do-next
 (undoable)
 (fn [cofx _]
   (let [db        (get-in cofx [:db])
         done?     (get-in db [::done?])
         steps     (get-in db [::steps])
         step-idx  (get-in db [::step])
         next-idx  (next-step step-idx (count steps))
         action    (get-in db [::steps next-idx])
         interval  (get-in db [::interval])]
     (if done?
       {:dispatch []}
       ;; else just dispatch next step
       {:db (assoc-in (:db cofx) [::step] next-idx)
        :dispatch [action]
        :undo "next step"}))))

(reg-event-fx
 ::continue
 (fn [cofx _]
   (let [db        (get-in cofx [:db])
         play?     (get-in db [::play?] false)
         done?     (get-in db [::done?] false)
         steps     (get-in db [::steps])
         step-idx  (get-in db [::step])
         next-idx  (next-step step-idx (count steps))
         action    (get-in db [::steps next-idx])
         interval  (get-in db [::interval])]
     (if (or (not play?) done?)
       {:dispatch []}
       ;; continuously dispatch every interval
       {:db (assoc-in (:db cofx) [::step] next-idx)
        :dispatch-later [{:ms (* interval 1) :dispatch [action]}
                         {:ms (* interval 2) :dispatch [::continue]}]
        ;;:undo "next step"
        }))))

(reg-event-fx
 ::stop
 (fn [cofx _]
   {:db (assoc-in (:db cofx) [::play?] false)}))

(reg-event-fx
 ::play
 (fn [cofx _]
   {:db (assoc-in (:db cofx) [::play?] true)
    :dispatch [::continue]}))

;; components

(defn perm-input []
  (let [v (subscribe [::perm-input])]
    (fn []
      [:div.form-group
       [:label "Enter a sequence of characters to start with"]
       [:input 
        {:class "form-control"
         :style {:width "275px"}
         :type "text"
         :value @v
         :on-change #(dispatch [::perm-input (.-value (.-target %))])}]])))

(defn reset-btn []
  (let [v     (subscribe [::perm-input])
        play? (subscribe [::play?])]
    (fn []
      (let [disabled? @play?]
       [:div.btn.btn-primary 
        {:on-click (fn [e] 
                     (when (not disabled?)
                       (do
                         (dispatch [:purge-undos])
                         (dispatch-sync [::initialize @v])
                         (.preventDefault e))))
         :disabled disabled?}
         [:i.fa.fa-refresh {:title "Reset"}]]))))

;;https://www.nayuki.io/page/next-lexicographical-permutation-algorithm
(defn permutation []
  (let [v        (subscribe [::perm])
        suffix   (subscribe [::suffix])
        interval (subscribe [::interval])]
    (fn []
      (let [suffix @suffix]
        [flip-move {:style css/perm-container
                    :duration @interval
                    :easing "cubic-bezier(0.6, -0.28, 0.735, 0.045)" ;;"linear"
                    :staggerDelayBy 20
                    :staggerDurationBy 20
                    }
         (map (fn [[idx {:keys [id value swap? pivot?]}]]
                (vector :div.square 
                        {:key (str id)
                         :style (css/perm-child 
                                 swap? pivot?
                                 (and suffix (>= idx suffix)))
                         }
                        value))
              @v)]))))

(defn next-btn []
  (let [play? (subscribe [::play?])
        done? (subscribe [::done?])]
    (fn []
      (let [disabled? (or @done? @play?)]
        [:div.btn.btn-primary 
         {:on-click (fn [e] 
                      (when (not disabled?)
                        (do
                          (dispatch [::do-next])
                          (.preventDefault e))))
          :disabled disabled?}
         [:i.fa.fa-forward {:title "Next Step"}]]))))

(defn undo-btn []
  (let [play?  (subscribe [::play?])
        done?  (subscribe [::done?])
        undos? (subscribe [:undos?])]
    (fn []
      (let [disabled? (or (not @undos?) @play?)]
        [:div.btn.btn-primary 
         {:on-click (fn [e] 
                      (when (not disabled?)
                        (do
                          (dispatch [:undo])
                          (.preventDefault e))))
          :disabled disabled?}
         [:i.fa.fa-backward {:title "Undo"}]]))))

(defn play-btn []
  (let [play? (subscribe [::play?])
        done? (subscribe [::done?])]
    (fn []
      (let [disabled? @done?]
        [:div.btn.btn-primary 
         {:on-click (fn [e] 
                      (when (not disabled?)
                        (do
                          (if @play?
                            (dispatch-sync [::stop])
                            (do 
                              (dispatch [:purge-undos])
                              (dispatch [::play])))
                          (.preventDefault e))))
          :disabled disabled?}
         (if @play?
           [:i.fa.fa-stop {:title "Stop"}]
           [:i.fa.fa-play {:title "Play"}])]))))

(defn widget []
  [:div
   [perm-input]
   [:div.btn-group
    [reset-btn]
    [undo-btn]
    [next-btn]
    [play-btn]]
   [permutation]])

(defn main []
  (dispatch-sync [::initialize "012345"]))

(defn dom 
  "This is what bootstraps in advanced compilation"
  []
  (if-let [node (.getElementById js/document "permute")]
    (do
      (main)
      (r/render-component [widget] node)
      (dispatch [::play]))))
