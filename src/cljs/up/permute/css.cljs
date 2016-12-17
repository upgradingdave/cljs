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
  {:display "flex"
   :flex-direction "row"
   :flex-wrap "wrap"
   :justify-content "flex-start" ;;"space-around"
   :align-items "center"
   :padding 0
   :maring 0
   :background-color "white"})

(defn perm-child [swap? prefix? suffix?]
  (let [bg-color (cond 
                   swap?   (:purple colors)
                   prefix? (:icon-green1 colors)
                   suffix? (:icon-blue1  colors) 
                   :else   (:light-blue2 colors))]
    {:height "80px"
     :line-height "20px"
     :margin "10px"
     :padding "30px"
     :text-align "center"
     :font-size "2em"
     :border-radius "8px"
     :color (:black2 colors)
     :background-color bg-color}))
