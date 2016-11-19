(ns up.units)

;; unit conversions

(defn kg->lb [kg]
  (* kg 2.20462))

(defn lb->kg [lb]
  (* lb 0.453592))

(defn ft->in [ft]
  (* ft 12))

(defn to-cm [ft in]
  (js/Math.round
   (* (+ (ft->in ft) in) 2.54)))

(defn cm->in [cm]
  (js/Math.round (* cm 0.393701)))

(defn in->ftin 
  "convert inches to feet and inches"
  [in]
  (let [ft (js/Math.floor (/ in 12))
        in (mod in 12)]
    {:ft ft
     :in in}))
