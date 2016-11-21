(ns up.common)

(defn o->map
  "Convert a javascript object into a clj map"
  [x]
  (into {} (for [k (.keys js/Object x)] [k (aget x k)])))
