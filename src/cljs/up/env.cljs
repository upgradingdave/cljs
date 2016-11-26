(ns up.env)

;; This namespace deals with maintaining information about the current
;; environment such as window size

;; For bootstrap css:
;; large >=1200
;; medium >=992
;; small >=768
;; extra small <768

(defn get-dimensions []
  {:width  (.-innerWidth js/window)
   :height (.-innerHeight js/window)})

(defn update-dimensions! [!state]
  (swap! !state update-in [:env] merge (get-dimensions)))

(defn watch-env! 
  "Add event handlers necessary to respond to evironment changes"
  [!state]
  (set! js/onresize #(update-dimensions! !state)));
