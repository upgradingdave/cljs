(def compiled-js-dir "resources/public/js/compiled")

(defn cljs-conf [ns]
  {:id (str "prod-" ns)
   :source-paths ["src"]
   :compiler {:main       (str "upgradingdave." ns)
              :asset-path "js/compiled/out"
              :output-to  (str compiled-js-dir "/" ns ".js")
              :optimizations :advanced
              ;;:optimizations :none
              }})

(defn cljs-dev-conf [ns]
  {:id (str "prod-" ns "-devcards")
   :source-paths ["src"]
   :compiler {:main       (str "upgradingdave." ns "-dev")
              :devcards   true
              :asset-path "js/compiled/out"
              :output-to  (str compiled-js-dir "/" ns "-dev.js")
              :optimizations :advanced}})

(defproject upgradingdave "0.1.0-SNAPSHOT"
  :description "Clojurescript Practice"
  :url "http://upgradingdave.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.8.51"]

                 [devcards "0.2.1" :exclusions [cljsjs/react]]
                 [org.clojure/test.check "0.9.0"]
                 [org.clojure/tools.reader "1.0.0-beta1"]
                 [com.cognitect/transit-clj "0.8.285"]

                 [reagent "0.5.1" :exclusions [cljsjs/react]]
                 [cljsjs/react-with-addons "0.14.7-0"]

                 [cljsjs/exif "2.1.1-1"]
                 [cljsjs/ical "1.2.1-1"]]

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-1"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "target"]
  
  :source-paths ["src"]

  :cljsbuild {
              :builds [{:id "devcards"
                        :source-paths ["src"]
                        :figwheel {:devcards true } ;; <- note this
                        :compiler {:main       "upgradingdave.core"
                                   :asset-path "js/compiled/devcards_out"
                                   :output-to ~(str 
                                                compiled-js-dir 
                                                "/upgradingdave_devcards.js")
                                   :output-dir ~(str 
                                                 compiled-js-dir 
                                                 "/devcards_out")
                                   :source-map-timestamp true }}

                       ~(cljs-conf "bmr")
                       ~(cljs-dev-conf "bmr")

                       ~(cljs-conf "exif")
                       ~(cljs-dev-conf "exif")

                       ~(cljs-conf "ics")
                       ~(cljs-dev-conf "ics")

                       ~(cljs-conf "lattice")
                       ~(cljs-dev-conf "lattice")

                       ~(cljs-conf "pcf")
                       ~(cljs-dev-conf "pcf")

                       ~(cljs-conf "pwd")
                       ~(cljs-dev-conf "pwd")

                       ~(cljs-conf "resize")
                       ~(cljs-dev-conf "resize")

                       ~(cljs-conf "tree")
                       ~(cljs-dev-conf "tree")

                       ]}

  :figwheel { :css-dirs ["resources/public/css"] })
