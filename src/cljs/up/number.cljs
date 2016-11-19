(ns up.number
  (:require [goog.string :as gstring]
            [goog.string.format]))

(defn format-decimal 
  "Convenience function for formatting decimals. Will format to 2
  places, or optionally specify the number of places you want to
  display"
  [decimal & [places]]
  (let [places (or places 2)]
    (gstring/format (str "%." places "f") decimal)))


