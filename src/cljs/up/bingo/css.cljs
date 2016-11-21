(ns up.bingo.css)

;; Styles

;;linear-gradient(to right, blue3, blue4)

(def cell-height 110)
(def cell-width  110)
(def font-size   20)

(def gutter-size  15)
(def board-height (+ (* (inc 5) gutter-size) (* 5 cell-height)))
(def board-width  (+ (* (inc 5) gutter-size) (* 5 cell-width)))

(def colors 
  {:black1       "#292d2e"
   :black2       "#444"

   :icon-blue1   "#90b4fe"
   :icon-blue2   "#5881d8"
   :icon-green1  "#91dc47"
   :icon-green2  "#63b132"

   :blue1        "#507dca"
   :blue2        "#4165a2"
   :blue3        "#0f2242"
   :blue4        "#2351a1"

   :light-blue1  "#dee8ec"
   :light-blue2  "#edf6fa"

   :green1       "#5cc70c"

   :grey1        "#292d2e"
   :grey2        "#e4e4e4"
   :purple       "#922790"

   })

(defn board-style []
  {:background-color   (:blue3 colors)
   :width              (str board-width  "px")
   :height             (str board-height "px")
   :border-radius      "6px"
   :position           "relative"

   :-webkit-transform  "translate3d(0,0,0)"
   :-moz-transform     "translate3d(0,0,0)"
   :transform          "translate3d(0,0,0)"
   })

(defn cell-style []
  {
   :background-color (:blue4 colors)
   :height           (str cell-height "px")
   :width            (str cell-width  "px")
   :border-radius    "4px"
   :font-family      "\"Helvetica Neue\", Arial, sans-serif"
   :font-size        (str font-size "px")
   :color            (:green1 colors)
   :line-height      "102px" 
   :font-weight      "bold"
   :text-align       "center"
   :vertical-align   "middle"
   :padding          "auto"

   :-webkit-transform "translate3d(0,0,0)"
   :-moz-transform    "translate3d(0,0,0)"
   :transform         "translate3d(0,0,0)"
   })

(defn cell-pos [top left]
  {:position   "absolute"
   :top        top 
   :left       left
   :cursor     "pointer"
   })

(defn cell-marked [] 
  {:color               (:black2 colors)
   :background-image    "url(\"/public/i/clojure-logo.png\")"
   :background-size     "contain"
   :background-repeat   "no-repeat"
   :background-position "center"
   })

;; /Styles
