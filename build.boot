(merge-env!
 :resource-paths #{"src/cljs" "resources/public"}
 :dependencies 
 '[[org.clojure/clojure       "1.9.0-alpha10"]
   [org.clojure/clojurescript "1.9.229"]
   [org.clojure/core.async    "0.2.385"]

   [org.clojure/test.check    "0.9.0"]
   [org.clojure/tools.reader  "1.0.0-beta1"]
   [com.cognitect/transit-clj "0.8.285"]

   [adzerk/bootlaces        "0.1.13" :scope "test"]
   [adzerk/boot-reload      "0.4.12" :scope "test"]
   [adzerk/boot-cljs        "1.7.228-1"           ] 
   [adzerk/boot-cljs-repl   "0.3.3"  :scope "test"]

   [com.cemerick/piggieback "0.2.1"  :scope "test"]
   [weasel                  "0.7.0"  :scope "test"]
   [devcards "0.2.1" :exclusions [cljsjs/react]]
   [reagent  "0.5.1" :exclusions [cljsjs/react]]
   [cljsjs/react-with-addons "0.14.7-0"]

   ;; TODO: I forget how this works, but I thought tools.nrepl should
   ;; be configured by boot-dave ???
   [org.clojure/tools.nrepl "0.2.12" :scope "test"]
   [upgradingdave/boot-dave "0.1.1"  :scope "test"]
   [pandeiro/boot-http      "0.6.3"  :scope "test"]
   



   [com.andrewmcveigh/cljs-time "0.4.0"]
   [upgradingdave/password      "0.2.2"]

   [cljsjs/exif "2.1.1-1"]
   [cljsjs/ical "1.2.1-1"]])

(require '[adzerk.boot-cljs             :refer [cljs]]
         '[adzerk.boot-cljs-repl        :refer [cljs-repl start-repl]]
         '[adzerk.boot-reload           :refer [reload]]
         '[upgradingdave.boot-cider     :refer [cider]]
         '[pandeiro.boot-http           :refer [serve]])

(task-options!
 pom {:project 'upgradingdave/cljs
      :version "0.1.0"
      :description "Dave's cljs playground"
      :license {"The MIT License (MIT)" 
                "http://opensource.org/licenses/mit-license.php"}})

(deftask dev
  "Sets up environment for development"
  []
  (comp
   (cider)
   (serve :dir "target")
   (watch)
   (reload 
    :on-jsload 'up.core/reload
    :ids #{"devcards"})
   (cljs-repl)
   (cljs :compiler-options {:devcards true}
         :ids #{"devcards" "timer" "wworker"})
   (target)))

(deftask devcards
  "Create self contained js files for each individual dev card"
  []
  (comp
   (serve :dir "target")
   (watch)
   (cljs :compiler-options {:devcards true
                            :optimizations :advanced}
         :ids #{"notifydev"})))

(deftask build []
  (comp
   (cljs :ids #{"notifydev"} 
         :optimizations :advanced)
   (target)))
