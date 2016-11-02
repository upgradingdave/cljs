(merge-env!
 :source-paths #{"src/cljs" "src/clj"
                 ;;"src/cljc" cljc is in progress
                 }
 :resource-paths #{"resources"}
 :dependencies 
 '[[org.clojure/clojure       "1.9.0-alpha14"]
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
   [cljsjs/ical "1.2.1-1"]

   [compojure "1.5.1"]
   [hiccup "1.0.5"]

   ])

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

(deftask webworkers 
  "Always use advanced optimization to compile js intended to be
  loaded inside a web worker"
  []
  (comp
   (cljs :compiler-options {:optimizations :advanced}
         :ids #{"public/js/compiled/webworkers/simple"
                "public/js/compiled/webworkers/timer"})
   (target)))

(deftask notifications
  "Build advanced compiled versions of notifications examples"
  []
  (comp
   (cljs :ids #{"public/js/compiled/notify"
                "public/js/compiled/notify_dev"})
   (target)
   (serve :handler 'up.http.core/notify-handler :reload true)
   (wait)))

(deftask devcards
  "Build advanced compiled versions of notifications examples"
  []
  (comp
   (cljs :compiler-options {:optimizations :none}
         :ids #{"public/js/compiled/devcards"})
   (target)))

(deftask dev
  "Sets up environment for development"
  []
  (comp
   (cider)
   (webworkers)
   (serve :handler 'up.http.core/handler :reload false)
   (watch)
   (cljs :ids #{"public/js/devcards"})
   (reload 
    :on-jsload 'up.core/reload
    :ids #{"public/js/devcards"})
   ;;(cljs-repl)
   (target)))
