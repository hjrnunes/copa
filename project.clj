(defproject copa "0.1.2-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238" :scope "provided"]
                 [org.clojure/tools.cli "0.3.5"]
                 [selmer "1.11.7"]
                 [markdown-clj "1.0.2"]
                 [bouncer "1.0.1"]
                 [org.webjars/font-awesome "5.0.9"]
                 [org.webjars.bower/tether "1.4.3"]
                 [org.webjars/jquery "3.3.1-1"]
                 [compojure "1.6.0"]
                 [ring-middleware-format "0.7.2"]
                 [metosin/ring-http-response "0.9.0"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-defaults "0.3.1"]
                 [ring-ttl-session "0.3.1"]
                 [ring "1.6.3" :exclusions [ring/ring-jetty-adapter]]
                 [mount "0.1.12"]
                 [buddy "2.0.0"]
                 [reagent "0.7.0"]
                 [reagent-forms "0.5.40"]
                 [reagent-utils "0.3.1"]
                 [re-frame "0.10.5"]
                 [day8.re-frame/http-fx "0.1.6"]
                 [cljs-ajax "0.7.3"]
                 [metosin/compojure-api "1.1.12"]
                 [metosin/ring-swagger-ui "3.9.0"]
                 [luminus/config "0.8"]
                 [luminus-http-kit "0.1.5"]
                 [luminus-log4j "0.1.5"]
                 [luminus-nrepl "0.1.4"]
                 [luminus-migrations "0.5.0"]
                 [io.rkn/conformity "0.5.1"]
                 [prismatic/plumbing "0.5.5"]
                 [prismatic/schema "1.1.9"]
                 [json-html "0.4.4"]
                 [com.novemberain/monger "3.1.0"]
                 [hodgepodge "0.1.3"]
                 [bidi "2.1.3"]
                 [kibu/pushy "0.3.8"]
                 [funcool/cuerdas "2.0.5"]
                 [cljsjs/semantic-ui "2.2.13-0"]
                 [conman "0.7.7"]
                 [com.h2database/h2 "1.4.197"]
                 [cprop "0.1.11"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.fzakaria/slf4j-timbre "0.3.8"]
                 ]

  :min-lein-version "2.0.0"
  :uberjar-name "copa.jar"
  :jvm-opts ["-server"]
  :resource-paths ["resources" "target/cljsbuild"]

  :main copa.core
  :migratus {:store :database :db ~(get (System/getenv) "DATABASE_URL")}

  :plugins [[lein-environ "1.0.1"]
            [lein-cljsbuild "1.1.1"]
            [lein-cprop "1.0.1"]
            [migratus-lein "0.4.1"]]
  :clean-targets ^{:protect false} [:target-path [:cljsbuild :builds :app :compiler :output-dir] [:cljsbuild :builds :app :compiler :output-to]]
  :cljsbuild
  {:builds
   {:app
    {:source-paths ["src-cljs"]
     :compiler
                   {:output-to    "target/cljsbuild/public/js/app.js"
                    :output-dir   "target/cljsbuild/public/js/out"
                    :externs      ["react/externs/react.js"]
                    :pretty-print true}}}}

  :profiles
  {:uberjar       {:omit-source    true
                   :env            {:production true}
                   :prep-tasks     ["compile" ["cljsbuild" "once"]]
                   :cljsbuild
                                   {:builds
                                    {:app
                                     {:source-paths ["env/prod/cljs"]
                                      :compiler
                                                    {:optimizations :advanced
                                                     :pretty-print  false
                                                     :closure-warnings
                                                                    {:externs-validation :off :non-standard-jsdoc :off}}}}}

                   :aot            :all
                   :source-paths   ["env/prod/clj"]
                   :resource-paths ["env/prod/resources"]}
   :dev           [:project/dev :profiles/dev]
   :test          [:project/test :profiles/test]
   :project/dev   {:dependencies   [[prone "1.5.1"]
                                    [ring/ring-mock "0.3.2"]
                                    [ring/ring-devel "1.6.3"]
                                    [pjstadig/humane-test-output "0.8.3"]
                                    [lein-figwheel "0.5.15"]
                                    [lein-doo "0.1.10"]
                                    [com.cemerick/piggieback "0.2.2"]]
                   :plugins        [[lein-figwheel "0.5.15"]
                                    [lein-doo "0.1.10"]
                                    [org.clojure/clojurescript "1.10.238"]]
                   :cljsbuild
                                   {:builds
                                    {:app
                                     {:source-paths ["env/dev/cljs"]
                                      :compiler
                                                    {:main          "copa.app"
                                                     :asset-path    "/js/out"
                                                     :optimizations :none
                                                     :source-map    true}}
                                     :test
                                     {:source-paths ["src-cljs" "test-cljs"]
                                      :compiler
                                                    {:output-to     "target/test.js"
                                                     :main          "copa.doo-runner"
                                                     ;:optimizations :whitespace
                                                     :optimizations :advanced
                                                     :pretty-print  true}}}}

                   :figwheel
                                   {:http-server-root "public"
                                    :server-port      3449
                                    :nrepl-port       7002
                                    :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"]
                                    :css-dirs         ["resources/public/css"]
                                    :ring-handler     copa.handler/app}
                   :doo            {:build "test"}
                   :source-paths   ["env/dev/clj"]
                   :resource-paths ["env/dev/resources"]
                   :repl-options   {:init-ns user}
                   :injections     [(require 'pjstadig.humane-test-output)
                                    (pjstadig.humane-test-output/activate!)]
                   ;;when :nrepl-port is set the application starts the nREPL server on load
                   :env            {:dev        true
                                    :port       3000
                                    :nrepl-port 7000}}
   :project/test  {:dependencies   [[prone "1.5.1"]
                                     [ring/ring-mock "0.3.2"]
                                     [ring/ring-devel "1.6.3"]
                                     [pjstadig/humane-test-output "0.8.3"]
                                     [lein-figwheel "0.5.15"]
                                     [lein-doo "0.1.10"]
                                     [com.cemerick/piggieback "0.2.2"]]
                   :resource-paths ["env/dev/resources" "env/test/resources"]}
   :profiles/dev  {}
   :profiles/test {}})
