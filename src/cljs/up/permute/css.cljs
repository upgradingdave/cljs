(ns up.permute.css)

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

(def perm-container 
  {
   :padding         0
   :margin          0
   :list-style      "none"

   :border-top      "1px solid black"
   :border-bottom   "1px solid black"
   :border-left     "1px solid black"

   :display         "flex"
   ;; :display         "-webkit-flex"
   ;; :display         "-webkit-box"
   ;; :display         "-moz-box"
   ;; :display         "-ms-flexbox"
   
   :flex-wrap       "flex-wrap"
   :justify-content "space-around"
   })

(defn perm-child [swap? prefix? suffix?]
  {:background-color (cond 
                       swap?   (:purple colors)
                       prefix? (:icon-green1 colors)
                       suffix? (:icon-blue1  colors) 

                       :else   (:light-blue2 colors))

   :border-right     "1px solid black"

   :padding "5px"
   
   :width  "200px" 
   :height "100px"
   :margin "auto"

   :line-height "100px"
   :color (:black2 colors)
   :font-weight "bold"
   :font-size "3em"
   :text-align "center"

   }
)
