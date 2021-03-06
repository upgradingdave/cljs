(ns up.core
  (:require
   [devcards.core :as dc :include-macros true]
   [up.alerts.dev            :as alerts]
   [up.auth0.dev             :as auth0]
   [up.bingo.dev.components  :as bingo1]
   [up.cookies.dev           :as cookies]
   ;;removed: [up.bmr.dev               :as bmr]
   [up.css.dev               :as css]
   [up.github.dev            :as github]
   [up.health.dev            :as health]
   [up.fun.euler.lattice-dev :as lat]
   [up.fun.pcf-dev           :as pcf]
   [up.ical.dev              :as ical]
   [up.img.exif.dev          :as exif]
   [up.img.orientation.dev   :as orientation]
   [up.img.resize.dev        :as resize]
   [up.notify.dev            :as notify]
   [up.nutrition.dev         :as nutrition]
   [up.passwd.dev            :as pwd]
   [up.permute.dev           :as permute]
   [up.person.dev            :as person]
   ;; needs work: [up.passwd.spec.dev     :as pass]
   [up.reframe.dev           :as reframe]
   [up.timers.dev            :as timer]
   [up.tree.dev              :as tree]
   [up.todo.dev              :as todo]
   [up.validate.dev          :as validate]
   ;; needs work: [up.webworkers.dev        :as wworker]
   )
  (:require-macros
   [devcards.core :refer [defcard deftest]]))

;; This is used in :init-fns in *.cljs.edn file. So it has to do with
;; cljs compilation
(defn main []
  (enable-console-print!)
  (dc/start-devcard-ui!)
  ;;(wworker/main)
  (notify/main)
  (timer/main)
  (cookies/main)
  (auth0/main)
  (reframe/main)
  (css/main)
  (github/main)
  (permute/main))

;; This is used in :on-jsload in boot-reload configuration in
;; build.boot
(defn reload []
  ;;(wworker/main)
  (notify/main)
  (timer/main)
  (cookies/main)
  (auth0/main))
