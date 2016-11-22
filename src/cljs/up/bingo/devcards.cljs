(ns up.bingo.devcards
  (:require
   [devcards.core :as dc :include-macros true]
   [up.bingo.dev.components :as c]
   [up.bingo.dev.aws        :as aws]))

(defn main []
  (enable-console-print!)
  (dc/start-devcard-ui!)
  ;;(dev/main)
  )

(defn reload []
  ;;(dev/main)
  )
