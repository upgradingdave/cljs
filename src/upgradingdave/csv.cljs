(ns upgradingdave.csv
  (:require [clojure.string :as str]))

;; Took this from 
;; https://github.com/testdouble/clojurescript.csv/blob/master/src/testdouble/cljs/csv.cljs

(defn- wrap-in-quotes [s]
  (str "\"" s "\""))

(defn- seperate [data separator quote]
  (if quote
    (str/join separator (map wrap-in-quotes data))
    (str/join separator data)))

(defn- write-data [data separator newline quote]
  (str/join newline (map #(seperate % separator quote) data)))

(def ^:private newlines
  {:lf "\n" :cr+lf "\r\n"})

(def ^:private newline-error-message
  (str ":newline must be one of [" (str/join "," (keys newlines)) "]"))

(defn write-csv
  "Writes data to String in CSV-format.
  Accepts the following options:
  :separator - field seperator
               (default ,)
  :newline   - line seperator
               (accepts :lf or :cr+lf)
               (default :lf)
  :quote     - wrap in quotes
               (default false)"

  {:arglists '([data] [data & options]) :added "0.1.0"}
  [data & options]
  (let [{:keys [separator newline quote] :or {separator "," newline :lf quote false}} options]
    (if-let [newline-char (get newlines newline)]
      (write-data data
                  separator
                  newline-char
                  quote)
      (throw (js/Error. newline-error-message)))))
