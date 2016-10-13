(ns upgradingdave.pass-cljs-macro
  (:require [cljs.spec]))

(defmacro and'
  [pred-forms]
  `(cljs.spec/and-spec-impl '~pred-forms ~pred-forms nil))
