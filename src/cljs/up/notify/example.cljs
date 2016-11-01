(ns up.notify.example
  (:require [reagent.core   :as r]
            [up.notify.core :as n]))

(defn main []
  (if-let [node (.getElementById js/document "notify-widget")]
    (let [data (r/atom {})
          notify-path [:notifications]]
      (n/notification-init! data notify-path)
      (r/render-component [n/full-example data notify-path] node))))

