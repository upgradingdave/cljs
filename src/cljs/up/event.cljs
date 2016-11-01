(ns up.event)

(defn prevent-default [evt]
  (.preventDefault evt))

(defn on-change! [data path]
  (fn [e]
    (swap! data assoc-in path (.-value (.-target e)))))
