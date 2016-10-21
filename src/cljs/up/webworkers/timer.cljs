(ns up.webworkers.timer
  (:require [up.webworkers.core :as c]))

(when (c/webworker?)
  (js/self.setInterval #(js/self.postMessage nil) 1000))

