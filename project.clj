(defproject copa "0.1.1-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293" :scope "provided"]
                 [org.clojure/tools.cli "0.3.5"]
                 [selmer "1.10.5"]
                 [markdown-clj "0.9.91"]
                 [bouncer "1.0.0"]
                 [org.webjars/font-awesome "4.7.0"]
                 [org.webjars.bower/tether "1.4.0"]
                 [org.webjars/jquery "3.1.1-1"]
                 [compojure "1.5.2"]
                 [ring-middleware-format "0.7.0"]
                 [metosin/ring-http-response "0.8.1"]
                 [ring-webjars "0.1.1"]
                 [ring/ring-defaults "0.2.2"]
                 [ring-ttl-session "0.3.0"]
                 [ring "1.5.1" :exclusions [ring/ring-jetty-adapter]]
                 [mount "0.1.11"]
                 [buddy "1.2.0"]
                 [reagent "0.6.0"]
                 [reagent-forms "0.5.28"]
                 [reagent-utils "0.2.0"]
                 [re-frame "0.9.1"]
                 [day8.re-frame/http-fx "0.1.3"]
                 [cljs-ajax "0.5.8"]
                 [metosin/compojure-api "1.1.10"]
                 [metosin/ring-swagger-ui "2.2.8"]
                 [luminus/config "0.8"]
                 [luminus-http-kit "0.1.4"]
                 [luminus-log4j "0.1.5"]
                 [luminus-nrepl "0.1.4"]
                 [luminus-migrations "0.2.9"]
                 [io.rkn/conformity "0.4.0"]
                 [prismatic/plumbing "0.5.3"]
                 [prismatic/schema "1.1.3"]
                 [json-html "0.4.0"]
                 [com.novemberain/monger "3.1.0"]
                 [hodgepodge "0.1.3"]
                 [bidi "2.0.16"]
                 [kibu/pushy "0.3.6"]
                 [funcool/cuerdas "2.0.2"]
                 [cljsjs/semantic-ui "2.2.4-0"]
                 [conman "0.6.2"]
                 [com.h2database/h2 "1.4.193"]
                 [cprop "0.1.10"]
                 [com.taoensso/timbre "4.8.0"]
                 [com.fzakaria/slf4j-timbre "0.3.2"]
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
   :project/dev   {:dependencies   [[prone "1.1.4"]
                                    [ring/ring-mock "0.3.0"]
                                    [ring/ring-devel "1.5.1"]
                                    [pjstadig/humane-test-output "0.8.1"]
                                    [lein-figwheel "0.5.8"]
                                    [lein-doo "0.1.7"]
                                    [com.cemerick/piggieback "0.2.2-SNAPSHOT"]]
                   :plugins        [[lein-figwheel "0.5.0-6"]
                                    [lein-doo "0.1.6"]
                                    [org.clojure/clojurescript "1.9.293"]]
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
   :project/test  {:dependencies   [[prone "1.1.4"]
                                    [ring/ring-mock "0.3.0"]
                                    [ring/ring-devel "1.5.1"]
                                    [pjstadig/humane-test-output "0.8.1"]
                                    [lein-figwheel "0.5.8"]
                                    [lein-doo "0.1.7"]
                                    [com.cemerick/piggieback "0.2.2-SNAPSHOT"]]
                   :resource-paths ["env/dev/resources" "env/test/resources"]}
   :profiles/dev  {}
   :profiles/test {}})
