(ns up.event)

(defn prevent-default [evt]
  (.preventDefault evt))

;; deprecated
(defn on-change! [data path] (fn [e] (swap! data assoc-in (conj path :value) 
                                            (.-value (.-target e)))))

