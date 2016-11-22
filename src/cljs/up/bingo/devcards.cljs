(ns up.bingo.devcards
  (:require
   [devcards.core :as dc :include-macros true]
   [up.bingo.dev            :as dev]
   [up.bingo.dev.components :as c]))

(defn main []
  (enable-console-print!)
  (dc/start-devcard-ui!)
  (dev/main))

(defn reload []
  (dev/main))
