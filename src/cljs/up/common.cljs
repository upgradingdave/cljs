(ns up.common
  (:require [cljs.core.async    :refer [put! chan <! >! close!]]
            [goog.net.EventType :as ge])
  (:import [goog.net XhrIo CorsXmlHttpFactory]))

(defn o->map
  "Convert a javascript object into a clj map"
  [x]
  (into {} (for [k (.keys js/Object x)] [k (aget x k)])))

(defn empty-or-nil? [data]
  (or (nil? data)
      (undefined? data)
      (empty? (js->clj data))))

(defn <run
  "This function is useful for putting data into a channel from an
  async response. For example, say you have (fn foo [a b cb]). You can
  do this: (go (<! (<run foo a b))). Note that this is a higher order
  function that configures which xform to use in order to process the
  values of the arguments passed to the callback. Inspired by
  http://www.lispcast.com/core-async-code-style."
  [xform]
  (fn 
    [f & args]
    (let [c (chan 1 xform)
          cb (fn [& args]
               (put! c args)
               (close! c))]
      (apply f (concat args [cb]))
      c)))

;; Ajax
(defn xhr 
  "cb is a function that accepts single result paramater"
  [cb] 
  (doto (goog.net.XhrIo. (CorsXmlHttpFactory.))
    (.listen ge/COMPLETE cb)))

(defn send [url & [cb]]
  (.send (xhr cb) url))

(defn <send [url]
  (let [xform (map (fn [[result]] 
                      (js->clj (.getResponseJson (.-target result)))))
        run   (<run xform)]
    (run send url)))
