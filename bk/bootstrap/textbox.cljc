(ns bootstrap.textbox
  (:require #?(:clj  [clojure.spec       :as s]
               :cljs [cljs.spec          :as s])
            [ud.form :as f]))

(defn text-change! 
  "Check new text input value against a spec and updates state
  appropriately"
  [data path spec]
  #(let [new-val (-> % .-target .-value)]
     (swap! data assoc-in (conj path :value) new-val)
     (if (and spec (s/valid? spec new-val))
       (do
         (swap! data assoc-in (conj path :valid) true)
         (swap! data assoc-in (conj path :error) nil))
       (do
         (swap! data assoc-in (conj path :valid) false)
         (swap! data assoc-in (conj path :error) 
                (s/explain-data spec new-val))))))

;; the first param should be a pair of [root-atom path]
;; and the value of which should be a field
(defn digit-input 
  "A bootstrap text input that only accepts digits"
  [[data path] & [opts]]
  (let [st      (merge (get-in @data path) opts)
        parsed  (s/conform :ud.form/digitbox (get @data path))
        label   (get parsed :field/label)
        value   (get parsed :field/value)
        spec    (get parsed :field/spec)
        valid?  (get parsed :field/valid?)
        error   (get parsed :field/error)
        errors  (known-problems (problems error))
        class   (str "form-group" (if error " has-error")
                     (if valid? " has-success"))]
    [:div {:class class}
     (if label
       [:label {:class "control-label"} label])
     [:input {:class "form-control" 
              :type "text" 
              :value val
              :on-change (text-change! data path spec)}]
     (if errors [:div {:class "help-block with-errors"} 
                 [problems-list errors]])]))

