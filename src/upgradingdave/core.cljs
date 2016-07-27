(ns upgradingdave.core
  (:require
   #_[om.core :as om :include-macros true]
   [sablono.core :as sab :include-macros true]
   [upgradingdave.bmr-dev        :as bmr]
   [upgradingdave.compiler       :as compile]
   [upgradingdave.exif-dev       :as exif]
   [upgradingdave.pwd-dev        :as pwd]
   [upgradingdave.lattice-dev    :as lat]
   [upgradingdave.pcf-dev        :as pcf]
   [upgradingdave.ics-dev        :as ics]
   [upgradingdave.resize-dev     :as resize]
   [upgradingdave.tree-dev       :as tree]
   [upgradingdave.common-dev     :as common]
   [upgradingdave.orientation-dev :as orient])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest]]))

(enable-console-print!)


