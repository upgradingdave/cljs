(ns up.webworkers.core)

(defn webworker? []
  (undefined? (.-document js/self)))

(defn webworker-create! [data path script-path]
  (let [w (js/Worker. script-path)]
    (swap! data assoc-in path w)
    w))

(defn webworker-get [data path]
  (get-in @data path))

(defn webworker-post [data path val]
  (let [w (webworker-get data path)] 
    (.postMessage w val)
    w))
