(ns up.bingo.devcards
  (:require
   [devcards.core :as dc :include-macros true]
   [up.bingo.dev.components :as c]
   [up.bingo.dev.aws        :as aws]
   [up.bingo.dev.session    :as session]))

(defn main []
  (enable-console-print!)
  (dc/start-devcard-ui!)

  (session/main))

(defn reload []
  (session/main))
