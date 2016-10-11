(ns upgradingdave.core
  (:require
   #_[om.core :as om :include-macros true]
   [sablono.core :as sab :include-macros true]
   [upgradingdave.bmr-dev        :as bmr]
   [upgradingdave.compiler       :as compile]
   [upgradingdave.exif-dev       :as exif]
   [upgradingdave.pwd-dev        :as pwd]
   [upgradingdave.spec-pwd-dev   :as pass]
   [upgradingdave.lattice-dev    :as lat]
   [upgradingdave.pcf-dev        :as pcf]
   [upgradingdave.ics-dev        :as ics]
   [upgradingdave.resize-dev     :as resize]
   [upgradingdave.todo-dev       :as todo]
   [upgradingdave.tree-dev       :as tree]
   [upgradingdave.common-dev     :as common]
   [upgradingdave.orientation-dev :as orient]
   [upgradingdave.spec-form-validation-dev :as sfv])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest]]))

(enable-console-print!)


