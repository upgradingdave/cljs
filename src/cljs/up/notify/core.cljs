(ns up.notify.core)

(defn notification-supported? []
  js/Notification)

(defn notification-permission []
  (.-permission js/Notification))

(defn notification-permitted? []
  (= "granted" (notification-permission)))

(defn notification-request-permission! [data path]
  (.requestPermission 
   js/Notification 
   (fn [permission]
     (swap! data assoc-in path {:enabled true
                                :permitted (= "granted" permission)
                                :permission permission}))))

(defn notification-enabled? [data path]
  (get-in @data (conj path :enabled)))

(defn disable! [data path]
  (swap! data assoc-in (conj path :enabled) false))

(defn enable! [data path]
  (swap! data assoc-in (conj path :enabled) true))

(defn create-notification 
  [enabled? & [{:keys [title body icon sound]
                :or {title ""
                     body  ""
                     icon "/i/clojure-logo.png"}}]]
  (when (and (notification-permitted?) enabled?)
    (let [n (js/Notification. 
             title 
             (clj->js {:body body
                       :icon icon}))]
      (when sound
        (.play (js/Audio. sound)))
      ;;(js/setTimeout #(.close n), 5000)
      n)))

(defn notification-init! 
  [data path]
  (if (notification-permitted?)
    (let [permission (notification-permission)]
      (swap! data assoc-in path 
             {:enabled    (get-in @data (conj path :enabled) true)
              :permitted  (= "granted" permission)
              :permission permission}))
    (notification-request-permission! data path)))
