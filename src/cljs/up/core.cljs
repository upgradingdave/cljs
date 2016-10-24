(ns up.core
  (:require
   [devcards.core :as dc :include-macros true]
   [up.alerts.dev            :as alerts]
   [up.bmr.dev               :as bmr]
   [up.fun.euler.lattice-dev :as lat]
   [up.fun.pcf-dev           :as pcf]
   [up.ical.dev              :as ical]
   [up.img.exif.dev          :as exif]
   [up.img.orientation.dev   :as orientation]
   [up.img.resize.dev        :as resize]
   [up.notify.dev            :as notify]
   [up.passwd.dev            :as pwd]
   ;; TODO: need to fix the cljc for password gen
   ;;[up.passwd.spec.dev     :as pass]
   [up.timers.dev            :as timer]
   [up.tree.dev              :as tree]
   [up.todo.dev              :as todo]
   [up.webworkers.dev        :as wworker])
  (:require-macros
   [devcards.core :refer [defcard deftest]]))

;; This is used in :init-fns in *.cljs.edn file. So it has to do with
;; cljs compilation
(defn main []
  (enable-console-print!)
  (dc/start-devcard-ui!)
  (wworker/main)
  (notify/main))

;; This is used in :on-jsload in boot-reload configuration in
;; build.boot
(defn reload []
  (wworker/main)
  (notify/main))
