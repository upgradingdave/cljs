(ns up.core
  (:require
   [devcards.core :as dc :include-macros true]
   ;;[sablono.core :as sab :include-macros true]
   ;;[up.bmr-dev        :as bmr]
   ;;[up.compiler       :as compile]
   ;;[up.exif-dev       :as exif]
   ;;[up.pwd-dev        :as pwd]
   ;;[up.spec-pwd-dev   :as pass]
   ;;[up.lattice-dev    :as lat]
   ;;[up.pcf-dev        :as pcf]
   ;;[up.ics-dev        :as ics]
   ;;[up.resize-dev     :as resize]
   ;;[up.todo-dev       :as todo]
   [up.alerts.dev     :as alerts]
   [up.notify.dev     :as notify]
   [up.timers.dev     :as timer]
   [up.todo.dev       :as todo]
   [up.webworkers.dev :as wworker]
   ;;[up.tree-dev       :as tree]
   ;;[up.common-dev     :as common]
   ;;[up.orientation-dev :as orient]
   ;;[up.spec-form-validation-dev :as sfv]
   )
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
