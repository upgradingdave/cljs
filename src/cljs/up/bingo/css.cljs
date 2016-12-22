(ns up.bingo.css)

;; Styles

;;linear-gradient(to right, blue3, blue4)

(defn board-height [cell-height gutter-size] 
  (+ (* 6 gutter-size) (* 5 cell-height)))

(defn board-width [cell-width gutter-size] 
  (+ (* 6 gutter-size) (* 5 cell-width)))

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

(defn board-style [cell-width cell-height gutter-size]
  {:background-color   (:blue3 colors)
   :width              (str (board-width  cell-width  gutter-size) "px")
   :height             (str (board-height cell-height gutter-size) "px")
   :border-radius      "6px"
   :position           "relative"

   :-webkit-transform  "translate3d(0,0,0)"
   :-moz-transform     "translate3d(0,0,0)"
   :transform          "translate3d(0,0,0)"
   })

(defn cell-style [cell-width cell-height font-size]
  {
   :background-color (:blue4 colors)
   :height           (str cell-height "px")
   :width            (str cell-width  "px")
   :border-radius    "4px"
   :font-family      "\"Helvetica Neue\", Arial, sans-serif"
   :font-size        (str font-size "px")
   :color            (:green1 colors)
   :line-height      (str (/ cell-height 1.1) "px") 
   :font-weight      "bold"
   :text-align       "center"
   :vertical-align   "middle"

   :-webkit-transform "translate3d(0,0,0)"
   :-moz-transform    "translate3d(0,0,0)"
   :transform         "translate3d(0,0,0)"
   })

(defn read-only [read-only]
  {:cursor     (if (not read-only) "pointer")})

(defn cell-pos [top left cell-width cell-height gutter-size]
  {:position   "absolute"
   :top        (str (+ (* top cell-height) 
                 (* (inc top) gutter-size)) "px")
   :left       (str (+ (* left cell-width)  
                 (* (inc left) gutter-size)) "px")
   })

(defn cell-marked [] 
  {:color               (:black2 colors)
   :background-image    "url(\"/public/i/clojure-logo.png\")"
   :background-size     "contain"
   :background-repeat   "no-repeat"
   :background-position "center"
   })

(defn show-grid 
  "Add to row to show grid"
  []
  {:padding-top "2px"
   :padding-bottom "2px"
   :background-color "rgba(86,61,124,.15)"
   :border "1px solid rgba(86,61,124,.2)"})

;; /Styles
